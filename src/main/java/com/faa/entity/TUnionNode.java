package com.faa.entity;

import com.alibaba.fastjson.annotation.JSONField;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name="unionnode")
public class TUnionNode {

    @JSONField(serialize=false)
    @Id
    @GeneratedValue
    private int id;
    @Column(name="node_name")
    private String nodeName;
    @Column(name="node_info")
    private String nodeInfo;
    @Column(name="node_ip")
    private String nodeIp;
    @Column(name="node_port")
    private String nodePort;
    @Column(name="node_addr")
    private String nodeAddr;
    @Column(name="reward_block")
    private String rewardBlock;
    @Column(name="reward_fee")
    private String rewardFee;
    @Column(name="date_created")
    private Timestamp dateCreated;
    private int status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(String nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public String getNodePort() {
        return nodePort;
    }

    public void setNodePort(String nodePort) {
        this.nodePort = nodePort;
    }

    public String getNodeAddr() {
        return nodeAddr;
    }

    public void setNodeAddr(String nodeAddr) {
        this.nodeAddr = nodeAddr;
    }

    public String getRewardBlock() {
        return rewardBlock;
    }

    public void setRewardBlock(String rewardBlock) {
        this.rewardBlock = rewardBlock;
    }

    public String getRewardFee() {
        return rewardFee;
    }

    public void setRewardFee(String rewardFee) {
        this.rewardFee = rewardFee;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
