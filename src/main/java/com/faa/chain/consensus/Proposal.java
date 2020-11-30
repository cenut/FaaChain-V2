/**
 * Copyright (c) 2019 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.faa.chain.consensus;

import com.faa.chain.core.Block;
import com.faa.chain.crypto.Key;
import com.faa.chain.crypto.Sign;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;
import com.faa.chain.node.Transaction;
import com.faa.entity.TTransaction;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Proposal {

    private final Proof proof;
    private Block block;
    private List<TTransaction> transactions;

    private final byte[] encoded;
    private Sign.SignatureData signature;

    public Proposal(Proof proof) {
        this.proof = proof;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(proof.toBytes());
        this.encoded = enc.toBytes();
    }

    public Proposal(Proof proof, Block block, List<TTransaction> transactions) {
        this.proof = proof;
        this.block = block;
        this.transactions = transactions;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(proof.toBytes());
        enc.writeBytes(block.toBytes());
        enc.writeInt(transactions.size());
        for (TTransaction tx : transactions) {
            enc.writeBytes(tx.toBytes());
        }
        this.encoded = enc.toBytes();
    }

    public Proposal(byte[] encoded, byte[] signature) {
        SimpleDecoder dec = new SimpleDecoder(encoded);
        this.proof = Proof.fromBytes(dec.readBytes());
        this.block = block.fromBytes(dec.readBytes());
        this.transactions = new ArrayList<>();
        int n = dec.readInt();
        for (int i = 0; i < n; i++) {
            transactions.add(TTransaction.fromBytes(dec.readBytes()));
        }

        this.encoded = encoded;
        this.signature = Sign.SignatureData.fromBytes(signature);
    }

    /**
     * Sign this proposal.
     * 
     * @param key
     * @return
     */
    public Proposal sign(Key key) {
        this.signature = key.sign(encoded);
        return this;
    }

    public boolean validate() {
        return true;
    }

    public Proof getProof() {
        return proof;
    }

    public long getHeight() {
        return proof.getHeight();
    }

    public int getView() {
        return proof.getView();
    }

    public Block getBlock() {
        return block;
    }

    public List<TTransaction> getTransactions() {
        return transactions;
    }

    public Sign.SignatureData getSignature() {
        return signature;
    }

    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(encoded);
        enc.writeBytes(signature.toBytes());

        return enc.toBytes();
    }

    public static Proposal fromBytes(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        byte[] encoded = dec.readBytes();
        byte[] signature = dec.readBytes();

        return new Proposal(encoded, signature);
    }

    @Override
    public String toString() {
        return "Proposal [height=" + getHeight() + ", view = " + getView() + ", # proof votes = "
                + proof.getVotes().size() + ", # txs = " + transactions.size() + "]";
    }
}
