package com.faa.utils;

import com.faa.chain.core.Unit;
import com.faa.chain.crypto.Key;
import com.faa.chain.net.*;
import com.faa.chain.utils.StringUtil;
import com.faa.chain.utils.SystemUtil;
import com.faa.chain.net.NodeManager.Node;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class CommonUtil {

    // 上一次已经使用的时间戳
    public static long USED_TIMEMILLIS = 0;

    // block sec
    public static int NOW_BLOCK_DELAY = 0;

    // 系统是否开放 1 true
    public static int IS_OPEN_ALL;

    // P2P bootstrap 服务器
    public static String BOOTSTRAP_SERVER = "";

    // 节点矿工
    public static String MINER_NODE = "";
    public static Key coinbase;

    // 爆块奖励
//    public static double REWARD_BLOCK;

    // FEE TEST
    public static double FEETEST;

    public static final String DEFAULT_USER_AGENT = "Mozilla/4.0";

    // 默认连接超时时间
    public static final int DEFAULT_CONNECT_TIMEOUT = 4000;

    // 默认读超时时间
    public static final int DEFAULT_READ_TIMEOUT = 4000;

    /**
     * Name of this client.
     */
    public static final String CLIENT_NAME = "Faachain";

    /**
     * Version of this client.
     */
    public static final String CLIENT_VERSION = "1.0.0";
    public static final String IMPLEMENTATION_VERSION = "1";

    // 减半进程
    public static double[] REWARD_ROAD_A = new double[]{
            80500000,
            120750000,
            140875000,
            150937500,
            155968750,
            158484375,
            159742187,
            160371093,
            160685546,
            160842773,
            160921386,
            160960693,
            160980346,
            160990173,
            160995086,
            160997543,
            160998771,
            160999385,
            160999692,
            161000000
    };
    public static double[] REWARD_ROAD_B = new double[]{
            6,
            3,
            1.5,
            0.75,
            0.375,
            0.1875,
            0.09375,
            0.04687,
            0.02343,
            0.01171,
            0.00585,
            0.00292,
            0.00146,
            0.00073,
            0.00036,
            0.00018,
            0.00009,
            0.00004,
            0.00002,
            0.00001
    };

    // 预挖数量
    public static double FAA_PRE;

    // 从交易所获取的当前价格和涨幅
    public static double FAA_PRICE;
    public static double FAA_ROSE;

    // 当前手续费
    public static double FEE_NOW;

    // 出块平均时间
    public static AverageUtil AVG_BLOCK;
    public static double AVG_BLOCK_S;

    // 是否有新提现请求
    public static boolean HAS_NEW_DRAW;

    // 根据os判断资源文件路径前缀
    public static String RES_ROOT;

    private static Properties prop;

    // p2p监听的IP地址
    public static String P2P_IPADDRESS = "0.0.0.0";
    public static Integer P2P_PORT = 9011;
    // Peerclient地址
    public static String P2P_MYIP;

    public static Set<Node> p2pSeedNodes = new HashSet<>();

    // TODO: MainNet种子节点，需要配置
    public static List<String> netDnsSeedsMainNet = Collections
            .unmodifiableList(Arrays.asList("127.0.0.1"));

    // 最大消息队列大小，默认4096
    public static Integer NET_MAX_MESSAGE_QUEUE_SIZE = 4096;
    public static Integer NET_MAX_OUT_BOUND_CONNECTIONS = 128;

    public static Set<MessageCode> netPrioritizedMessages = new HashSet<>(Arrays.asList(
            MessageCode.BFT_NEW_HEIGHT,
            MessageCode.BFT_NEW_VIEW,
            MessageCode.BFT_PROPOSAL,
            MessageCode.BFT_VOTE));

    public static int netMaxInboundConnectionsPerIp = 5;
    public static int netChannelIdleTimeout = 2 * 60 * 1000;
    public static int netMaxFrameBodySize = 128 * 1024;
    public static int netHandshakeExpiry = 5 * 60 * 1000;
    public static int netMaxPacketSize = 16 * 1024 * 1024;
    public static int netMaxInboundConnections = 512;

    // 默认使用main net
    public static final short MAINNET_VERSION = 0;
    public static final short DEVNET_VERSION = 1;
    public static Network network = Network.DEVNET;
    public static short networkVersion = DEVNET_VERSION;

    public static int netRelayRedundancy = 8;
    public static short MAXVALIDATORS = 21;

    // =========================
    // Transaction pool
    // =========================
    public static int poolBlockGasLimit = 10_000_000;
    public static BigInteger poolMinGasPrice = BigInteger.valueOf(10); // 10 NanoALC = 10 Gwei
    public static long poolMaxTransactionTimeDrift = TimeUnit.HOURS.toMillis(2);

    // =========================
    // Chain spec
    // =========================
    public static long maxBlockGasLimit = 30_000_000L; // 30m gas
    public static BigInteger minTransactionFee = BigInteger.valueOf(100);
    public static BigInteger minDelegateBurnAmount = BigInteger.valueOf(100000000);
    public static long nonVMTransactionGasCost = 5_000L;

    // Sync 相关参数
    // =========================
    public static long syncDownloadTimeout = 10_000L;
    public static int syncMaxQueuedJobs = 8192;
    public static int syncMaxPendingJobs = 256;
    public static int syncMaxPendingBlocks = 512;
    public static boolean syncDisconnectOnInvalidBlock = false;


    public static String getClientId() {
        return String.format("%s/v%s-%s/%s/%s",
                CLIENT_NAME,
                CLIENT_VERSION,
                IMPLEMENTATION_VERSION,
                SystemUtil.getOsName().toString(),
                SystemUtil.getOsArch());
    }

    public static CapabilityTreeSet getClientCapabilities() {
        return CapabilityTreeSet.of(Capability.FAACHAIN, Capability.FAST_SYNC);
    }

    public static Optional<String> p2pMyIp() {
        return StringUtil.isNullOrEmpty(P2P_MYIP) ? Optional.empty() : Optional.of(P2P_MYIP);
    }

    static{
        try {
            String os = System.getProperty("os.name");
            if(os.toLowerCase().startsWith("win")){
                RES_ROOT = System.getProperty("user.dir") + "/static/";
            }else{
                RES_ROOT = "static/";
            }

            InputStream in = new BufferedInputStream(new FileInputStream(RES_ROOT + "prop/mainconf.properties"));
            prop = new Properties();
            prop.load(new InputStreamReader(in, "utf-8"));

            BOOTSTRAP_SERVER = prop.getProperty("bootnode");
            // TODO: 从private key获取钱包地址
            MINER_NODE = prop.getProperty("miner");

            // 从配置文件读取private key生成coinbase
            String pk = prop.getProperty("key.private");
            String ANSI_RED = "\u001B[31m";
            String ANSI_RESET = "\u001B[0m";
            if ( StringUtil.isNullOrEmpty(pk) ) {
                coinbase = new Key();
                // 打印新建的private key
                System.out.println(ANSI_RED + "New key created, please save and config with option key.private: " + coinbase.getPrivateKey() + ANSI_RESET);
            } else {
                try {
                    coinbase = new Key(new BigInteger(pk));
                } catch (Exception e) {
                    System.out.println(ANSI_RED + "\nERROR: Invalid private key in config file, please check config key.private\n");
                    System.exit(1);
                }
            }

            // p2p配置项
            P2P_IPADDRESS = prop.getProperty("p2p.listenIp").trim();
            P2P_PORT = Integer.parseInt(prop.getProperty("p2p.listenPort").trim());

            P2P_MYIP = prop.getProperty("p2p.myIP").trim();

            // 配置seed nodes
            String[] nodes = prop.getProperty("p2p.seedNodes").trim().split(",");
            for (String node : nodes) {
                if (!node.trim().isEmpty()) {
                    String[] tokens = node.trim().split(":");
                    if (tokens.length == 2) {
                        p2pSeedNodes.add(new Node(tokens[0], Integer.parseInt(tokens[1])));
                    } else {
                        p2pSeedNodes.add(new Node(tokens[0], P2P_PORT));
                    }
                }
            }

            // 配置network类型
            // 默认使用main net
            String netname = prop.getProperty("network").trim();
            if ( netname == "main") {
                network = Network.MAINNET;
                networkVersion = MAINNET_VERSION;
            } else if ( netname == "dev" ) {
                network = Network.DEVNET;
                networkVersion = DEVNET_VERSION;
            }

            // message配置项
            NET_MAX_MESSAGE_QUEUE_SIZE = Integer.parseInt(prop.getProperty("net.maxMessageQueueSize"));
            NET_MAX_OUT_BOUND_CONNECTIONS = Integer.parseInt(prop.getProperty("net.maxOutboundConnections"));

//            REWARD_BLOCK = Double.parseDouble(prop.getProperty("reward"));
            FEETEST = Double.parseDouble(prop.getProperty("ffftest"));

//            WALLET_ERC_PLATFORM = prop.getProperty("walletercplatform");
//            WALLET_ERC_PLATFORM_PK = prop.getProperty("walletercplatformpk");

//            WALLET_ERC_DRAW = prop.getProperty("walletercdraw");
//            WALLET_ERC_DRAW_PK = prop.getProperty("walletercdrawpk");

//            PAY_DRAW_TYPE = Integer.parseInt(prop.getProperty("defultpdtype"));

//            BIGDRAW_LIMIT = Integer.parseInt(prop.getProperty("bigdrawlimit"));

            IS_OPEN_ALL = Integer.parseInt(prop.getProperty("isopenall"));

            FAA_PRE = 7000000;

//            REWARD_ROAD_A = new double[]{
//                    80500000,
//                    120750000,
//                    140875000,
//                    150937500,
//                    155968750,
//                    158484375,
//                    159742187,
//                    160371093,
//                    160685546,
//                    160842773,
//                    160921386,
//                    160960693,
//                    160980346,
//                    160990173,
//                    160995086,
//                    160997543,
//                    160998771,
//                    160999385,
//                    160999692,
//                    161000000
//            };
//            REWARD_ROAD_B = new double[]{
//                    6,
//                    3,
//                    1.5,
//                    0.75,
//                    0.375,
//                    0.1875,
//                    0.09375,
//                    0.04687,
//                    0.02343,
//                    0.01171,
//                    0.00585,
//                    0.00292,
//                    0.00146,
//                    0.00073,
//                    0.00036,
//                    0.00018,
//                    0.00009,
//                    0.00004,
//                    0.00002,
//                    0.00001
//            };

            FAA_PRICE = 0;
            FAA_ROSE = 0;
            FEE_NOW = 0.5;

            AVG_BLOCK = new AverageUtil(50);
            AVG_BLOCK_S = 0;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        HAS_NEW_DRAW = false;
    }

    public static void reloadMainConf(){
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(RES_ROOT + "prop/mainconf.properties"));
            prop = new Properties();
            prop.load(new InputStreamReader(in, "utf-8"));

            BOOTSTRAP_SERVER = prop.getProperty("bootnode");
            MINER_NODE = prop.getProperty("miner");
//            REWARD_BLOCK = Double.parseDouble(prop.getProperty("reward"));
            FEETEST = Double.parseDouble(prop.getProperty("ffftest"));

//            WALLET_ERC_PLATFORM = prop.getProperty("walletercplatform");
//            WALLET_ERC_PLATFORM_PK = prop.getProperty("walletercplatformpk");

//            WALLET_ERC_DRAW = prop.getProperty("walletercdraw");
//            WALLET_ERC_DRAW_PK = prop.getProperty("walletercdrawpk");

//            PAY_DRAW_TYPE = Integer.parseInt(prop.getProperty("defultpdtype"));

//            BIGDRAW_LIMIT = Integer.parseInt(prop.getProperty("bigdrawlimit"));

            IS_OPEN_ALL = Integer.parseInt(prop.getProperty("isopenall"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 较为全面的获取请求 IP 地址方法
    public static String getRemoteHost(HttpServletRequest request){
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1")?"127.0.0.1":ip;
    }

    // 计算字符串的 SHA-1 值
    public static String getSHA1(String string){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            String pwdSha1 = new String(md.digest(string.getBytes()));
            return pwdSha1;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}
