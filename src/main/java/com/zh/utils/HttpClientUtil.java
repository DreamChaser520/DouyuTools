package com.zh.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * HttpClient4.3工具类
 */

public class HttpClientUtil {

    private static RequestConfig requestConfig = null;
    private static String charset = "utf-8";


    static {
        // 设置请求和传输超时时间
        requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
    }


    /**
     * @param url
     * @param obj 1. json字符串   2. map  3.JSONObject
     * @return JSONObject
     */
    public static Map<String,Object> httpPost(String url, Object obj) {
        Logger logger = Logger.getLogger(HttpClientUtil.class);
        // 存储json结果和cookies
        Map<String,Object> result= new HashMap<>();

        // post请求返回结果
        BasicCookieStore basicCookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(basicCookieStore).build();
        HttpPost httpPost = new HttpPost(url);
        try {
            if (null != obj) {
                StringEntity entity = null;
                if (obj instanceof String) {
                    entity = new StringEntity(obj.toString(), charset);
                } else {
                    entity = new StringEntity(JSON.toJSONString(obj), charset);
                }
                entity.setContentEncoding(charset);
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }
            CloseableHttpResponse response = httpClient.execute(httpPost);
            List<Cookie> cookies = basicCookieStore.getCookies();
            result.put("cookies",cookies);
            result.put("JSONObject",convertResponse(response));
            return result;
        } catch (Exception e) {
            logger.error("post请求提交失败:" + url, e);
        } finally {
            httpPost.releaseConnection();
        }
        return null;
    }


    /**
     * post请求传输String参数 例如：name=Jack&sex=1&type=2
     * Content-type:application/x-www-form-urlencoded
     *
     * @param url url地址
     * @param
     * @return
     */
    public static Map<String,Object> httpPostForm(String url, Map<String, String> params) {
        Logger logger = Logger.getLogger(HttpClientUtil.class);
        // 存储json结果和cookies
        Map<String,Object> result= new HashMap<>();

        // post请求返回结果
        BasicCookieStore basicCookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(basicCookieStore).build();
        HttpPost httpPost = new HttpPost(url);

        try {
            if (null != params) {
                //组织请求参数
                List<NameValuePair> paramList = new ArrayList<NameValuePair>();
                if (params != null && params.size() > 0) {
                    Set<String> keySet = params.keySet();
                    for (String key : keySet) {
                        paramList.add(new BasicNameValuePair(key, params.get(key)));
                    }
                }
                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(paramList, charset);
                //设置参数格式
                httpPost.setHeader("referer", "https://passport.douyu.com/member/login");
                httpPost.setEntity(urlEncodedFormEntity);
            }
            CloseableHttpResponse response = httpClient.execute(httpPost);
            List<Cookie> cookies = basicCookieStore.getCookies();
            result.put("cookies",cookies);
            result.put("JSONObject",convertResponse(response));
            return result;
        } catch (IOException e) {
            logger.error("postForm请求提交失败:" + url, e);
        } finally {
            httpPost.releaseConnection();
        }
        return null;
    }

    /**
     * 发送get请求
     *
     * @param url 路径
     * @return
     */
    public static Map<String,Object> httpGet(String url) {
        Logger logger = Logger.getLogger(HttpClientUtil.class);
        // 存储json结果和cookies
        Map<String,Object> result= new HashMap<>();
        // get请求返回结果
        BasicCookieStore basicCookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(basicCookieStore).build();
        // 发送get请求
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("referer", "https://passport.douyu.com/member/login");
        try {
            CloseableHttpResponse response = httpClient.execute(httpget);
            List<Cookie> cookies = basicCookieStore.getCookies();
            result.put("cookies",cookies);
            result.put("JSONObject",convertResponse(response));
            return result;
        } catch (Exception e) {
            logger.error("get请求提交失败:" + url, e);
        } finally {
            httpget.releaseConnection();
        }
        return null;
    }

    private static JSONObject convertResponse(CloseableHttpResponse response) throws IOException, ParseException {
        Logger logger = Logger.getLogger(HttpClientUtil.class);
        // 请求发送成功，并得到响应
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            // 读取服务器返回过来的json字符串数据
            HttpEntity entity = response.getEntity();
            String strResult = EntityUtils.toString(entity, "utf-8").replaceAll("\\(","").replaceAll("\\)","");
            // 把json字符串转换成json对象
            return JSONObject.parseObject(strResult);
        } else {
            logger.error("Json转换失败。");
        }
        return null;
    }
}