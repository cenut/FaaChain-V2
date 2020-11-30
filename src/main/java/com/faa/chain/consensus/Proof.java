/**
 * Copyright (c) 2019 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.faa.chain.consensus;

import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Proof {
    private final int height;
    private final int view;
    private final List<Vote> votes;

    public Proof(int height, int view, List<Vote> votes) {
        this.height = height;
        this.view = view;
        this.votes = votes;
    }

    public Proof(int height, int view) {
        this(height, view, Collections.emptyList());
    }

    public int getHeight() {
        return height;
    }

    public int getView() {
        return view;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeInt(height);
        enc.writeInt(view);
        enc.writeInt(votes.size());
        for (Vote v : votes) {
            enc.writeBytes(v.toBytes());
        }
        return enc.toBytes();
    }

    public static Proof fromBytes(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        int height = dec.readInt();
        int view = dec.readInt();
        List<Vote> votes = new ArrayList<>();
        int n = dec.readInt();
        for (int i = 0; i < n; i++) {
            Vote newvote = Vote.fromBytes(dec.readBytes());
            votes.add(newvote);
        }

        return new Proof(height, view, votes);
    }

    @Override
    public String toString() {
        return "Proof [height=" + height + ", view=" + view + ", # votes=" + votes.size() + "]";
    }
}