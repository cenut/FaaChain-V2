package com.faa.chain.utils;

import com.faa.utils.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SystemUtil {

    private static final Logger logger = LoggerFactory.getLogger(SystemUtil.class);

    /**
     * Returns my public IP address.
     *
     * @return an IP address if available, otherwise local address
     */
    public static String getIp() {
        try {
            URL url = new URL("http://checkip.amazonaws.com/");
            URLConnection con = url.openConnection();
            con.addRequestProperty("User-Agent", CommonUtil.DEFAULT_USER_AGENT);
            con.setConnectTimeout(CommonUtil.DEFAULT_CONNECT_TIMEOUT);
            con.setReadTimeout(CommonUtil.DEFAULT_READ_TIMEOUT);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), UTF_8));
            String ip = reader.readLine().trim();
            reader.close();

            // only IPv4 is supported currently
            if (ip.matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
                return ip;
            }
        } catch (IOException e1) {
            logger.warn("Failed to retrieve your public IP address");

            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e2) {
                logger.warn("Failed to retrieve your localhost IP address");
            }
        }

        return InetAddress.getLoopbackAddress().getHostAddress();
    }

    public enum OsName {
        WINDOWS("Windows"),

        LINUX("Linux"),

        MACOS("macOS"),

        UNKNOWN("Unknown");

        private final String name;

        OsName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Returns the operating system name.
     *
     * @return
     */
    public static OsName getOsName() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (os.contains("win")) {
            return OsName.WINDOWS;
        } else if (os.contains("linux")) {
            return OsName.LINUX;
        } else if (os.contains("mac")) {
            return OsName.MACOS;
        } else {
            return OsName.UNKNOWN;
        }
    }

    /**
     * Returns the operating system architecture
     *
     * @return
     */
    public static String getOsArch() {
        return System.getProperty("os.arch");
    }
}
