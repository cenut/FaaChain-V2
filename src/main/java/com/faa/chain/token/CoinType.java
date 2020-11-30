package com.faa.chain.token;


public class CoinType {

    // 币名称
    private String name;
    // 手续费单位
    private long baseUnit;
    // 币单位
    private long decimals;
    // 币缩写
    private String id;
    // 是否是代币 true：是 ，false:否
    private boolean tokens;
    // 代币合约地址
    private String tokensAddress;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBaseUnit() {
        return baseUnit;
    }

    public void setBaseUnit(long baseUnit) {
        this.baseUnit = baseUnit;
    }

    public long getDecimals() {
        return decimals;
    }

    public void setDecimals(long decimals) {
        this.decimals = decimals;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isTokens() {
        return tokens;
    }

    public void setTokens(boolean tokens) {
        this.tokens = tokens;
    }

    public String getTokensAddress() {
        return tokensAddress;
    }

    public void setTokensAddress(String tokensAddress) {
        this.tokensAddress = tokensAddress;
    }
}
