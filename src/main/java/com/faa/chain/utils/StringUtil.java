package com.faa.chain.utils;

public class StringUtil {
    /**
     * Returns if the given string is null or empty.
     *
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private StringUtil() {
    }
}
