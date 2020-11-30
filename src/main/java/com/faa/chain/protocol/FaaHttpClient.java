package com.faa.chain.protocol;

import com.faa.chain.protocol.http.HttpClient4;

import java.util.HashMap;
import java.util.Map;

/**
 * 基本 http 业务客户端
 */
public class FaaHttpClient {

    // 节点 api url
    public static String ip = "http://127.0.0.1:8061";

    // FAA主链 广播离线签名交易
    public static String faaSendRawTransaction(String signedTransactionData){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("signedTransactionData", signedTransactionData);
        return HttpClient4.doPost(ip + "/faaRawTransaction", paramMap);
    }
}
