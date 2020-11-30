package com.faa.entity;

import com.faa.chain.node.Transaction;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name="block")
public class TBlock {

    @Id
    @GeneratedValue
    private Integer id;
    @Column(name="block_no")
    private Integer blockNo;
    private String hash;
    private String prehash;
    @Column(name="tran_count")
    private Integer tranCount;
    private String winner;
    private String reward;
    private String fee;
    @Column(name="date_created")
    private Timestamp dateCreated;
    private Boolean enable;

    @Transient
    private List<Transaction> transactions;
    @Transient
    private double rewardHuman;
    @Transient
    private double feeHuman;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBlockNo() {
        return blockNo;
    }

    public void setBlockNo(Integer blockNo) {
        this.blockNo = blockNo;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPrehash() {
        return prehash;
    }

    public void setPrehash(String prehash) {
        this.prehash = prehash;
    }

    public Integer getTranCount() {
        return tranCount;
    }

    public void setTranCount(Integer tranCount) {
        this.tranCount = tranCount;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public double getRewardHuman() {
        return rewardHuman;
    }

    public void setRewardHuman(double rewardHuman) {
        this.rewardHuman = rewardHuman;
    }

    public double getFeeHuman() {
        return feeHuman;
    }

    public void setFeeHuman(double feeHuman) {
        this.feeHuman = feeHuman;
    }
}
