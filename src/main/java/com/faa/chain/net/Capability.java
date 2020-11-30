package com.faa.chain.net;

/**
 * This enum represents the available capabilities in current version of Alienchain
 * wallet. One peer should be disconnected by
 * ${@link ReasonCode#BAD_NETWORK_VERSION} if the peer doesn't support the
 * required set of capabilities.
 */
public enum Capability {

    /**
     * Mandatory for all network.
     */
    FAACHAIN,

    /**
     * This client supports the FAST_SYNC protocol.
     */
    FAST_SYNC,

    /**
     * This client supports the LIGHT protocol.
     */
    LIGHT;

    public static Capability of(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException | NullPointerException ex) {
            return null;
        }
    }

}
