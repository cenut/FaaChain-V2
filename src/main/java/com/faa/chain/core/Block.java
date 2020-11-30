/**
 * Copyright (c) 2019 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.faa.chain.core;

import com.faa.chain.crypto.Sign;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;
import com.faa.chain.node.Transaction;
import com.faa.entity.TTransaction;
import com.faa.utils.utilswt.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a block in the blockchain.
 */
public class Block {

    static final Logger logger = LoggerFactory.getLogger(Block.class);

    /**
     * The block header.
     */
    private final BlockHeader header;

    /**
     * The transactions.
     */
    private final List<TTransaction> transactions;
    private List<Transaction> txs;

    /**
     * The BFT view and votes.
     */
    private int view;
    private List<Sign.SignatureData> votes;

    public Block(BlockHeader header, List<TTransaction> transactions, List<Transaction> txs, int view,
                 List<Sign.SignatureData> votes) {
        this.header = header;
        this.transactions = transactions;
        this.txs = txs;
        this.view = view;
        this.votes = votes;
    }

    public Block(BlockHeader header, List<TTransaction> transactions, List<Transaction> txs) {
        this(header, transactions, txs, 0, new ArrayList<>());
    }

    public void setView(int view) {
        this.view = view;
    }

    public void setVotes(List<Sign.SignatureData> votes) {
        this.votes = votes;
    }

    /**
     * Validates block header.
     *
     * @param header
     * @param parentHeader
     * @return
     */
    public boolean validateHeader(BlockHeader header, BlockHeader parentHeader) {
        if (header == null) {
            logger.warn("Header was null.");
            return false;
        }

        if (!header.validate()) {
            logger.warn("Header was invalid.");
            return false;
        }

        if (header.getNumber() != parentHeader.getNumber() + 1) {
            logger.warn("Header number was not one greater than previous block.");
            return false;
        }

        if (!Arrays.equals(header.getParentHash(), parentHeader.getHash())) {
            logger.warn("Header parent hash was not equal to previous block hash.");
            return false;
        }

        if (header.getTimestamp() <= parentHeader.getTimestamp()) {
            logger.warn("Header timestamp was before previous block.");
            return false;
        }

        return true;
    }

    /**
     * Validates transactions in parallel.
     *
     * @param header
     * @param transactions
     * @return
     */
    public boolean validateTransactions(BlockHeader header, List<TTransaction> transactions) {
        return validateTransactions(header, transactions, transactions);
    }

    /**
     * Validates transactions in parallel, only doing those that have not already
     * been calculated.
     *
     * @param header
     *            block header
     * @param unvalidatedTransactions
     *            transactions needing validating
     * @param allTransactions
     *            all transactions within the block
     * @return
     */
    public boolean validateTransactions(BlockHeader header, Collection<TTransaction> unvalidatedTransactions,
            List<TTransaction> allTransactions) {
        return true;
    }

    /**
     * Returns a shallow copy of the block header.
     *
     * @return
     */
    public BlockHeader getHeader() {
        return header;
    }

    /**
     * Returns a shallow copy of the transactions.
     *
     * @return
     */
    public List<TTransaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    /**
     * Returns the BFT view.
     *
     * @return
     */
    public int getView() {
        return view;
    }

    /**
     * Returns a shallow copy of the votes.
     *
     * @return
     */
    public List<Sign.SignatureData> getVotes() {
        return new ArrayList<>(votes);
    }

    /**
     * Returns the block hash.
     *
     * @return
     */
    public byte[] getHash() {
        return header.getHash();
    }

    public String getHashString() {
        return Base64.encode(header.getHash());
    }

    /**
     * Returns the block number.
     *
     * @return
     */
    public int getNumber() {
        return header.getNumber();
    }

    /**
     * Returns the coinbase
     *
     * @return
     */
    public String getCoinbase() {
        return header.getCoinbase();
    }

    /**
     * Returns the hash of the parent block
     *
     * @return
     */
    public byte[] getParentHash() {
        return header.getParentHash();
    }

    public String getParentHashString() {
        return Base64.encode(header.getParentHash());
    }

    /**
     * Returns the block timestamp.
     *
     * @return
     */
    public long getTimestamp() {
        return header.getTimestamp();
    }

    /**
     * Serializes the block header into byte array.
     *
     * @return
     */
    public byte[] getEncodedHeader() {
        return header.toBytes();
    }

    /**
     * Serializes the block transactions into byte array.
     *
     * @return
     */
    public byte[] getEncodedTransactions() {
        return getEncodedTransactionsAndIndices().getLeft();
    }

    public Pair<byte[], List<Integer>> getEncodedTransactionsAndIndices() {
        List<Integer> indices = new ArrayList<>();

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeInt(transactions.size());
        for (TTransaction transaction : transactions) {
            int index = enc.getWriteIndex();
            enc.writeBytes(transaction.toBytes());
            indices.add(index);
        }

        return Pair.of(enc.toBytes(), indices);
    }

    /**
     * Serializes the BFT votes into byte array.
     *
     * @return
     */
    public byte[] getEncodedVotes() {
        SimpleEncoder enc = new SimpleEncoder(4 + 4 + votes.size() * Sign.SignatureData.LENGTH);

        enc.writeInt(view);
        enc.writeInt(votes.size());
        for (Sign.SignatureData vote : votes) {
            enc.writeBytes(vote.toBytes());
        }

        return enc.toBytes();
    }

    /**
     * Parses a block instance from bytes.
     *
     * @param h
     *            Serialized header
     * @param t
     *            Serialized transactions
     * @param v
     *            Serialized votes
     * @return
     */
    public static Block fromComponents(byte[] h, byte[] t, byte[] v) {
        if (h == null) {
            throw new IllegalArgumentException("Block header can't be null");
        }
        if (t == null) {
            throw new IllegalArgumentException("Block transactions can't be null");
        }

        BlockHeader header = BlockHeader.fromBytes(h);

        SimpleDecoder dec = new SimpleDecoder(t);
        List<TTransaction> transactions = new ArrayList<>();
        List<Transaction> txs = new ArrayList<>();
        int n = dec.readInt();
        for (int i = 0; i < n; i++) {
            transactions.add(TTransaction.fromBytes(dec.readBytes()));
        }

        int view = 0;
        List<Sign.SignatureData> votes = new ArrayList<>();
        if (v != null) {
            dec = new SimpleDecoder(v);
            view = dec.readInt();
            n = dec.readInt();
            for (int i = 0; i < n; i++) {
                votes.add(Sign.SignatureData.fromBytes(dec.readBytes()));
            }
        }

        return new Block(header, transactions, txs, view, votes);
    }

    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(getEncodedHeader());
        enc.writeBytes(getEncodedTransactions());
        enc.writeBytes(getEncodedVotes());

        return enc.toBytes();
    }

    public static Block fromBytes(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        byte[] header = dec.readBytes();
        byte[] transactions = dec.readBytes();
        byte[] votes = dec.readBytes();

        return Block.fromComponents(header, transactions, votes);
    }

    /**
     * Get block size in bytes
     *
     * @return block size in bytes
     */
    public int size() {
        return toBytes().length;
    }


    @Override
    public String toString() {
        return "Block [number = " + getNumber() + ", view = " + getView() + ", hash = " + Base64.encode(getHash())
                + ", # txs = " + transactions.size() + ", # votes = " + votes.size() + "]";
    }

}
