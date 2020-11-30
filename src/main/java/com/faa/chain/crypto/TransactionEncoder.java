package com.faa.chain.crypto;

import java.util.ArrayList;
import java.util.List;

import com.faa.chain.rlp.RlpEncoder;
import com.faa.chain.rlp.RlpList;
import com.faa.chain.rlp.RlpString;
import com.faa.chain.rlp.RlpType;
import com.faa.chain.utils.Bytes;
import com.faa.chain.utils.Numeric;


public class TransactionEncoder {

    public static byte[] signMessage(RawTransaction rawTransaction, CredentialsWallet credentials) {
        byte[] encodedTransaction = encode(rawTransaction);
        Sign.SignatureData signatureData = Sign.signMessage(encodedTransaction, credentials.getEcKeyPair());

        return encode(rawTransaction, signatureData);
    }

    public static byte[] signMessage(RawTransaction rawTransaction, byte chainId, CredentialsWallet credentials) {
        byte[] encodedTransaction = encode(rawTransaction, chainId);
        Sign.SignatureData signatureData = Sign.signMessage(encodedTransaction, credentials.getEcKeyPair());

        Sign.SignatureData eip155SignatureData = createEip155SignatureData(signatureData, chainId);
        return encode(rawTransaction, eip155SignatureData);
    }

    public static Sign.SignatureData createEip155SignatureData(Sign.SignatureData signatureData, byte chainId) {
        byte v = (byte) (signatureData.getV() + (chainId << 1) + 8);

        return new Sign.SignatureData(v, signatureData.getR(), signatureData.getS());
    }

    public static byte[] encode(RawTransaction rawTransaction) {
        return encode(rawTransaction, null);
    }

    public static byte[] encode(RawTransaction rawTransaction, byte chainId) {
        Sign.SignatureData signatureData = new Sign.SignatureData(chainId, new byte[] {}, new byte[] {});
        return encode(rawTransaction, signatureData);
    }

    private static byte[] encode(RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> values = asRlpValues(rawTransaction, signatureData);
        RlpList rlpList = new RlpList(values);
        return RlpEncoder.encode(rlpList);
    }

    static List<RlpType> asRlpValues(RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> result = new ArrayList<>();

        // an empty to address (contract creation) should not be encoded as a numeric 0 value
        String from = rawTransaction.getInputFrom();
        if (from != null && from.length() > 0) {
            // addresses that start with zeros should be encoded with the zeros included, not
            // as numeric values
            result.add(RlpString.create(Numeric.hexStringToByteArray(from)));
        } else {
            result.add(RlpString.create(""));
        }

        String to = rawTransaction.getTo();
        if (to != null && to.length() > 0) {
            // addresses that start with zeros should be encoded with the zeros included, not
            // as numeric values
            result.add(RlpString.create(Numeric.hexStringToByteArray(to)));
        } else {
            result.add(RlpString.create(""));
        }

        result.add(RlpString.create(rawTransaction.getValue()));
        result.add(RlpString.create(rawTransaction.getFee()));

        // value field will already be hex encoded, so we need to convert into binary first
        byte[] data = Numeric.hexStringToByteArray(rawTransaction.getData());
        result.add(RlpString.create(data));

        if (signatureData != null) {
            result.add(RlpString.create(signatureData.getV()));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
        }

        return result;
    }
}
