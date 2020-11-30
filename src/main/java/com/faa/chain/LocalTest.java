package com.faa.chain;

import com.faa.chain.node.Transaction;
import com.faa.chain.protocol.FaaHttpClient;
import com.faa.chain.token.CoinType;
import com.faa.chain.token.CoinsBaseUnits;
import com.faa.chain.token.FaaMain;
import com.faa.chain.utils.Numeric;
import com.faa.chain.crypto.*;

import java.math.BigDecimal;


public class LocalTest {

    /**
     * 主账户配置挖矿
     * @return
     * @throws Exception
     */
    public static void mining() throws Exception {}

    /**
     * 创建钱包
     * @return
     * @throws Exception
     */
    public static CredentialsWallet createCredentialsWallet() throws Exception {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        return CredentialsWallet.create(ecKeyPair);
    }

    /**
     * 离线签名转账 faa
     * @param sendAddress
     * @param receiveAddress
     * @param quantity
     * @return
     */
    public static String offLineTransactionFaa(String sendAddress, String receiveAddress, String quantity, String fee, String privateKey) {
        try{

            //转币类型
            FaaMain coinType =  FaaMain.get();
            //生成离线交易数据
            String signedTransactionData = createRawTx(sendAddress, receiveAddress, quantity, fee, privateKey, coinType);

//            return signedTransactionData;
            //将离线交易数据广播 获取交易 hash
            String transactionHash = FaaHttpClient.faaSendRawTransaction(signedTransactionData);

            if(null == transactionHash){
                return "-1";
            }else {
                return transactionHash;
            }

        }catch (Exception e){
            e.printStackTrace();
            return "-1";
        }
    }

    // 构造 signedTransactionData
    private static String createRawTx(String sendAddress, String receiveAddress, String quantity, String fee, String privateKey, CoinType coinType) throws Exception {
        BigDecimal _quantity_ = CoinsBaseUnits.toDecimals(quantity, coinType);
        BigDecimal _fee_ = CoinsBaseUnits.toBaseUnit(fee, coinType);

        RawTransaction rawTransaction = RawTransaction.createFaaTransaction(sendAddress, receiveAddress, _quantity_.toBigInteger(), _fee_.toBigInteger());

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, CredentialsWallet.create(privateKey));

        return Numeric.toHexString(signedMessage);
    }


    // 测试 生成离线交易签名数据
    public static String createOffLineTransactionFaa(String sendAddress, String receiveAddress, String quantity, String fee, String privateKey) {
        try{

            // 币类型
            FaaMain coinType =  FaaMain.get();
            // 生成离线交易数据
            String signedTransactionData = createRawTx(sendAddress, receiveAddress, quantity, fee, privateKey, coinType);

            return signedTransactionData;

        }catch (Exception e){
            e.printStackTrace();
            return "-1";
        }
    }

    public static void main(String[] args) throws Exception {

        // LJL 生成离线签名交易
        String signed = createOffLineTransactionFaa("FWC58026E5D9C64D4F427E48C8A341AF50732DEB46A9", "FW35B1A634BEA64BA2CBE10DBA67029954F4DD149FE5", "30168", "1.23", "fk63c7b1b87015b0fc95d1938b757ddc221f32078ba139ce67afd229f7f9166b0c8");
        System.out.println("signed transaction: " + signed);
        System.out.println();

        // LJL 验证离线签名交易
        Transaction t = new Transaction(signed);
        int rtn = t.validate();
        if (rtn == 0){
            System.out.println("from: " + t.getFrom());
            System.out.println("to: " + t.getTo());
            System.out.println("value: " + t.getValue());
            System.out.println("fee: " + t.getFee());
        }else{
            System.out.println("invalid transaction");
        }
    }
}
