package com.faa.chain.crypto;

import java.math.BigInteger;

import com.faa.chain.rlp.RlpDecoder;
import com.faa.chain.rlp.RlpList;
import com.faa.chain.rlp.RlpString;
import com.faa.chain.utils.Numeric;


public class TransactionDecoder {

    public static RawTransaction decode(String hexTransaction) {
        byte[] transaction = Numeric.hexStringToByteArray(hexTransaction);
        RlpList rlpList = RlpDecoder.decode(transaction);
        RlpList values = (RlpList) rlpList.getValues().get(0);

        String inputFrom = ((RlpString) values.getValues().get(0)).asFaaString();
        String to = ((RlpString) values.getValues().get(1)).asFaaString();
        BigInteger value = ((RlpString) values.getValues().get(2)).asBigInteger();
        BigInteger fee = ((RlpString) values.getValues().get(3)).asBigInteger();
        String data = ((RlpString) values.getValues().get(4)).asString();
        if (values.getValues().size() > 5) {
            byte v = ((RlpString) values.getValues().get(5)).getBytes()[0];
            byte[] r = zeroPadded(((RlpString) values.getValues().get(6)).getBytes(), 32);
            byte[] s = zeroPadded(((RlpString) values.getValues().get(7)).getBytes(), 32);
            Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);

            return new SignedRawTransaction(inputFrom, to, value, fee, signatureData);

        } else {
            return new RawTransaction(inputFrom, to, value, fee);
        }
    }

    private static byte[] zeroPadded(byte[] value, int size) {
        if (value.length == size) {
            return value;
        }
        int diff = size - value.length;
        byte[] paddedValue = new byte[size];
        System.arraycopy(value, 0, paddedValue, diff, value.length);
        return paddedValue;
    }
}
