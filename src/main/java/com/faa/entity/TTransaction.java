package com.faa.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;

import javax.persistence.*;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;

@Entity
@Table(name="transaction")
public class TTransaction {

    @JSONField(serialize=false)
    @Id
    @GeneratedValue
    private int id;
    @JSONField(serialize=false)
    @Column(name="id_block")
    private int idBlock;
    @Column(name="block_no")
    private int blockNo;
    @Column(name="from_address")
    private String fromAddress;
    @Column(name="to_address")
    private String toAddress;
    private String value;
    private String fee;
    private String hash;
    private String hexdata;
    @JSONField(serialize=false)
    private String node;
    @Column(name="date_created")
    private Timestamp dateCreated;
    private int status;
    @JSONField(serialize=false)
    private boolean enable;

    @JSONField(serialize=false)
    @Transient
    private double valueHuman;
    @JSONField(serialize=false)
    @Transient
    private double feeHuman;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdBlock() {
        return idBlock;
    }

    public void setIdBlock(int idBlock) {
        this.idBlock = idBlock;
    }

    public int getBlockNo() {
        return blockNo;
    }

    public void setBlockNo(int blockNo) {
        this.blockNo = blockNo;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHexdata() {
        return hexdata;
    }

    public void setHexdata(String hexdata) {
        this.hexdata = hexdata;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
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

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public double getValueHuman() {
        return valueHuman;
    }

    public void setValueHuman(double valueHuman) {
        this.valueHuman = valueHuman;
    }

    public double getFeeHuman() {
        return feeHuman;
    }

    public void setFeeHuman(double feeHuman) {
        this.feeHuman = feeHuman;
    }

    /**
     * Converts into a byte array.
     *
     * @return
     */
    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeInt(blockNo);
        enc.writeString(fromAddress);
        enc.writeString(toAddress);
        enc.writeString(value);
        enc.writeString(fee);
        enc.writeString(hash);
        enc.writeString(hexdata);
        enc.writeString(node);
        enc.writeLong(dateCreated.getTime());
        enc.writeInt(status);
        enc.writeBoolean(enable);
        enc.writeString(valueHuman+"");
        enc.writeString(feeHuman+"");

        return enc.toBytes();
    }

    /**
     * Parses from a byte array.
     *
     * @param bytes
     * @return
     */
    public static TTransaction fromBytes(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        int blockNo = dec.readInt();
        String fromAddress = dec.readString();
        String toAddress = dec.readString();
        String value = dec.readString();
        String fee = dec.readString();
        String hash = dec.readString();
        String hexdata = dec.readString();
        String node = dec.readString();
        Timestamp dateCreated = new Timestamp(dec.readLong());
        int status = dec.readInt();
        Boolean enable = dec.readBoolean();
        Double valueHuman = Double.parseDouble(dec.readString());
        Double feeHuman = Double.parseDouble(dec.readString());

        TTransaction transaction = new TTransaction();
        transaction.setBlockNo(blockNo);
        transaction.setFromAddress(fromAddress);
        transaction.setToAddress(toAddress);
        transaction.setValue(value);
        transaction.setFee(fee);
        transaction.setHash(hash);
        transaction.setHexdata(hexdata);
        transaction.setNode(node);
        transaction.setDateCreated(dateCreated);
        transaction.setStatus(status);
        transaction.setEnable(enable);
        transaction.setValueHuman(valueHuman);
        transaction.setFeeHuman(feeHuman);
        return transaction;
    }

    /**
     * Returns size of the transaction in bytes
     *
     * @return size in bytes
     */
    public int size() {
        return toBytes().length;
    }
}
