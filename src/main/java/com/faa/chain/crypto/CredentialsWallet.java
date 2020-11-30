package com.faa.chain.crypto;

import com.faa.chain.utils.Numeric;

/**
 * 账户钱包封装类.
 */
public class CredentialsWallet {

    private final ECKeyPair ecKeyPair;
    private final String address;

    private CredentialsWallet(ECKeyPair ecKeyPair, String address) {
        this.ecKeyPair = ecKeyPair;
        this.address = address;
    }

    public ECKeyPair getEcKeyPair() {
        return ecKeyPair;
    }

    public String getAddress() {
        return address;
    }

    public String getPrivateKey(){
        String keyOri = ecKeyPair.getPrivateKey().toString(16);
        StringBuilder sb = new StringBuilder(keyOri);
        String A = keyOri.substring(0, 8);
        sb.delete(0, 8);
        sb.append(A);
        keyOri = sb.toString();
        String B = keyOri.substring(keyOri.length() - 16);
        sb.delete(keyOri.length() - 16, keyOri.length());
        sb.insert(0, B);
        sb.insert(0, "fk");
        keyOri = sb.toString();
        String C = keyOri.substring(38, 39);
        sb.insert(26, C);
        keyOri = sb.toString();
        return keyOri.toLowerCase();
    }

    public static CredentialsWallet create(ECKeyPair ecKeyPair) {
        String address = Numeric.prependFaaPrefix(Keys.getAddress(ecKeyPair));
        return new CredentialsWallet(ecKeyPair, address);
    }

//    public static CredentialsWallet create(String privateKey, String publicKey) {
//        return create(new ECKeyPair(Numeric.toBigInt(privateKey), Numeric.toBigInt(publicKey)));
//    }

    public static CredentialsWallet create(String privateKey) {
        StringBuilder sb_ = new StringBuilder(privateKey);
        sb_.delete(26, 27);
        sb_.delete(0, 2);
        privateKey = sb_.toString();
        String A_ = privateKey.substring(0, 16);
        sb_.delete(0, 16);
        sb_.append(A_);
        privateKey = sb_.toString();
        String B_ = privateKey.substring(privateKey.length() - 8);
        sb_.delete(privateKey.length() - 8, privateKey.length());
        sb_.insert(0, B_);
        privateKey = sb_.toString();
        return create(ECKeyPair.create(Numeric.toBigInt(privateKey)));
    }

    @Override
    public int hashCode() {
        int result = ecKeyPair != null ? ecKeyPair.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CredentialsWallet:");
        sb.append("\naddress:     ").append(getAddress());
        sb.append("\nprivateKey:  ").append(getPrivateKey());
        return sb.toString();
    }
}
