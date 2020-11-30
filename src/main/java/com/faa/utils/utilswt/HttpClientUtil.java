package com.faa.utils.utilswt;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xt on 2016/11/21.
 */
public class HttpClientUtil {
    //超时时间
    private static final int timeOut = 60000;

    //最大连接数
    private static final int maxTotal = 1000;

    //基础连接数
    private static final int maxPerRoute = 50;

    //目标主机最大连接数
    private static final int maxRoute = 1000;

    private static CloseableHttpClient httpClient = null;

    private final static Object syncLock = new Object();

    private static void config(HttpRequestBase httpRequestBase) {
        // 设置Header等
//         httpRequestBase.setHeader("User-Agent", "Mozilla/5.0");
//         httpRequestBase
//         .setHeader("Accept",
//         "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//         httpRequestBase.setHeader("Accept-Language",
//         "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");// "en-US,en;q=0.5");
//         httpRequestBase.setHeader("Accept-Charset",
//         "ISO-8859-1,utf-8,gbk,gb2312;q=0.7,*;q=0.7");

        // 配置请求的超时设置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeOut)
                .setConnectTimeout(timeOut).setSocketTimeout(timeOut).build();
        httpRequestBase.setConfig(requestConfig);
    }

    /**
     * 获取HttpClient对象
     */
    public static CloseableHttpClient getHttpClient(String url) {
        String hostname = url.split("/")[2];
        int port = 80;
        if (hostname.contains(":")) {
            String[] arr = hostname.split(":");
            hostname = arr[0];
            port = Integer.parseInt(arr[1]);
        }
        if (httpClient == null) {
            synchronized (syncLock) {
                if (httpClient == null) {
                    httpClient = createHttpClient(hostname, port);
                }
            }
        }
        return httpClient;
    }

    /**
     * 创建HttpClient对象
     */
    public static CloseableHttpClient createHttpClient(String hostname, int port) {
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory
                .getSocketFactory();
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContexts.custom().loadTrustMaterial(null,
                    new TrustSelfSignedStrategy())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,hostnameVerifier);

//        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory
//                .getSocketFactory();

        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory> create().register("http", plainsf)
                .register("https",sslsf).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                registry);
        // 将最大连接数增加
        cm.setMaxTotal(maxTotal);
        // 将每个路由基础的连接增加
        cm.setDefaultMaxPerRoute(maxPerRoute);
        HttpHost httpHost = new HttpHost(hostname, port);
        // 将目标主机的最大连接数增加
        cm.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);

        // 请求重试处理
//        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
//            public boolean retryRequest(IOException exception,
//                                        int executionCount, HttpContext context) {
//                if (executionCount >= 5) {// 如果已经重试了5次，就放弃
//                    return false;
//                }
//                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
//                    return true;
//                }
//                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
//                    return false;
//                }
//                if (exception instanceof InterruptedIOException) {// 超时
//                    return false;
//                }
//                if (exception instanceof UnknownHostException) {// 目标服务器不可达
//                    return false;
//                }
//                if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
//                    return false;
//                }
//                if (exception instanceof SSLException) {// SSL握手异常
//                    return false;
//                }
//
//                HttpClientContext clientContext = HttpClientContext
//                        .adapt(context);
//                HttpRequest request = clientContext.getRequest();
//                // 如果请求是幂等的，就再次尝试
//                if (!(request instanceof HttpEntityEnclosingRequest)) {
//                    return true;
//                }
//                return false;
//            }
//        };

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();

        return httpClient;
    }

    private static void setPostParams(HttpPost httpost,
                                      Map<String, String> params) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            nvps.add(new BasicNameValuePair(key, params.get(key)));
        }
        try {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * POST方式发送json
     *
     * @param parameters
     * @return
     */
    public static String postJson(String url,String parameters) throws Exception{
        HttpPost httppost = new HttpPost(url);
        config(httppost);
        httppost.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));
        CloseableHttpResponse response = null;
        try {
            response = getHttpClient(url).execute(httppost,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            return result;
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * POST请求URL获取内容
     */
    public static String post(String url, Map params) throws Exception {
        HttpPost httppost = new HttpPost(url);
        config(httppost);
        setPostParams(httppost,params);
        CloseableHttpResponse response = null;
        try {
            response = getHttpClient(url).execute(httppost,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);
            return result;
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * GET请求URL获取内容
     */
    public static String get(String url) {
        HttpGet httpget = new HttpGet(url);
        config(httpget);
        CloseableHttpResponse response = null;
        try {
            response = getHttpClient(url).execute(httpget,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static void main(String[] args) {
//        try {
//            String returnStr = HttpClientUtil.post("http://10.200.201.105:18888/channel-transpond-scan/business", "business={\"amount\":\"1\",\"ChannelId\":\"000002\",\"merId\":\"15812342422\",\"notifyUrl\":\"baidu.com\",\"orderId\":\"test1479745332869\",\"payType\":\"2\"}");
//            System.out.println(returnStr);
//        }catch (Exception e){
//            System.out.println(e);
//        }

        // URL列表数组
//        String[] urisToGet = {
//                "http://10.200.201.105:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business",
//                "http://localhost:18888/channel-transpond-scan/business"};
//
//        long start = System.currentTimeMillis();
//        try {
//            int count = 300;
//            int pagecount = urisToGet.length;
//            ExecutorService executors = Executors.newFixedThreadPool(count);
//            CountDownLatch countDownLatch = new CountDownLatch(count);
//            for (int i = 0; i < count; i++) {
//                // 启动线程抓取
//                executors.execute(new GetRunnable("http://10.200.201.105:18888/channel-transpond-scan/business", countDownLatch));
////                executors.execute(new GetRunnable("http://localhost:18888/channel-transpond-scan/business", countDownLatch));
//            }
//            countDownLatch.await();
//            executors.shutdown();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            System.out.println("线程" + Thread.currentThread().getName() + ","
//                    + System.currentTimeMillis() + ", 所有线程已完成，开始进入下一步！");
//        }
//
//        long end = System.currentTimeMillis();
//        System.out.println("consume -> " + (end - start));
    }

//    static class GetRunnable implements Runnable {
//        private CountDownLatch countDownLatch;
//        private String url;
//
//        public GetRunnable(String url, CountDownLatch countDownLatch) {
//            this.url = url;
//            this.countDownLatch = countDownLatch;
//        }
//
//        @Override
//        public void run() {
//            try {
//                Map<String,Object> dataMap = new HashMap<>();
//                while(true) {
//                    dataMap.put("business","{\"amount\":\"1\",\"merId\":\"15173712561\",\"notifyUrl\":\"baidu.com\",\"orderId\":\""+UUID.randomUUID().toString().replaceAll("-","")+"\",\"payType\":\"1\"}");
//                    System.out.println(HttpClientUtil.post(url, dataMap) + "====================================");
//                }
//            } catch (Exception e){
//                System.out.println(e);
//            }
//            finally {
//                countDownLatch.countDown();
//            }
//        }
//    }
}