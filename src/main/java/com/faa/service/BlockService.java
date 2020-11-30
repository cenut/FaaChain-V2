package com.faa.service;

import com.faa.chain.consensus.Vote;
import com.faa.chain.consensus.VoteType;
import com.faa.chain.core.Block;
import com.faa.chain.core.BlockHeader;
import com.faa.chain.node.Transaction;
import com.faa.chain.crypto.Key;
import com.faa.chain.utils.ByteArray;
import com.faa.entity.TBalance;
import com.faa.entity.TBlock;
import com.faa.entity.TTransaction;
import com.faa.mapper.BalanceRepository;
import com.faa.mapper.BlockRepository;
import com.faa.mapper.TransactionRepository;
import com.faa.utils.CommonUtil;
import com.faa.chain.crypto.Hash;
import com.faa.chain.token.CoinsBaseUnits;
import com.faa.chain.token.FaaMain;
import com.faa.chain.utils.Numeric;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;


@Service
public class BlockService {

    private static final Logger logger = LoggerFactory.getLogger(BlockService.class);

    @Resource
    private TransactionRepository transactionRepository;
    @Resource
    private BlockRepository blockRepository;
    @Resource
    private BalanceRepository balanceRepository;
    @Resource
    private UnionNodeService unionNodeService;

    private final ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();

    // 生成新区块
    public void checkAndCreateNewBlock() throws Exception{
        String hexTransaction = "";
        BigInteger feeTotal = new BigInteger("0");
        int transactionCount = 0;

        List<TTransaction> tranList = transactionRepository.findAllByIdBlockAndStatusOrderByDateCreatedAsc(-1, 0);

        if(tranList != null && tranList.size() > 0){
            // 处理每笔交易 判断余额 设置成功失败等
            for (TTransaction t : tranList) {
                TBalance tbFrom = balanceRepository.findOneByAddress(t.getFromAddress());
                TBalance tbTo = balanceRepository.findOneByAddress(t.getToAddress());
                // 已存在数据库的from地址
                if(tbFrom != null){
                    BigInteger balance = new BigInteger(tbFrom.getBalance());
                    BigInteger willOut = new BigInteger(t.getValue()).add(new BigInteger(t.getFee()));
                    // 余额足够 进行转账
                    if(balance.compareTo(willOut) != -1){
                        balance = balance.subtract(willOut);
                        tbFrom.setBalance(balance.toString());
                        tbFrom.syncBalancef();

                        if(tbTo == null){
                            tbTo = new TBalance();
                            tbTo.setAddress(t.getToAddress());
                            tbTo.setBalance("0");
                            tbTo.setBalancef(new BigDecimal("0"));
                            tbTo.setStatus(1);
                            tbTo.setEnable(true);
                        }
                        BigInteger balanceTo = new BigInteger(tbTo.getBalance()).add(new BigInteger(t.getValue()));
                        tbTo.setBalance(balanceTo.toString());
                        tbTo.syncBalancef();

                        balanceRepository.save(tbFrom);
                        balanceRepository.save(tbTo);
                        t.setNode(CommonUtil.MINER_NODE);
                        t.setStatus(9);

                        hexTransaction = hexTransaction + t.getHash();
                        feeTotal = feeTotal.add(new BigInteger(t.getFee()));
                        transactionCount += 1;
                    }
                    else{
                        t.setStatus(-1); // 余额不足 交易失败
                    }
                }
                else{
                    t.setStatus(-1); // 地址不存在 交易失败
                }
            }
        }

        // 生成preHash
        TBlock lastBlock = blockRepository.findLastBlock();
        String preHash = "";
        int blockNo;
        if(lastBlock == null){    // 创世块
            preHash = "-1";
            blockNo = 0;
        }else{
            preHash = lastBlock.getHash();
            blockNo = lastBlock.getBlockNo() + 1;
        }

        // 爆块奖励
        FaaMain coinType =  FaaMain.get();
        BigInteger reward = CoinsBaseUnits.toBaseUnit(comReward() + "", coinType).toBigInteger();

//        Random r = new Random();
//        int number = r.nextInt(tranList.size());
//        String winner = tranList.get(number).getNode();
        String winner = CommonUtil.MINER_NODE;

        // 生成hash
        long now = System.currentTimeMillis();  //使用系统时间作为种子
        if(CommonUtil.USED_TIMEMILLIS == now){
            now += 1;
        }
        byte[] salt = new byte[64];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(now);
        secureRandom.nextBytes(salt);
        String hexHash = Hash.sha3(hexTransaction + Numeric.toHexString(salt)).toLowerCase();
        String hash = Numeric.prependFaaBlockPrefix(Numeric.cleanHexPrefix(hexHash)).toLowerCase();
        CommonUtil.USED_TIMEMILLIS = now;

        // 新增区块
        TBlock newBlock = addNewBlock(blockNo, hash, preHash, transactionCount, reward.toString(), feeTotal.toString(), winner);

        // 给每笔成功的交易绑定区块号
        if(tranList != null && tranList.size() > 0) {
            for (TTransaction t : tranList) {
                if (t.getStatus() == 9) {
                    t.setIdBlock(newBlock.getId());
                    t.setBlockNo(newBlock.getBlockNo());
                }
            }
            transactionRepository.save(tranList);
        }

        // 发放爆块奖励
        TBalance tbWin = balanceRepository.findOneByAddress(newBlock.getWinner());
        if(tbWin == null){
            tbWin = new TBalance();
            tbWin.setAddress(winner);
            tbWin.setBalance("0");
            tbWin.setBalancef(new BigDecimal("0"));
            tbWin.setStatus(1);
            tbWin.setEnable(true);
        }
        BigInteger balanceWin = new BigInteger(tbWin.getBalance()).add(reward);
        tbWin.setBalance(balanceWin.toString());
        tbWin.syncBalancef();
        balanceRepository.save(tbWin);
    }

    // 新增一个区块
    public TBlock addNewBlock(int blockNo, String hash, String preHash, int tranCount, String reward, String fee, String winner) throws Exception{
        TBlock tb = new TBlock();

        tb.setBlockNo(blockNo);
        tb.setHash(hash);
        tb.setPrehash(preHash);
        tb.setTranCount(tranCount);
        tb.setWinner(winner);
        tb.setReward(reward);
        tb.setFee(fee);
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        tb.setDateCreated(ts);
        tb.setEnable(true);

        return blockRepository.saveAndFlush(tb);
    }

    // 计算当前爆块奖励
    public double comReward(){
        double faaAll = sumAllFaa().doubleValue();

        // 正常挖矿阶段
        if(faaAll >= CommonUtil.FAA_PRE){
            double nowDigOut = faaAll - CommonUtil.FAA_PRE;
            for (int i = 0; i < CommonUtil.REWARD_ROAD_A.length; i++) {
                if(nowDigOut < CommonUtil.REWARD_ROAD_A[i]){
                    return CommonUtil.REWARD_ROAD_B[i];
                }
            }
            // 已全部挖出
            return 0;
        }
        // 预挖阶段爆块奖励1000枚
        else{
            return 1000;
        }
    }

    // 查询最新的10个区块并换算单位
    public List<TBlock> findLast10Block(){
        List<TBlock> blockList = blockRepository.findLast10Block();
        for (TBlock tb : blockList) {
            BigDecimal rewardHuman = CoinsBaseUnits.toHumanUnit(tb.getReward(), FaaMain.get());
            BigDecimal feeHuman = CoinsBaseUnits.toHumanUnit(tb.getFee(), FaaMain.get());
            tb.setRewardHuman(rewardHuman.doubleValue());
            tb.setFeeHuman(feeHuman.doubleValue());
        }
        return blockList;
    }

    // 统计Faa总数
    public BigDecimal sumAllFaa(){
        Object obj = balanceRepository.sumAllFaaBalance();;
        if(null == obj){
            return new BigDecimal("0");
        }else{
            return (BigDecimal)obj;
        }
    }

    // 统计使用过的钱包地址总数
    public int addressCount(){
        Object obj = balanceRepository.addressCount();
        if(null == obj){
            return 0;
        }else{
            BigInteger countB = (BigInteger)obj;
            return countB.intValue();
        }
    }

    public TBlock getLatestBlock() {
        return blockRepository.findLastBlock();
    }

    public int getLatestBlockNumber() { return blockRepository.findLastBlockNumber(); }

    public Boolean hasBlock(long number) {
        return blockRepository.hasBlock(number);
    }

    public Boolean hasTransaction(String hash) {
        return blockRepository.hasTransaction(hash);
    }

    public List<String> getValidators() {
        List<String> validators = new ArrayList<>();
        int maxValidators = CommonUtil.MAXVALIDATORS;
        List<InetSocketAddress> activeAddresses = unionNodeService.listAllUnionNode();

        if(activeAddresses.size() > 0){
            for(InetSocketAddress inetSocketAddress : activeAddresses) {
                String addr = inetSocketAddress.getAddress().toString().replace("/", "");
                if(!validators.contains(addr))
                    validators.add(addr);
            }
        }
        return validators;
    }

    /**
     * Validate the block. Votes are validated only if validateVotes is true.
     *
     * @param block
     * @param validateVotes
     * @return
     */
    protected boolean validateBlock(Block block, boolean validateVotes) {
        try {
            if (validateVotes) {
                return validateBlockVotes(block);
            }

            return true;
        } catch (Exception e) {
            logger.error("Unexpected exception during block validation", e);
            return false;
        }
    }

    public boolean validateBlockVotes(Block block) {
        int maxValidators = 21;
        List<String> validatorList = this.getValidators();
        if (validatorList.size() > maxValidators) {
            validatorList = validatorList.subList(0, maxValidators);
        }
        Set<String> validators = new HashSet<>(validatorList);

        int twoThirds = (int) Math.ceil(validators.size() * 2.0 / 3.0);
        Vote vote = new Vote(VoteType.PRECOMMIT, Vote.VALUE_APPROVE, block.getNumber(), block.getView(),
                block.getHashString(), CommonUtil.coinbase.toAddress());
        byte[] encoded = vote.getEncoded();

        if (block.getVotes().stream().anyMatch(sig -> {
            try {
                return !validators.contains(sig.getAddress(encoded));
            } catch (SignatureException e) {
                logger.warn("Block votes are invalid");
               return false;
            }
        })) {
            logger.warn("Block votes are invalid");
            return false;
        }

        if (!block.getVotes().stream()
                .allMatch(sig -> Key.verify(encoded, sig))) {
            logger.warn("Block votes are invalid");
            return false;
        }

        if (block.getVotes().stream()
                .map(sig -> new ByteArray(sig.getR()))
                .collect(Collectors.toSet()).size() < twoThirds) {
            logger.warn("Not enough votes, required (2/3+) = {}, actual = {}", twoThirds, block.getVotes().size());
            return false;
        }

        return true;
    }

    public boolean importBlock(Block block, boolean validateVotes) throws Exception {
        return validateBlock(block, validateVotes) && applyBlock(block);
    }

    protected boolean applyBlock(Block block) throws Exception {
        ReentrantReadWriteLock.WriteLock writeLock = this.stateLock.writeLock();
        writeLock.lock();
        try {
            this.addBlock(block);
        } finally {
            writeLock.unlock();
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized void addBlock(Block block) throws Exception {
        BigInteger feeTotal = new BigInteger("0");

        int number = block.getNumber();
        String hash = block.getHashString();

        List<TTransaction> txs = block.getTransactions();
        FaaMain coinType =  FaaMain.get();
        BigInteger reward = CoinsBaseUnits.toBaseUnit(comReward() + "", coinType).toBigInteger();

        TBlock newBlock = addNewBlock(number, hash, block.getParentHashString(), txs.size(), reward.toString(), feeTotal.toString(), block.getCoinbase());

        for (int i = 0; i < txs.size(); i++) {
            TTransaction tx = txs.get(i);
            tx.setIdBlock(newBlock.getId());
            transactionRepository.save(tx);
        }

    }

    public Block getBlock(int number) {
        return null;
    }

    public BlockHeader getBlockHeader(long number) {
        return null;
    }
}