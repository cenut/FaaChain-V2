/**
 * Copyright (c) 2019 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.faa.chain.core;

import com.faa.chain.utils.Bytes;
import com.faa.chain.net.SimpleDecoder;
import com.faa.chain.net.SimpleEncoder;

import java.util.Base64;

public class TransactionResult {

    /**
     * Transaction execution result code.
     */
    public enum Code {

        /**
         * Success. The values has to be 0x01 for compatibility.
         */
        SUCCESS(0x01),

        /**
         * VM failure, e.g. REVERT, STACK_OVERFLOW, OUT_OF_GAS, etc.
         */
        FAILURE(0x02),

        /**
         * The transaction hash is invalid (should NOT be included on chain).
         */
        INVALID(0x20),

        /**
         * The transaction format is invalid. See {@link Transaction#validate(Network)}
         */
        INVALID_FORMAT(0x21),

        /**
         * The transaction timestamp is incorrect.
         */
        INVALID_TIMESTAMP(0x22),

        /**
         * The transaction type is invalid.
         */
        INVALID_TYPE(0x23),

        /**
         * The transaction nonce does not match the account nonce.
         */
        INVALID_NONCE(0x24),

        /**
         * The transaction fee (or gas * gasPrice) doesn't meet the minimum.
         */
        INVALID_FEE(0x25),

        /**
         * The transaction data is invalid, typically too large.
         */
        INVALID_DATA(0x27),

        /**
         * Insufficient available balance.
         */
        INSUFFICIENT_AVAILABLE(0x28),

        /**
         * Insufficient locked balance.
         */
        INSUFFICIENT_LOCKED(0x29),

        /**
         * Invalid delegate name.
         */
        INVALID_DELEGATE_NAME(0x2a),

        /**
         * Invalid burning address.
         */
        INVALID_DELEGATE_BURN_ADDRESS(0x2b),

        /**
         * Invalid delegate burn amount.
         */
        INVALID_DELEGATE_BURN_AMOUNT(0x2c),

        /**
         * The DELEGATE operation is invalid.
         */
        INVALID_DELEGATING(0x2d),

        /**
         * The VOTE operation is invalid.
         */
        INVALID_VOTING(0x2e),

        /**
         * The UNVOTE operation is invalid.
         */
        INVALID_UNVOTING(0x2f);

        private static Code[] map = new Code[256];

        static {
            for (Code code : Code.values()) {
                map[code.v] = code;
            }
        }

        private byte v;

        Code(int c) {
            this.v = (byte) c;
        }

        public static Code of(int c) {
            return map[c];
        }

        public byte toByte() {
            return v;
        }

        public boolean isSuccess() {
            return this == SUCCESS;
        }

        public boolean isFailure() {
            return this == FAILURE;
        }

        public boolean isRejected() {
            return !isSuccess() && !isFailure();
        }

        public boolean isAcceptable() {
            return isSuccess() || isFailure();
        }
    }

    /**
     * Transaction execution result code.
     */
    protected Code code;

    /**
     * Transaction returns.
     */
    protected byte[] returnData;

    /**
     * Gas info
     */
    protected long gas;
    protected Amount gasPrice;
    protected long gasUsed;

    /**
     * Block info
     */
    protected long blockNumber;

    /**
     * Create a transaction result.
     */
    public TransactionResult() {
        this(Code.SUCCESS);
    }

    public TransactionResult(Code code) {
        this(code, Bytes.EMPTY_BYTES);
    }

    public TransactionResult(Code code, byte[] returnData) {
        this.code = code;
        this.returnData = returnData;

        this.gas = 0;
        this.gasPrice = Amount.ZERO;
        this.gasUsed = 0;

        this.blockNumber = 0;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public byte[] getReturnData() {
        return returnData;
    }

    public void setReturnData(byte[] returnData) {
        this.returnData = returnData;
    }

    public long getGas() {
        return gas;
    }

    public void setGas(long gas, Amount gasPrice, long gasUsed) {
        this.gas = gas;
        this.gasPrice = gasPrice;
        this.gasUsed = gasUsed;
    }

    public Amount getGasPrice() {
        return gasPrice;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public static TransactionResult fromBytes(byte[] bytes) {
        TransactionResult result = new TransactionResult();

        SimpleDecoder dec = new SimpleDecoder(bytes);
        Code code = Code.of(dec.readByte());
        result.setCode(code);

        byte[] returnData = dec.readBytes();
        result.setReturnData(returnData);

        // Dirty hack to maintain backward compatibility
        if (dec.getReadIndex() != bytes.length) {
            long gas = dec.readLong();
            Amount gasPrice = dec.readAmount();
            long gasUsed = dec.readLong();
            result.setGas(gas, gasPrice, gasUsed);

            long blockNumber = dec.readLong();
            result.setBlockNumber(blockNumber);

        }

        return result;
    }

    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeByte(code.toByte());
        enc.writeBytes(returnData);

        enc.writeLong(gas);
        enc.writeAmount(gasPrice);
        enc.writeLong(gasUsed);

        enc.writeLong(blockNumber);

        return enc.toBytes();
    }

    public byte[] toBytesForMerkle() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeByte(code.toByte());
        enc.writeBytes(returnData);

        return enc.toBytes();
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "code=" + code +
                ", returnData=" + Base64.getEncoder().encode(returnData) +
                ", gas=" + gas +
                ", gasPrice=" + gasPrice +
                ", gasUsed=" + gasUsed +
                ", blockNumber=" + blockNumber +
                '}';
    }
}
