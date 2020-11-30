package com.faa.chain.node;

import com.faa.chain.core.TransactionType;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;
import com.faa.utils.CommonUtil;
import com.faa.chain.crypto.Hash;
import com.faa.chain.crypto.SignedRawTransaction;
import com.faa.chain.crypto.TransactionDecoder;
import org.apache.log4j.Logger;
import com.faa.chain.token.CoinsBaseUnits;
import com.faa.chain.token.FaaMain;
import com.faa.chain.utils.Numeric;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.sql.Timestamp;


public class Transaction {

    private Logger logger = Logger.getLogger(Transaction.class);

    public static final int ADDRESS_LENGTH = 44;

    private String hexTransaction;

    private String from;
    private String to;
    private BigInteger value;
    private BigInteger fee;
    private String data;

    private String hash;

    private BigInteger blockId;
    private long timestamp;
    private TransactionType type;

    public Transaction(String hexTransaction) {
        this.hexTransaction = hexTransaction;
        this.timestamp = System.currentTimeMillis();
        this.type = TransactionType.COINBASE;
    }

    private boolean verifyTransaction(SignedRawTransaction srt) {
        try {
            if (from.equals(srt.getFrom())){
                return true;
            }else{
                return false;
            }

        } catch (SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int validate(){
        SignedRawTransaction srt = (SignedRawTransaction) TransactionDecoder.decode(hexTransaction);

        this.from = srt.getInputFrom();
        this.to = srt.getTo();
        /**
         * little trick
         * 100 100000000000000000000
         * 3000000 3000000000000000000000000
         */
        this.value = srt.getValue().subtract(new BigInteger("3000000000000000000000000"));
        this.fee = srt.getFee();
        this.data = srt.getData();

        if (null == from || from.length() != ADDRESS_LENGTH){
            logger.warn("Send address is not valid");
            return 1;
        }
        if (verifyTransaction(srt) == false){
            logger.warn("tampered transaction");
            return 2;
        }
        if (null == to || to.length() != ADDRESS_LENGTH){
            logger.warn("Receive address is not valid");
            return 3;
        }
        if (null == value || value.compareTo(new BigInteger("0")) == -1
                || value.compareTo(new BigInteger("100000000000000000000000000")) == 1) {
            logger.warn("Value is not valid");
            return 4;
        }
        if (null == fee || fee.compareTo(new BigInteger("0")) == -1
                || fee.compareTo(new BigInteger("10000000000000000000000")) == 1) {
            logger.warn("Fee is not valid");
            return 5;
        }

        Double feeMin = CommonUtil.FEETEST * 0.49;
        FaaMain coinType =  FaaMain.get();
        BigInteger feeMinB = CoinsBaseUnits.toBaseUnit(feeMin.toString(), coinType).toBigInteger();
        if (fee.compareTo(feeMinB) == -1){
            logger.warn("Fee is not valid");
            return 5;
        }

        long now = System.currentTimeMillis();  //使用系统时间作为种子
        if(CommonUtil.USED_TIMEMILLIS == now){
            now += 1;
        }
        byte[] salt = new byte[64];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(now);
        secureRandom.nextBytes(salt);
        String hexHash = Hash.sha3(this.hexTransaction + Numeric.toHexString(salt));
        this.hash = Numeric.prependFaaTransPrefix(Numeric.cleanHexPrefix(hexHash)).toLowerCase();
        CommonUtil.USED_TIMEMILLIS = now;

        return 0;
    }

    public void setBlockId(BigInteger blockId) {
        this.blockId = blockId;
    }

    public String getHexTransaction() {
        return hexTransaction;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public BigInteger getValue() {
        return value;
    }

    public BigInteger getFee() {
        return fee;
    }

    public String getData() {
        return data;
    }

    public String getHash() {
        return hash;
    }

    public BigInteger getBlockId() {
        return blockId;
    }

    public Boolean isVMTransaction() { return false; }

    public long getTimestamp() {
        return timestamp;
    }

    public TransactionType getType() {
        return type;
    }

    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeString(hexTransaction);

        return enc.toBytes();
    }

    public static Transaction fromBytes(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        String hexTransaction = dec.readString();

        return new Transaction(hexTransaction);
    }
}
