package com.faa.utils.utilswt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 签名帮助类
 */
public class SignUtils {

    /**
     * @param params 签名数据map
     * @param secret 密钥
     * @return
     */
    public static String sign(Map<String, String> params, String secret) throws Exception{

        // 根据元素的自然顺序按升序进行排序
        List<String> keys = Arrays.asList(params.keySet().toArray(new String[params.size()]));
        Collections.sort(keys);

        // 根据不同的签名方法计算签名值
        String sign = null;
        StringBuilder signData = new StringBuilder();
        signData.append(secret);
        for (String key : keys) {
            signData.append(key).append(trimToEmpty(params.get(key)));
        }
        signData.append(secret);

        sign = MD5Util.getMD5(signData.toString().getBytes("UTF-8"));
        return sign.toUpperCase();
    }

    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

}

