package com.faa.chain.crypto;

import com.faa.chain.crypto.Sign.SignatureData;
import com.faa.chain.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Objects;

import static com.faa.chain.crypto.Sign.signedMessageToKey;

public class Key {
    private static final Logger logger = LoggerFactory.getLogger(Key.class);

    protected ECKeyPair keypair;
    protected BigInteger privateKey;
    protected BigInteger publicKey;

    /**
     * Creates a random EC key pair
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public Key() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        keypair = Keys.createEcKeyPair();
        privateKey = keypair.getPrivateKey();
        publicKey = keypair.getPublicKey();
    }

    /**
     * Creates a key pair with a specified private key
     *
     * @param privateKey
     */
    public Key(BigInteger privateKey) {
        keypair = ECKeyPair.create(privateKey);
        this.privateKey = keypair.getPrivateKey();
        this.publicKey = keypair.getPublicKey();
    }

    private Key(BigInteger privateKey, BigInteger publicKey) {
        keypair = ECKeyPair.create(privateKey);
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public BigInteger getPrivateKey() { return privateKey; }

    public BigInteger getPublicKey() { return publicKey; }

    /**
     *
     * @return the Faachain address.
     */
    public String toAddress() {
        return Numeric.prependFaaPrefix(Keys.getAddress(this.publicKey));
    }

    public SignatureData sign(byte[] message) {
        SignatureData data = Sign.signMessage(message, this.keypair);
        return data;
    }

    public static boolean verify(byte[] message, SignatureData signature, String peerID) {
        if (message != null && signature != null) {
            try {
                BigInteger pk = signedMessageToKey(message, signature);
                String address = Numeric.prependFaaPrefix(Keys.getAddress(pk));
                if (!Objects.equals(address, peerID)) {
                    return false;
                }
                return true;
            } catch (Exception e) {
                // do nothing
            }
        }

        return false;
    }

    public static boolean verify(byte[] message, SignatureData signature) {
        if (message != null && signature != null) {
            try {
                BigInteger pk = signedMessageToKey(message, signature);
                String address = Numeric.prependFaaPrefix(Keys.getAddress(pk));
                return true;
            } catch (Exception e) {
                // do nothing
            }
        }

        return false;
    }
}
