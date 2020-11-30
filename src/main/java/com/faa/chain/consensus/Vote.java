/**
 * Copyright (c) 2019 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.faa.chain.consensus;

import com.faa.chain.crypto.Key;
import com.faa.chain.crypto.Keys;
import com.faa.chain.crypto.Sign;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;
import com.faa.chain.utils.Bytes;
import com.faa.chain.utils.Numeric;
import com.faa.chain.utils.SystemUtil;

public class Vote {
    public static final boolean VALUE_APPROVE = true;
    public static final boolean VALUE_REJECT = false;

    private final VoteType type;
    private final boolean value;

    private long height = 0;
    private int view = 0;
    private String blockHash = "";
    private String peerID = "";

    private final byte[] encoded;

    private Sign.SignatureData signatureData;
    private Boolean validated;

    public Vote(VoteType type, boolean value, long height, int view, String blockHash, String peerID) {
        this.type = type;
        this.value = value;
        this.height = height;
        this.view = view;
        this.blockHash = blockHash;
        this.peerID = peerID;
        this.validated = null;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeByte(type.toByte());
        enc.writeBoolean(value);
        enc.writeLong(height);
        enc.writeInt(view);
        enc.writeString(blockHash);
        enc.writeString(peerID);
        this.encoded = enc.toBytes();
    }

    public Vote(byte[] encoded, byte[] signatureData) {
        this.encoded = encoded;

        SimpleDecoder dec = new SimpleDecoder(encoded);
        this.type = VoteType.of(dec.readByte());
        this.value = dec.readBoolean();
        this.height = dec.readLong();
        this.view = dec.readInt();
        this.blockHash = dec.readString();
        this.peerID = dec.readString();
        this.validated = null;

        this.signatureData = Sign.SignatureData.fromBytes(signatureData);
    }

    public static Vote newApprove(VoteType type, long height, int view, String blockHash, String peerID) {
        return new Vote(type, VALUE_APPROVE, height, view, blockHash, peerID);
    }

    public static Vote newReject(VoteType type, long height, int view, String peerID) {
        return new Vote(type, VALUE_REJECT, height, view, "", peerID);
    }

    /**
     * Sign this vote.
     * 
     * @param key
     * @return
     */
    public Vote sign(Key key) {
        this.signatureData = key.sign(encoded);
        this.validated = null;
        return this;
    }

    /**
     * validate the vote format and signatureData while ignoring any cached validation
     * value.
     * 
     * @return
     */
    public boolean revalidate() {
        System.out.println("Debug revalidate : height " + height + " view " + view + " blockhash len " + blockHash.length());
        return (validated = (type != null
                && height > 0
                && view >= 0
                && blockHash != null
                && encoded != null
                && signatureData != null
                && Key.verify(encoded, signatureData)));
    }

    /**
     * validate the vote format and signatureData. if exists, a memoized validation
     * value is returned. NOTE: to force revalidation use {@link Vote#revalidate()}.
     * 
     * @return
     */
    public boolean validate() {
        return validated == null ? revalidate() : validated;
    }

    public VoteType getType() {
        return type;
    }

    public boolean getValue() {
        return value;
    }

    public long getHeight() {
        return height;
    }

    public int getView() {
        return view;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public String getPeerID() {
        return peerID;
    }

    public byte[] getEncoded() {
        return encoded;
    }

    public Sign.SignatureData getSignature() {
        return signatureData;
    }

    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(encoded);
        enc.writeBytes(signatureData.toBytes());

        return enc.toBytes();
    }

    public static Vote fromBytes(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        byte[] encoded = dec.readBytes();
        byte[] signatureData = dec.readBytes();

        return new Vote(encoded, signatureData);
    }

    @Override
    public String toString() {
        return "Vote [" + type + ", " + (value ? "approve" : "reject") + ", height=" + height + ", view=" + view + "]";
    }
}
