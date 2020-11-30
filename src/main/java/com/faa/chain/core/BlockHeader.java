/**
 * Copyright (c) 2019 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.faa.chain.core;

import com.faa.chain.crypto.Hash;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;

import java.util.Arrays;

public class BlockHeader {

    private final byte[] hash;

    private final int number;

    private final String coinbase;

    private final byte[] parentHash;

    private final long timestamp;

    private final byte[] encoded;

    public BlockHeader(int number, String coinbase, byte[] prevHash, long timestamp) {
        this.number = number;
        this.coinbase = coinbase;
        this.parentHash = prevHash;
        this.timestamp = timestamp;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(number);
        enc.writeString(coinbase);
        enc.writeBytes(prevHash);
        enc.writeLong(timestamp);
        this.encoded = enc.toBytes();
        this.hash = Hash.sha3(encoded);
    }

    /**
     * Parses block header from byte arrays.
     *
     * @param hash
     * @param encoded
     */
    public BlockHeader(byte[] hash, byte[] encoded) {
        this.hash = hash;

        SimpleDecoder dec = new SimpleDecoder(encoded);
        this.number = dec.readInt();
        this.coinbase = dec.readString();
        this.parentHash = dec.readBytes();
        this.timestamp = dec.readLong();

        this.encoded = encoded;
    }

    /**
     * Validates block header format.
     *
     * @return true if success, otherwise false
     */
    public boolean validate() {
        return hash != null
                && number >= 0
                && coinbase != null
                && parentHash != null
                && timestamp >= 0
                && encoded != null
                && Arrays.equals(Hash.sha3(encoded), hash);
    }

    public byte[] getHash() {
        return hash;
    }

    public int getNumber() {
        return number;
    }

    public String getCoinbase() {
        return coinbase;
    }

    public byte[] getParentHash() {
        return parentHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(hash);
        enc.writeBytes(encoded);
        return enc.toBytes();
    }

    public static BlockHeader fromBytes(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        byte[] hash = dec.readBytes();
        byte[] encoded = dec.readBytes();

        return new BlockHeader(hash, encoded);
    }

}
