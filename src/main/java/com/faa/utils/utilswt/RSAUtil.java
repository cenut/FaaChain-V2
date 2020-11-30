package com.faa.utils.utilswt;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xt on 2016/12/16.
 */
public class RSAUtil {
    static final Logger logger = LoggerFactory.getLogger(RSAUtil.class);

    /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "SHA1WithRSA";//SHA1WithRSA  MD5withRSA

    /**
     * 获取公钥的key
     */
    private static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * 获取私钥的key
     */
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * <p>
     * 生成密钥对(公钥和私钥)
     * </p>
     * bitsize: 1024/2048/...
     *
     * @return
     * @throws Exception
     */
    public static Map<String, Object> genKeyPair(int bitsize) throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(bitsize);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Object> keyMap = new HashMap<>(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * <p>
     * 用私钥对信息生成数字签名
     * </p>
     *
     * @param data
     *            已加密数据
     * @param privateKey
     *            私钥(BASE64编码)
     *
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = Base64Util.decode(privateKey.getBytes());
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        try {
            signature.initSign(privateK);
        } catch (Exception e) {
            logger.warn("{}", e);
        }
        signature.update(data);
        return new String(Base64Util.encode(signature.sign()));
    }

    public static String signSafe(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = Base64Util.decode(privateKey.getBytes());
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        try {
            signature.initSign(privateK);
        } catch (Exception e) {
            logger.warn("{}", e);
        }
        signature.update(data);
        return new String(Base64Util.encodeSafe(signature.sign()));
    }

    /**
     * <p>
     * 校验数字签名
     * </p>
     *
     * @param data
     *            已加密数据
     * @param publicKey
     *            公钥(BASE64编码)
     * @param sign
     *            数字签名
     *
     * @return
     * @throws Exception
     *
     */
    public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
        byte[] keyBytes = Base64Util.decode(publicKey.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(Base64Util.decode(sign.getBytes()));
    }

    /**
     * <P>
     * 私钥解密
     * </p>
     *
     * @param encryptedData
     *            已加密数据
     * @param privateKey
     *            私钥(BASE64编码)
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey) throws Exception {
        byte[] keyBytes = Base64Util.decode(privateKey.getBytes());
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        return decrypt(encryptedData, privateK, null);
    }

    public static String decryptByPrivateKey2(byte[] encryptedData, String privateKey) throws Exception {
        byte[] keyBytes = Base64Util.decode(privateKey.getBytes());
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        return decrypt2(encryptedData, privateK);
    }

    public static byte[] decryptByPrivateKey(byte[] data, String modulus, String exponent, Provider provider)
            throws Exception {
        return decrypt(data, getPrivateKey(modulus, exponent), provider);
    }

    /**
     * <p>
     * 公钥解密
     * </p>
     *
     * @param encryptedData
     *            已加密数据
     * @param publicKey
     *            公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(byte[] encryptedData, String publicKey) throws Exception {
        byte[] keyBytes = Base64Util.decode(publicKey.getBytes());
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        return decrypt(encryptedData, publicK, null);
    }

    public static byte[] decryptByPublicKey(byte[] data, String modulus, String exponent, Provider provider)
            throws Exception {
        return decrypt(data, getPublicKey(modulus, exponent), provider);
    }

    /**
     * <p>
     * 公钥加密
     * </p>
     *
     * @param data
     *            源数据
     * @param publicKey
     *            公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws Exception {
        byte[] keyBytes = Base64Util.decode(publicKey.getBytes());
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return encrypt(data, keyFactory.generatePublic(x509KeySpec), null);
    }

    public static byte[] encryptByPublicKey(byte[] data, String modulus, String exponent, Provider provider)
            throws Exception {
        return encrypt(data, getPublicKey(modulus, exponent), provider);
    }

    /**
     * <p>
     * 私钥加密
     * </p>
     *
     * @param data
     *            源数据
     * @param privateKey
     *            私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPrivateKey(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = Base64Util.decode(privateKey.getBytes());
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return encrypt(data, keyFactory.generatePrivate(pkcs8KeySpec), null);
    }

    public static byte[] encryptByPrivateKey(byte[] data, String modulus, String exponent, Provider provider)
            throws Exception {
        return encrypt(data, getPrivateKey(modulus, exponent), provider);
    }

    /**
     * <p>
     * 获取私钥
     * </p>
     *
     * @param keyMap
     *            密钥对
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return new String(Base64Util.encode(key.getEncoded()));
    }

    /**
     * <p>
     * 获取公钥
     * </p>
     *
     * @param keyMap
     *            密钥对
     * @return
     * @throws Exception
     */
    public static String getPublicKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return new String(Base64Util.encode(key.getEncoded()));
    }

    //----------------------------------------------------------------------------------------------------
    /**
     * 使用模和指数生成RSA公钥
     *
     * @param modulus
     *            模
     * @param pubicExponent
     *            指数
     * @return
     */
    public static RSAPublicKey getPublicKey(String modulus, String pubicExponent) {
        try {
            BigInteger b1 = new BigInteger(modulus);
            BigInteger b2 = new BigInteger(pubicExponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            logger.warn("{}", e);
            return null;
        }
    }

    /**
     * 生成私钥
     *
     * @param modulus
     *            模
     * @param privateExponent
     * @return RSAPrivateKey
     * @throws Exception
     */
    public static RSAPrivateKey getPrivateKey(String modulus, String privateExponent) throws Exception {
        try {
            BigInteger b1 = new BigInteger(modulus);
            BigInteger b2 = new BigInteger(privateExponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            logger.warn("{}", e);
            return null;
        }
    }

    public static byte[] encrypt(byte[] data, Key key, Provider provider) throws Exception {
        Cipher cipher = null;
        ByteArrayOutputStream out = null;
        try {

            // 对数据加密
            if (provider == null) cipher = Cipher.getInstance(KEY_ALGORITHM);
            else
                cipher = Cipher.getInstance(KEY_ALGORITHM, provider);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            int blockSize = cipher.getBlockSize();
            if (blockSize <= 0) blockSize = MAX_ENCRYPT_BLOCK;
//            logger.info("blockSize:" + blockSize);

            //int blocksSize = leavedSize != 0 ? data.length / blockSize + 1 : data.length / blockSize;
            int inputLen = data.length;
            out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > blockSize) {
                    cache = cipher.doFinal(data, offSet, blockSize);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * blockSize;
            }
            byte[] encryptedData = out.toByteArray();
            return encryptedData;
        } finally {
            cipher = null;
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
                out = null;
            }
            provider = null;
        }
    }

    public static byte[] decrypt(byte[] encryptedData, Key key, Provider provider) throws Exception {
        Cipher cipher = null;
        ByteArrayOutputStream out = null;
        try {

            if (provider == null) cipher = Cipher.getInstance(KEY_ALGORITHM);
            else
                cipher = Cipher.getInstance(KEY_ALGORITHM, provider);

            cipher.init(Cipher.DECRYPT_MODE, key);

            int blockSize = cipher.getBlockSize();
            if (blockSize <= 0) blockSize = MAX_DECRYPT_BLOCK;
            logger.info("blockSize:" + blockSize);

            int inputLen = encryptedData.length;
            out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > blockSize) {
                    cache = cipher.doFinal(encryptedData, offSet, blockSize);
                } else {
                    cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * blockSize;
            }
            byte[] decryptedData = out.toByteArray();

            return decryptedData;
        } finally {
            cipher = null;
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
                out = null;
            }
            provider = null;
        }
    }

    public static String decrypt2(byte[] encryptedData, Key key) throws Exception {
        Cipher cipher = null;
        ByteArrayOutputStream out = null;
        try {
            StringBuilder result = new StringBuilder();
            cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            for (int i = 0; i < encryptedData.length; i += 256) {
                byte[] decrypt = cipher.doFinal(ArrayUtils.subarray(encryptedData, i, i + 256));
                result.append(new String(decrypt, "UTF-8"));
            }
            return result.toString();
        } finally {
            cipher = null;
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
                out = null;
            }
        }
    }

}
