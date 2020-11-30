package com.faa.chain.token;

import java.math.BigDecimal;

public class CoinsBaseUnits {
    public static BigDecimal zero = new BigDecimal(0);

    public static BigDecimal toBigDecimal(String input) {
        BigDecimal inputBCL = new BigDecimal(input);
        return inputBCL;
    }

    // 5.6个币 》 5600000000000000000
    public static BigDecimal toBaseUnit(String input, CoinType coinType) {
        BigDecimal inputBCL = new BigDecimal(input);

        BigDecimal baseUnitBCL = new BigDecimal(coinType.getBaseUnit());

        return inputBCL.multiply(baseUnitBCL);
    }

    // 5600000000000000000 》 5.6个币
    public static BigDecimal toHumanUnit(String input, CoinType coinType) {
        BigDecimal inputBCL = new BigDecimal(input);

        BigDecimal baseUnitBCL = new BigDecimal(coinType.getBaseUnit());

        return inputBCL.divide(baseUnitBCL);
    }

    public static BigDecimal toDecimals(String input, CoinType coinType) {
        BigDecimal inputBCL = new BigDecimal(input);

        BigDecimal baseUnitBCL = new BigDecimal(coinType.getDecimals());

        return inputBCL.multiply(baseUnitBCL);
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }
}
