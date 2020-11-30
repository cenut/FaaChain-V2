package com.faa.chain.crypto;

import java.math.BigInteger;

import com.faa.chain.utils.Numeric;

/**
 * Transaction class used for signing transactions locally.
 */
public class RawTransaction {

//    private BigInteger nonce;
//    private BigInteger gasPrice;
//    private BigInteger gasLimit;
    private String inputFrom;
    private String to;
    private BigInteger value;
    private BigInteger fee;
    private String data;

//    protected RawTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to,
//                           BigInteger value, String data) {
//        this.nonce = nonce;
//        this.gasPrice = gasPrice;
//        this.gasLimit = gasLimit;
//        this.to = to;
//        this.value = value;
//
//        if (data != null) {
//            this.data = Numeric.cleanHexPrefix(data);
//        }
//    }

    protected RawTransaction(String from, String to, BigInteger value, BigInteger fee) {
        this.inputFrom = from;
        this.to = to;
        this.value = value;
        this.fee = fee;
        this.data = Numeric.cleanHexPrefix("");
    }

//    public static RawTransaction createContractTransaction(
//            BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, BigInteger value,
//            String init) {
//
//        return new RawTransaction(nonce, gasPrice, gasLimit, "", value, init);
//    }

//    public static RawTransaction createFaaTransaction(
//            BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to,
//            BigInteger value) {
//
//        return new RawTransaction(nonce, gasPrice, gasLimit, to, value, "");
//
//    }

    public static RawTransaction createFaaTransaction(String from, String to, BigInteger value, BigInteger fee) throws Exception {
        if(from.length() != 44 || to.length() != 44){
            throw new Exception("invalid address length");
        }
        // FAA little trick
        value = value.add(new BigInteger("3000000000000000000000000"));

        return new RawTransaction(from, to, value, fee);
    }

//    public static RawTransaction createTransaction(
//            BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, String data) {
//        return createTransaction(nonce, gasPrice, gasLimit, to, BigInteger.ZERO, data);
//    }

//    public static RawTransaction createTransaction(
//            BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to,
//            BigInteger value, String data) {
//
//        return new RawTransaction(nonce, gasPrice, gasLimit, to, value, data);
//    }

//    public BigInteger getNonce() {
//        return nonce;
//    }

//    public BigInteger getGasPrice() {
//        return gasPrice;
//    }

//    public BigInteger getGasLimit() {
//        return gasLimit;
//    }

    public String getInputFrom() {
        return inputFrom;
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
}
