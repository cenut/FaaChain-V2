package com.faa.service;

import com.faa.chain.consensus.*;
import com.faa.chain.consensus.messages.NewHeightMessage;
import com.faa.chain.consensus.messages.NewViewMessage;
import com.faa.chain.consensus.messages.ProposalMessage;
import com.faa.chain.consensus.messages.VoteMessage;

import com.faa.chain.core.Block;
import com.faa.chain.core.BlockHeader;
import com.faa.chain.core.SyncManager;

import com.faa.chain.crypto.Sign;
import com.faa.chain.net.*;
import com.faa.chain.node.Transaction;
import com.faa.chain.utils.SystemUtil;
import com.faa.chain.utils.TimeUtil;
import com.faa.entity.TTransaction;
import com.faa.mapper.BlockRepository;
import com.faa.mapper.TransactionRepository;
import com.faa.utils.CommonUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@EnableAsync
@Service
public class BftService {
    @Resource
    private BlockService blockService;
    @Resource
    private ChannelManager channelManager;
    @Resource
    private PendingManager pendingManager;
    @Resource
    private UnionNodeService unionNodeService;
    @Resource
    private SyncManager syncMgr;

    public enum State {
        NEW_HEIGHT,
        PROPOSE,
        VALIDATE,
        PRE_COMMIT,
        COMMIT,
        FINALIZE
    }

    private static final Logger logger = LoggerFactory.getLogger(BftService.class);
    private int height = 0;
    private int view = 0;
    private Proof proof;
    private Proposal proposal;
    private long timeout;
    private List<String> validators = new ArrayList<>();
    private List<Channel> activeValidators = new ArrayList<>();
    protected BlockingQueue<Event> events = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    private Cache<String, Block> validBlocks = Caffeine.newBuilder().maximumSize(8).build();
    private VoteSet validateVotes = new VoteSet(VoteType.VALIDATE, height, view, validators);
    private VoteSet precommitVotes = new VoteSet(VoteType.PRECOMMIT, height, view, validators);
    private VoteSet commitVotes = new VoteSet(VoteType.COMMIT, height, view, validators);
    private State state;

    private final long bftNewHeightTimeout = 3000L;
    private final long bftProposeTimeout = 12000L;
    private final long bftValidateTimeout = 6000L;
    private final long bftPreCommitTimeout = 6000L;
    private final long bftCommitTimeout = 3000L;
    private final long bftFinalizeTimeout = 3000L;
    private long lastUpdate;

    //切换到 NewHeight 处理逻辑
    public void enterNewHeight() {
        long h = 0;
        state = State.NEW_HEIGHT;

        //FIXME: Currently check height from DB instead of using SyncManager
        height = blockService.getLatestBlock().getId() + 1;
        view = 0;
        proof = new Proof(height, view);
        proposal = null;

        updateValidators();
        clearVotes();
        clearTimerAndEvents();

        logger.info("Entered new_height: height = {},  activeValidators = {} activeValidators.size = {}", height, activeValidators, activeValidators.size());
        resetTimeout(bftNewHeightTimeout);

        NewHeightMessage msg = new NewHeightMessage(height);
        for (Channel c : channelManager.getActiveChannels()) {
            c.getMessageQueue().sendMessage(msg);
        }
    }

    //切换到 Propose 处理逻辑
    public void enterPropose() {
        state = State.PROPOSE;

        resetTimeout(bftProposeTimeout);
        updateValidators();

        if (precommitVotes.isRejected()) {
            view++;
            proof = new Proof(height, view, precommitVotes.getRejections());
            proposal = null;
            clearVotes();
        }

        logger.info("Entered propose: height = {}, view = {}, primary = {}, # connected validators =  {}, proposal = {}", height,
                view, isPrimary(), validators.size(), proposal);

        if (true || isPrimary()) {
            if (proposal == null) {
                Block block = proposeBlock();
                proposal = new Proposal(proof, block, block.getTransactions());
                proposal.sign(CommonUtil.coinbase);
            }

            logger.info("Proposing: {}", proposal);
            ProposalMessage msg = new ProposalMessage(proposal);
            broadcastMessage(msg);
        }

        NewViewMessage msg = new NewViewMessage(proof);
        for (Channel c : activeValidators) {
            c.getMessageQueue().sendMessage(msg);
        }
    }

    //切换到 Validate 处理逻辑
    public void enterValidate() {
        state = State.VALIDATE;
        logger.info("Entered validate: proposal = {}, votes = {} {} {}", proposal != null, validateVotes,
                precommitVotes, commitVotes);

        resetTimeout(bftValidateTimeout);

        boolean valid = (proposal != null)
                && validateBlockProposal(proposal.getBlock(), proposal.getTransactions());

        Vote vote = valid ? Vote.newApprove(VoteType.VALIDATE, height, view, Base64.getEncoder().encodeToString(proposal.getBlock().getHash()), CommonUtil.coinbase.toAddress())
                : Vote.newReject(VoteType.VALIDATE, height, view, CommonUtil.coinbase.toAddress());
        vote.sign(CommonUtil.coinbase);

        validateVotes.addVote(vote);
        VoteMessage msg = new VoteMessage(vote);
        broadcastMessage(msg);
    }

    //切换到 PreCommit 处理逻辑
    public void enterPreCommit() {
        state = State.PRE_COMMIT;
        logger.info("Entered pre_commit: proposal = {}, votes = {} {} {}", proposal != null, validateVotes,
                precommitVotes, commitVotes);

        resetTimeout(bftPreCommitTimeout);

        Optional<String> blockHash = validateVotes.anyApproved();
        Vote vote = blockHash.map(bytes -> Vote.newApprove(VoteType.PRECOMMIT, height, view, bytes, CommonUtil.coinbase.toAddress()))
                .orElseGet(() -> Vote.newReject(VoteType.PRECOMMIT, height, view, CommonUtil.coinbase.toAddress()));
        vote.sign(CommonUtil.coinbase);

        precommitVotes.addVote(vote);
        VoteMessage msg = new VoteMessage(vote);
        broadcastMessage(msg);
    }

    public void enterCommit() {
        state = State.COMMIT;
        logger.info("Entered commit: proposal = {}, votes = {} {} {}", proposal != null, validateVotes, precommitVotes,
                commitVotes);

        resetTimeout(bftCommitTimeout);

        Optional<String> blockHash = precommitVotes.anyApproved();
        if (!blockHash.isPresent()) {
            logger.error("Entered COMMIT state without +2/3 pre-commit votes");
        } else {
            Vote vote = Vote.newApprove(VoteType.COMMIT, height, view, blockHash.get(), CommonUtil.coinbase.toAddress());
            vote.sign(CommonUtil.coinbase);

            commitVotes.addVote(vote);
            VoteMessage msg = new VoteMessage(vote);
            broadcastMessage(msg);
        }
    }

    public void enterFinalize() {
        if (state == State.FINALIZE) {
            return;
        }

        state = State.FINALIZE;
        logger.info("Entered finalize: proposal = {}, votes = {} {} {}", proposal != null, validateVotes,
                precommitVotes, commitVotes);

        resetTimeout(bftFinalizeTimeout);

        Optional<String> blockHash = precommitVotes.anyApproved();
        logger.info("blockhash {} validBlocks {}", blockHash, validBlocks.getIfPresent(blockHash.orElse("")));
        if (blockHash.isPresent()) {
            List<Sign.SignatureData> votes = new ArrayList<>();
            for (Vote vote : precommitVotes.getApprovals(blockHash.orElse(""))) {
                votes.add(vote.getSignature());
            }

            logger.info("Generate A Block ");
        } else {
            sync(height + 1);
        }

    }

    protected void jumpToView(int view, Proof proof, Proposal proposal) {
        this.view = view;
        this.proof = proof;
        this.proposal = proposal;
        clearVotes();
        clearTimerAndEvents();

        enterPropose();
    }

    /**
     * Synchronization will be started if the 2/3th active validator's height
     * (sorted by latest block number) is greater than local height. This avoids a
     * vulnerability that malicious validators might announce an extremely large
     * height in order to hang sync process of peers.
     *
     * @param newHeight new height
     */
    protected void onNewHeight(long newHeight) {
        if (newHeight > height && state != State.FINALIZE) {
            activeValidators = channelManager.getActiveChannels();

            int[] heights = activeValidators.stream()
                    .mapToInt(c -> c.getRemotePeer().getLatestBlockNumber() + 1)
                    .sorted()
                    .toArray();

            if (heights.length != 0) {
                int q = (int) Math.ceil(heights.length * 2.0 / 3.0);
                int h = heights[heights.length - q];

                if (h > height) {
                    sync(h);
                }
            }
        }
    }

    protected void onNewView(Proof p) {
        logger.info("On new_view: {}", p);

        if (p.getHeight() == height
                && p.getView() > view && state != State.COMMIT && state != State.FINALIZE) {// larger view

            VoteSet vs = new VoteSet(VoteType.PRECOMMIT, p.getHeight(), p.getView() - 1, validators);
            vs.addVotes(p.getVotes());
            if (!vs.isRejected()) {
                return;
            }

            logger.info("Switching view because of NEW_VIEW message: {}", p.getView());
            jumpToView(p.getView(), p, null);
        }
    }

    protected void onProposal(Proposal p) {
        logger.info("On proposal: {}", p);

        if (p.getHeight() == height
                && (p.getView() == view && proposal == null && (state == State.NEW_HEIGHT || state == State.PROPOSE)
                || p.getView() > view && state != State.COMMIT && state != State.FINALIZE)
        ) {

            if (p.getView() != 0) {
                VoteSet vs = new VoteSet(VoteType.PRECOMMIT, p.getHeight(), p.getView() - 1, validators);
                vs.addVotes(p.getProof().getVotes());
                if (!vs.isRejected()) {
                    return;
                }
            } else if (!p.getProof().getVotes().isEmpty()) {
                return;
            }
            logger.info("Proposal accepted: height = {}, view = {}", p.getHeight(), p.getView());

            ProposalMessage msg = new ProposalMessage(p);
            broadcastMessage(msg);

            if (view == p.getView()) {
                proposal = p;
            } else {
                logger.info("Switching view because of PROPOSE message");
                jumpToView(p.getView(), p.getProof(), p);
            }
        }
    }

    protected void onVote(Vote v) {
        logger.info("On vote: {}", v);

        if (v.getHeight() == height
                && v.getView() == view
                && v.validate()) {
            boolean added = false;

            switch (v.getType()) {
                case VALIDATE:
                    added = validateVotes.addVote(v);
                    break;
                case PRECOMMIT:
                    added = precommitVotes.addVote(v);
                    break;
                case COMMIT:
                    added = commitVotes.addVote(v);
                    if (commitVotes.anyApproved().isPresent()) {
                        enterFinalize();
                    }
                    break;
            }

            if (added) {
                VoteMessage msg = new VoteMessage(v);
                broadcastMessage(msg);
            }
        }
    }

    public void onTimeout() {
        switch (state) {
            case NEW_HEIGHT:
                enterPropose();
                break;
            case PROPOSE:
                enterValidate();
                break;
            case VALIDATE:
                enterPreCommit();
                break;
            case PRE_COMMIT:
                if (precommitVotes.anyApproved().isPresent()) {
                    enterCommit();
                } else {
                    enterPropose();
                }
                break;
            case COMMIT:
                enterFinalize();
                break;
            case FINALIZE:
                enterNewHeight();
                break;
            default:
                logger.info("unknown state " + state);
                break;
        }
    }

    public void onMessage(Channel channel, Message msg) {
        switch (msg.getCode()) {
            case BFT_NEW_HEIGHT: {
                NewHeightMessage m = (NewHeightMessage) msg;

                channel.getRemotePeer().setLatestBlockNumber(m.getHeight() - 1);

                if (m.getHeight() > height) {
                    events.add(new Event(Event.Type.NEW_HEIGHT, m.getHeight()));
                }
                break;
            }
            case BFT_NEW_VIEW: {
                NewViewMessage m = (NewViewMessage) msg;

                channel.getRemotePeer().setLatestBlockNumber(m.getHeight() - 1);

                if (m.getHeight() > height) {
                    events.add(new Event(Event.Type.NEW_HEIGHT, m.getHeight()));
                } else if (m.getHeight() == height) {
                    events.add(new Event(Event.Type.NEW_VIEW, m.getProof()));
                }
                break;
            }
            case BFT_PROPOSAL: {
                ProposalMessage m = (ProposalMessage) msg;
                Proposal p = m.getProposal();

                if (p.getHeight() == height) {
                    if (p.validate()) {
                        events.add(new Event(Event.Type.PROPOSAL, m.getProposal()));
                    } else {
                        logger.info("Invalid proposal from {}", channel.getRemotePeer().getPeerId());
                        channel.getMessageQueue().disconnect(ReasonCode.BAD_PEER);
                    }
                }
                break;
            }
            case BFT_VOTE: {
                VoteMessage m = (VoteMessage) msg;
                Vote vote = m.getVote();

                if (vote.getHeight() == height) {
                    if (vote.revalidate()) {
                        events.add(new Event(Event.Type.VOTE, vote));
                    } else {
                        logger.info("Invalid vote from {}", channel.getRemotePeer().getPeerId());
                        channel.getMessageQueue().disconnect(ReasonCode.BAD_PEER);
                    }
                }
                break;
            }
            default: {
                logger.info("invalid msg code : {}", msg.getCode());
                break;
            }
        }
    }


    /**
     * Reset all vote sets. This should be invoked whenever height or view changes.
     */
    protected void clearVotes() {
        validateVotes.updateHeightViewValidators(height, view, validators);
        precommitVotes.updateHeightViewValidators(height, view, validators);
        commitVotes.updateHeightViewValidators(height, view, validators);
    }

    protected boolean isPrimary() {
        return validators.get((int) height % validators.size()).equalsIgnoreCase(CommonUtil.p2pMyIp().orElse(SystemUtil.getIp()));
    }

    /**
     * Check if a block proposal is valid.
     */
    protected boolean validateBlockProposal(Block block, List<TTransaction> transactions) {
        logger.info("validateBlockProposal block.getHash().toString() {} block {}", block.getHash().toString(), block);
        validBlocks.put(String.valueOf(block.getHash().toString()), block);
        return true;
    }

    /**
     * Pause the bft manager, and do synchronization.
     */
    protected void sync(int target) {
        clearVotes();
        clearTimerAndEvents();

        syncMgr.start(target);
        height = target;

        enterNewHeight();

    }

    /**
     * Update the validator sets.
     */
    protected void updateValidators() {
        int maxValidators = CommonUtil.MAXVALIDATORS;
        List<InetSocketAddress> activeAddresses = unionNodeService.listAllUnionNode();

        if (activeAddresses.size() > 0) {
            for (InetSocketAddress inetSocketAddress : activeAddresses) {
                String addr = inetSocketAddress.getAddress().toString().replace("/", "");
                if (!validators.contains(addr))
                    validators.add(addr);
            }
        }

        if (validators.size() > maxValidators) {
            validators = validators.subList(0, maxValidators);
        }

        activeValidators = channelManager.getActiveChannels();
        lastUpdate = TimeUtil.currentTimeMillis();
    }

    /**
     * Reset timer and events.
     */
    protected void clearTimerAndEvents() {
        clearTimeout();
        events.clear();
    }

    @Async("BftTaskExecutor")
    public void runBftBroadcaster() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<Channel> channels = activeValidators;
                if (channels.isEmpty()) {
                    activeValidators = channelManager.getActiveChannels();
                    continue;
                } else {
                    Message msg = queue.take();

                    for (Channel c : activeValidators) {
                        c.getMessageQueue().sendMessage(msg);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void broadcastMessage(Message msg) {
        if (!queue.offer(msg)) {
            logger.error("Failed to add a message to the broadcast queue: msg = {}", msg);
        }
    }

    @Async("BftTaskExecutor")
    public void runBftTimer() {
        while (!Thread.currentThread().isInterrupted()) {

            synchronized (this) {
                if (timeout != -1 && timeout < TimeUtil.currentTimeMillis()) {
                    events.add(new Event(Event.Type.TIMEOUT));
                    timeout = -1;
                    continue;
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

    }

    public void resetTimeout(long milliseconds) {
        if (milliseconds < 0) {
            throw new IllegalArgumentException("Timeout can not be negative");
        }
        timeout = TimeUtil.currentTimeMillis() + milliseconds;
    }

    public void clearTimeout() {
        timeout = -1;
    }

    /**
     * Main loop that processes all the BFT events.
     */
    @Async("BftTaskExecutor")
    public void runBftEventLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Event ev = events.take();

                if (lastUpdate + 2 * 60 * 1000L < TimeUtil.currentTimeMillis()) {
                    updateValidators();
                }

                switch (ev.getType()) {
                    case TIMEOUT:
                        onTimeout();
                        break;
                    case NEW_HEIGHT:
                        onNewHeight(ev.getData());
                        break;
                    case NEW_VIEW:
                        onNewView(ev.getData());
                        break;
                    case PROPOSAL:
                        onProposal(ev.getData());
                        break;
                    case VOTE:
                        onVote(ev.getData());
                        break;
                    default:
                        break;
                }

            } catch (InterruptedException e) {
                logger.info("BftEventLoop got interrupted");
                Thread.currentThread().interrupt();
                break;

            } catch (Exception e) {
                logger.warn("Unexpected exception in BftEventLoop", e);
            }
        }
    }

    public static class Event {
        public enum Type {
            /**
             * Received a timeout signal.
             */
            TIMEOUT,

            /**
             * Received a new height message.
             */
            NEW_HEIGHT,

            /**
             * Received a new view message.
             */
            NEW_VIEW,

            /**
             * Received a proposal message.
             */
            PROPOSAL,

            /**
             * Received a vote message.
             */
            VOTE
        }

        private final Type type;
        private final Object data;

        public Event(Type type) {
            this(type, null);
        }

        public Event(Type type, Object data) {
            this.type = type;
            this.data = data;
        }

        public Type getType() {
            return type;
        }

        public <T> T getData() {
            return (T) data;
        }

        @Override
        public String toString() {
            return "Event [type=" + type + ", data=" + data + "]";
        }
    }

    /**
     * Create a block for BFT proposal.
     *
     * @return the proposed block
     */
    protected Block proposeBlock() {
        long t1 = TimeUtil.currentTimeMillis();

        int number = height;
        long timestamp = TimeUtil.currentTimeMillis();
        byte[] prevHash = "testprevHash".getBytes();
        BlockHeader tempHeader = new BlockHeader(height, CommonUtil.coinbase.toAddress(), prevHash, timestamp);

        final List<PendingManager.PendingTransaction> pendingTxs = pendingManager
                .getPendingTransactions(CommonUtil.poolBlockGasLimit);
        final List<TTransaction> includedTxs = new ArrayList<>();
        final List<Transaction> newTxs = new ArrayList<>();

        long remainingBlockGas = CommonUtil.poolBlockGasLimit;
        long gasUsedInBlock = 0;
        logger.info("pendingTxs size {} {}", pendingTxs.size(), pendingTxs);
        for (PendingManager.PendingTransaction pendingTx : pendingTxs) {
            Transaction ptx = pendingTx.transaction;

            long gas = CommonUtil.nonVMTransactionGasCost;
            if (gas > remainingBlockGas) {
                break;
            }

            newTxs.add(ptx);

            long gasUsed = CommonUtil.nonVMTransactionGasCost;
            remainingBlockGas -= gasUsed;
            gasUsedInBlock += gasUsed;
        }

        BlockHeader header = new BlockHeader(number, CommonUtil.coinbase.toAddress(), prevHash, timestamp);
        Block block = new Block(header, includedTxs, newTxs);

        long t2 = TimeUtil.currentTimeMillis();
        logger.debug("Block creation: # txs = {}, time = {} ms", includedTxs.size(), t2 - t1);

        return block;
    }

}
