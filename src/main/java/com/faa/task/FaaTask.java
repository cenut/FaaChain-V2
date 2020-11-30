package com.faa.task;

import com.alibaba.fastjson.JSON;
import com.faa.chain.crypto.CredentialsWallet;
import com.faa.chain.crypto.RawTransaction;
import com.faa.chain.crypto.TransactionEncoder;
import com.faa.chain.net.PendingManager;
import com.faa.chain.node.Transaction;
import com.faa.chain.token.CoinType;
import com.faa.chain.token.CoinsBaseUnits;
import com.faa.chain.token.FaaMain;
import com.faa.chain.utils.Numeric;
import com.faa.service.*;
import com.faa.utils.*;
import org.apache.log4j.Logger;
import com.faa.chain.protocol.http.HttpClient4;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * FAA主链相关定时任务
 */
@Component
@EnableScheduling
public class FaaTask {
    Logger logger = Logger.getLogger(FaaTask.class);
    Logger loggerP = Logger.getLogger("pointLog");

    @Resource
    private BlockService blockService;

    @Resource
    private PendingManager pendingManager;

    // 多线程同时执行多个任务
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(12);
        return taskScheduler;
    }

    // 每 1 秒
    @Scheduled(cron="0/1 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void createBlock() throws Exception {
        // 最快 6 秒尝试一次出块
        if(CommonUtil.NOW_BLOCK_DELAY > 6){
        }
        else{
            CommonUtil.NOW_BLOCK_DELAY += 1;
        }
    }

    // 每 36 秒从交易所获取FAA单价
    @Scheduled(cron="0/36 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void updatePrice() throws Exception {
        String jsonString = HttpClient4.doGet("https://open.loex.io/open/api/get_ticker?symbol=faausdt");
        Map resultA = JSON.parseObject(jsonString);
        if (resultA.get("code").equals("0")) {
            Map resultB = JSON.parseObject(resultA.get("data").toString());
            CommonUtil.FAA_PRICE = Double.parseDouble(resultB.get("last").toString());
            double rose = Double.parseDouble(resultB.get("rose").toString());
            BigDecimal b = new BigDecimal(rose * 100);
            CommonUtil.FAA_ROSE = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
    }

    // 每 1 秒 addTransaction
    @Scheduled(cron="0/1 * * * * ?")
    public void testAddTransaction() {
        String signed = createOffLineTransactionFaa("FWC58026E5D9C64D4F427E48C8A341AF50732DEB46A9", "FW35B1A634BEA64BA2CBE10DBA67029954F4DD149FE5", "30168", "1.23", "fk63c7b1b87015b0fc95d1938b757ddc221f32078ba139ce67afd229f7f9166b0c8");
        Transaction tx = new Transaction(signed);
        pendingManager.addTransaction(tx);
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

    // 构造 signedTransactionData
    private static String createRawTx(String sendAddress, String receiveAddress, String quantity, String fee, String privateKey, CoinType coinType) throws Exception {
        BigDecimal _quantity_ = CoinsBaseUnits.toDecimals(quantity, coinType);
        BigDecimal _fee_ = CoinsBaseUnits.toBaseUnit(fee, coinType);

        RawTransaction rawTransaction = RawTransaction.createFaaTransaction(sendAddress, receiveAddress, _quantity_.toBigInteger(), _fee_.toBigInteger());

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, CredentialsWallet.create(privateKey));

        return Numeric.toHexString(signedMessage);
    }

}
