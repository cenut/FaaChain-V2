package com.faa.utils.utilswt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpClientTools {

    /**
     * 非common包下的httpclient post请求
     * 
     * @param uri
     * @param params
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     * @throws Exception
     */
    public static String sendPost(String uri, Map<String, String> params) {
        List<NameValuePair> nvp = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            nvp.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        String str = null;// 返回的json数据
        HttpPost post = new HttpPost(uri);// HttpPost对象
        // setEntity
        try {
            post.setEntity(new UrlEncodedFormEntity(nvp, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        // 发送post请求
        org.apache.http.client.HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("http.socket.timeout", 30000); // 为HttpClient设置参数
        post.getParams().setParameter("http.socket.timeout", 30000); // 为HttpMethod设置参数

        try {
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String returnJson = EntityUtils.toString(response.getEntity()); // 鑾峰彇鎺ュ彛璋冪敤杩斿洖缁撴灉
                return returnJson;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 非common包下的httpclient post请求
     * 
     * @param uri
     * @param params
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     * @throws Exception
     */
    public static String sendPost(String uri, List<NameValuePair> params) {
        return sendPost(uri, params, true);
    }

    /**
     * 非common包下的httpclient post请求
     * 
     * @param uri
     * @param params
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     * @throws Exception
     */
    public static String sendPost(String uri, List<NameValuePair> params, Boolean encode) {
        String str = null;// 返回的json数据
        HttpPost post = new HttpPost(uri);// HttpPost对象
        // setEntity
        try {
            post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        // 发送post请求
        org.apache.http.client.HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("http.socket.timeout", 30000); // 为HttpClient设置参数
        post.getParams().setParameter("http.socket.timeout", 30000); // 为HttpMethod设置参数

        try {
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String returnJson;
                if (encode) {
                    returnJson = new String(EntityUtils.toString(response.getEntity()).getBytes("ISO-8859-1"), "UTF-8"); // 鑾峰彇鎺ュ彛璋冪敤杩斿洖缁撴灉
                } else {
                    returnJson = EntityUtils.toString(response.getEntity());
                }
                return returnJson;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

//    public static String sendPost(String uri, byte[] data) {
//        return sendPost(uri, data, true);
//    }

    public static String sendPost(String uri, byte[] data, boolean encode) throws IOException {
        HttpPost post = new HttpPost(uri);// HttpPost对象
        post.setEntity(new ByteArrayEntity(data));
        // 发送post请求
        org.apache.http.client.HttpClient client = new DefaultHttpClient();
        
        client.getParams().setParameter("http.socket.timeout", 30000); // 为HttpClient设置参数
        post.getParams().setParameter("http.socket.timeout", 30000); // 为HttpMethod设置参数
        post.setHeader("Content-Type", "application/json");  
        
        HttpResponse response = client.execute(post);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String returnJson;
            if (encode) returnJson = new String(EntityUtils.toString(response.getEntity()).getBytes("ISO-8859-1"),
                                                "UTF-8");
            else returnJson = EntityUtils.toString(response.getEntity());
            return returnJson;
        } else {
            return null;
        }
    }
    
    public static String sendPostJson(String uri, String json){
    	HttpPost post = new HttpPost(uri);// HttpPost对象
        StringEntity s = new StringEntity(json,"utf-8");  
        s.setContentEncoding(HTTP.UTF_8);  
        s.setContentType("application/json");    
//        s.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));   
        post.setEntity(s);   
        // 发送post请求
        org.apache.http.client.HttpClient client = new DefaultHttpClient();
        post.setHeader("Content-Type", "application/json");  
        
        
        client.getParams().setParameter("http.socket.timeout", 30000); // 为HttpClient设置参数
        post.getParams().setParameter("http.socket.timeout", 30000); // 为HttpMethod设置参数
        try {
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String returnJson;
                returnJson = EntityUtils.toString(response.getEntity());
                return returnJson;
            } else {
                return null;
            }
        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
    }

    /**
     * 非common包下的httpclient post请求
     * 
     * @param uri
     * @param params
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     * @throws Exception
     */
    public static String sendGet(String uri) {
        String str = null;// 返回的json数据
        HttpGet get = new HttpGet(uri);// HttpPost对象
        // 发送post请求
        org.apache.http.client.HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("http.socket.timeout", 30000); // 为HttpClient设置参数
        get.getParams().setParameter("http.socket.timeout", 30000); // 为HttpMethod设置参数

        try {
            HttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String returnJson = new String(EntityUtils.toString(response.getEntity()).getBytes("ISO-8859-1"),
                                               "UTF-8"); // 鑾峰彇鎺ュ彛璋冪敤杩斿洖缁撴灉
                return returnJson;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

}
