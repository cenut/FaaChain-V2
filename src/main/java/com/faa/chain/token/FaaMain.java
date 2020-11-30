package com.faa.chain.token;

/**
 * 主币 FAA   Flying Apsara
 */
public class FaaMain extends CoinType {

    private static FaaMain main;

    static {
        main = new FaaMain();
    }

    public static FaaMain get() {
        return main;
    }

    public FaaMain() {
        this.setDecimals(1000000000000000000L);
        this.setId("FAA");
        this.setName("Flying Apsara");
        this.setTokens(false);
        this.setBaseUnit(1000000000000000000L);
    }

}
