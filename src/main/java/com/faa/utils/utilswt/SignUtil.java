package com.faa.utils.utilswt;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.*;

/**
 * Created by xt on 2016/11/14.
 */
public class SignUtil {

    static final Logger logger = LoggerFactory.getLogger(SignUtil.class);

    public static String getSignData(Map map){
        StringBuilder retStr = new StringBuilder();
        //获取map集合中的所有键，存入到Set集合中，
        Set<Map.Entry<String,String>> entry = map.entrySet();
        //通过迭代器取出map中的键值关系，迭代器接收的泛型参数应和Set接收的一致
        Iterator<Map.Entry<String,String>> it = entry.iterator();
        while (it.hasNext())
        {
            //将键值关系取出存入Map.Entry这个映射关系集合接口中
            Map.Entry<String,String>  me = it.next();
            //使用Map.Entry中的方法获取键和值
            String key = me.getKey();
            String value = me.getValue();
            retStr.append(key + "=" + value+"&");
        }
        return retStr.toString().substring(0,retStr.length()-1);
    }


    public static Map<String,String> getMapData(String data){
        Map<String,String> returnMap = new TreeMap<>();
        String [] datas = data.split("&");
        for (int i =0;i<datas.length;i++){
            String [] temp = datas[i].split("=");
            StringBuffer tempBuff = new StringBuffer();
            for (int j=1;j<temp.length;j++){
                tempBuff.append(temp[j]+"=");
            }
            if (temp.length != 1) {
                returnMap.put(temp[0],tempBuff.toString().substring(0,tempBuff.length()-1));
            }else{
                returnMap.put(temp[0],"");
            }
        }
        return returnMap;
    }

    public static Map<String,String> jsonToMap(JSONObject jsonStr){
        Map<String,String> returnMap = new TreeMap<>();
        try {
            Set<String> set = jsonStr.keySet();
            Iterator i = set.iterator();
            while (i.hasNext()){
                String key = String.valueOf(i.next());
                String value = (String) jsonStr.get(key);
                returnMap.put(key, value);
            }
            return returnMap;
        } catch (Exception e) {
            logger.error("数据转换异常！");
        }
        return null;
    }

    /**
     * 传化签名计算
     * 通过map参数生成sign字符串
     * @param map 数据参数
     * @param secret 约定的私钥
     * @return
     */
    public static String chSign(Map<String, String> map, String secret) throws Exception
    {
        map.put("dog_sk", secret);
        Object[] key = map.keySet().toArray();
        Arrays.sort(key);
        StringBuffer sb = new StringBuffer();
        for (int i = key.length - 1; i >= 0; i--) {
            sb.append(map.get(key[i]));
        }
        String keyString = sb.toString();
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(keyString.getBytes("utf-8"));
        String result = "";
        byte[] temp;
        temp = md5.digest("".getBytes("utf-8"));
        for (int i = 0; i < temp.length; i++) {
            result += Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
        }
        return result.toUpperCase();
    }

    public static void main(String [] args){
        Map a = getMapData("sign=48ad8962c5d98e59b1742a7b47f42e9e&r1_merchantNo=B103484154&r3_amount=1.60&retCode=0000&r6_createDate=2017-09-28 21:59:41&r7_completeDate=2017-09-28 22:00:39&r5_business=KUAI&r2_orderNumber=1709282158460100199&trxType=OnlineQuery&r9_withdrawStatus=INIT&r4_bankId=KUAI&r8_orderStatus=SUCCESS");
        System.out.println(a);
    }

}
