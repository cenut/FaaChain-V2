package com.faa.chain.core;

import java.math.BigInteger;

import static java.util.Arrays.stream;

public enum Unit {
    NANO_ALC(0, "nALC"),

    MICRO_ALC(3, "μALC"),

    MILLI_ALC(6, "mALC"),

    ALC(9, "ALC"),

    KILO_ALC(12, "kALC"),

    MEGA_ALC(15, "MALC");

    public final int exp;
    public final long factor;
    public final String symbol;

    Unit(int exp, String symbol) {
        this.exp = exp;
        this.factor = BigInteger.TEN.pow(exp).longValueExact();
        this.symbol = symbol;
    }

    /**
     * Decode the unit from symbol.
     *
     * @param symbol
     *            the symbol text
     * @return a Unit object if valid; otherwise false
     */
    public static Unit of(String symbol) {
        return stream(values()).filter(v -> v.symbol.equals(symbol)).findAny().orElse(null);
    }
}
