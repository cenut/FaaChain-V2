package com.faa.utils;

import com.faa.chain.node.Transaction;

public class LocalTest {

    static double a = 0;
    static double b = 0;

    public static void main(String args[]){

        Transaction t = new Transaction("0xf87b960f0a38f30fbb0889bcdfffbb2f2afbdfca12bd49e9c1960f5e83296c47665182e1c31ce2815798857c8da5d2ad843b9aca00831e84808025a0b0e99c470e5ba1d10ab888ecb848830a012147064a629ab0082c3532d5bc0446a047397cae5c19018c1898dcb99985181fb6e6f59c868811e777b87417d185382e");
        System.out.println(t.getFrom());
        System.out.println(t.getTo());
        System.out.println(t.getValue());
        System.out.println(t.getFee());
        int rtn = t.validate();
        System.out.println(rtn);

//        String str = "12312334543";
//        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
//        System.out.println(pattern.matcher(str).matches());

//        AverageUtil a = new AverageUtil(20);
//        a.add(1.0);
//        a.add(1.0);
//        a.add(3.77);
//        a.add(3.77);
//        a.add(3.77);
//        System.out.println(a.getAvg());

//        String jsonString = HttpClient4.doGet("https://open.loex.io/open/api/get_ticker?symbol=faausdt");
//        Map resultA = JSON.parseObject(jsonString);
//        if(resultA.get("code").equals("0")){
//            Map resultB = JSON.parseObject(resultA.get("data").toString());
//            System.out.println(resultB.get("last"));
//        }

//        System.out.println(CommonUtil.REWARD_ROAD_A.length);
//        System.out.println(CommonUtil.REWARD_ROAD_B.length);
////        for (int i = CommonUtil.REWARD_ROAD_A.length - 1; i >= 0; i--) {
//        for (int i = 0; i < CommonUtil.REWARD_ROAD_A.length; i++) {
//            System.out.println(CommonUtil.REWARD_ROAD_A[i] + " :  " + CommonUtil.REWARD_ROAD_B[i]);
//        }

//        double faa = 168000000 - 7000000;
//        double all = 0;
//        String tmp = "";
//        for (int i = 0; i < 26; i++) {
////        while (true){
////            if(faa < 0.001){
////                break;
////            }
//
//            faa /= 2;
//            all += faa;
//            DecimalFormat decimalFormat = new DecimalFormat("0.######");//格式化设置
//            System.out.println(decimalFormat.format(all));
//            tmp += "," + decimalFormat.format(all);
//        }
//        System.out.println(tmp);
//
//        double r = 12;
//        String rtmp = "";
//        for (int i = 0; i < 26; i++) {
//            r /= 2;
//            DecimalFormat decimalFormat = new DecimalFormat("0.##################");//格式化设置
//            System.out.println(decimalFormat.format(r));
//            rtmp += "," + decimalFormat.format(r);
//        }
//        System.out.println(rtmp);

//        BigDecimal inputBCL = new BigDecimal("5600000123123456789");
//        BigDecimal baseUnitBCL = new BigDecimal(1000000000000000000L);
//        DecimalFormat decimalFormat = new DecimalFormat("0.##################");//格式化设置
//        System.out.println("FAA all format : "+decimalFormat.format(inputBCL.divide(baseUnitBCL)));

//        Random r = new Random();
//        for (int i = 0; i < 30; i++) {
//            int number = r.nextInt(6);
//            System.out.println(number);
//        }

//        for (int i = 0; i < 30; i++) {
//            float number = r.nextInt(10) + 5;
//            System.out.println(number / 10);
//        }

//        byte[] salt = new byte[64];
//        SecureRandom secureRandom = new SecureRandom();
//        secureRandom.setSeed(System.currentTimeMillis());  //使用系统时间作为种子
//        secureRandom.nextBytes(salt);
//        System.out.println(Numeric.toHexString(salt));

//        BigInteger a = new BigInteger("32156456546");
//        BigInteger b = new BigInteger("3216");
//        System.out.println(a.compareTo(b) == -1);
//        System.out.println(a.toString());

//        int PUBLIC_KEY_SIZE = 64;
//        int ADDRESS_SIZE = 160;
//        int ADDRESS_LENGTH_IN_HEX = ADDRESS_SIZE >> 2;
//        int PUBLIC_KEY_LENGTH_IN_HEX = PUBLIC_KEY_SIZE << 1;
//
//        System.out.println(ADDRESS_SIZE);
//        System.out.println(ADDRESS_LENGTH_IN_HEX);

    }
}
