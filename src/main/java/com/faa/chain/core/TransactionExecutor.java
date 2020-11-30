/**
 * Copyright (c) 2019 The Alienchain Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.faa.chain.core;

import com.faa.chain.node.Transaction;
import com.faa.chain.utils.Bytes;
import com.faa.chain.core.TransactionResult.Code;
import com.faa.utils.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Transaction executor
 */
public class TransactionExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TransactionExecutor.class);
    private static final boolean[] delegateNameAllowedChars = new boolean[256];

    static {
        for (byte b : Bytes.of("abcdefghijklmnopqrstuvwxyz0123456789_")) {
            delegateNameAllowedChars[b & 0xff] = true;
        }
    }

    private boolean isVMEnabled;
    private boolean isVotingPrecompiledUpgraded;


    /**
     * Creates a new transaction executor.
     *
     */
    public TransactionExecutor(boolean isVMEnabled,
                               boolean isVotingPrecompiledUpgraded) {
        this.isVMEnabled = isVMEnabled;
        this.isVotingPrecompiledUpgraded = isVotingPrecompiledUpgraded;
    }

    public int maxTransactionDataSize(TransactionType type) {
        switch (type) {
            case COINBASE:
            case UNVOTE:
            case VOTE:
                return 0;

            case TRANSFER:
                return 128;

            case DELEGATE:
                return 16;

            case CREATE:
            case CALL:
                return 512 * 1024;

            default:
                logger.info("unknown state");
                return 0;
        }
    }

    /**
     * Execute a list of transactions.
     *
     * NOTE: transaction format and signature are assumed to be success.
     *
     * @param txs
     *            transactions
     * @param block
     *            the block context
     * @param gasUsedInBlock
     *            the BigInteger of gas that has been consumed by previous transaction
     *            in the block
     * @return
     */
    public List<TransactionResult> execute(List<Transaction> txs, Block block, long gasUsedInBlock) {
        List<TransactionResult> results = new ArrayList<>();

        for (Transaction tx : txs) {
            TransactionResult result = new TransactionResult();
            results.add(result);

            TransactionType type = tx.getType();
            String from = tx.getFrom();
            String to = tx.getTo();
            BigInteger value = tx.getValue();
            BigInteger fee = tx.getFee();
            String data = tx.getData();

            if (fee.compareTo(CommonUtil.minTransactionFee) == -1) {
                logger.debug("not vm transaction fee check. fee: {}, block minTransactionFee: {}", fee, CommonUtil.minTransactionFee);
                result.setCode(Code.INVALID_FEE);
                continue;
            }

            if (result.getCode().isAcceptable()) {

                gasUsedInBlock += CommonUtil.nonVMTransactionGasCost;
            }


            result.setBlockNumber(block.getNumber());
        }

        return results;
    }

    /**
     * Execute one transaction.
     *
     * NOTE: transaction format and signature are assumed to be success.
     *
     * @param block
     *            the block context
     * @param gasUsedInBlock
     *            the BigInteger of gas that has been consumed by previous transaction
     *            in the block
     * @return
     */
    public TransactionResult execute(Transaction tx, Block block, long gasUsedInBlock) {
        return execute(Collections.singletonList(tx), block, gasUsedInBlock).get(0);
    }
}
