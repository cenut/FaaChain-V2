package com.faa.entity;

import javax.persistence.*;

@Entity
@Table(name="properties")
public class Properties {

    @Id
    @GeneratedValue
    private int id;
    @Column(name="block_scand")
    private String blockScand;
    private int nonceNow;
    private boolean enable;

    @Override
    public String toString() {
        return "Properties{" +
                "id=" + id +
                ", blockScand='" + blockScand + '\'' +
                ", nonceNow=" + nonceNow +
                ", enable=" + enable +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBlockScand() {
        return blockScand;
    }

    public void setBlockScand(String blockScand) {
        this.blockScand = blockScand;
    }

    public int getNonceNow() {
        return nonceNow;
    }

    public void setNonceNow(int nonceNow) {
        this.nonceNow = nonceNow;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
