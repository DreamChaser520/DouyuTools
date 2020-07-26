package com.zh.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
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
    private static String charset = "utf-8";

    /**
     * @param url
     * @param obj 1. json字符串   2. map  3.JSONObject
     * @return JSONObject
     */
    public static Map<String, Object> httpPost(String url, Object obj, Map<String, String> header) {
        Logger logger = Logger.getLogger(HttpClientUtil.class);
        // 存储json结果和cookies
        Map<String, Object> result = new HashMap<>();

        // post请求返回结果
        BasicCookieStore basicCookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(basicCookieStore).build();
        HttpPost httpPost = new HttpPost(url);
        // 设置头部参数
        if (header != null && header.size() > 0) {
            Set<String> keySet = header.keySet();
            for (String key : keySet) {
                httpPost.setHeader(key, header.get(key));
            }
        }
        // 发送Post请求
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
            result.put("cookies", cookies);
            result.put("JSONObject", convertResponse(response));
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
    public static Map<String, Object> httpPostForm(String url, Map<String, String> params, Map<String, String> header) {
        Logger logger = Logger.getLogger(HttpClientUtil.class);
        // 存储json结果和cookies
        Map<String, Object> result = new HashMap<>();

        // post请求返回结果
        BasicCookieStore basicCookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(basicCookieStore).build();
        HttpPost httpPost = new HttpPost(url);

        //设置头部参数
        if (header != null && header.size() > 0) {
            Set<String> keySet = header.keySet();
            for (String key : keySet) {
                httpPost.setHeader(key, header.get(key));
            }
        }
        // 发送Post请求
        try {
            if (null != params) {
                //组织请求参数
                List<NameValuePair> paramList = new ArrayList<>();
                if (params != null && params.size() > 0) {
                    Set<String> keySet = params.keySet();
                    for (String key : keySet) {
                        paramList.add(new BasicNameValuePair(key, params.get(key)));
                    }
                }
                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(paramList, charset);
                httpPost.setEntity(urlEncodedFormEntity);
            }
            CloseableHttpResponse response = httpClient.execute(httpPost);
            List<Cookie> cookies = basicCookieStore.getCookies();
            result.put("cookies", cookies);
            result.put("JSONObject", convertResponse(response));
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
    public static Map<String, Object> httpGet(String url, Map<String, String> header) {
        Logger logger = Logger.getLogger(HttpClientUtil.class);
        // 存储json结果和cookies
        Map<String, Object> result = new HashMap<>();
        // get请求返回结果
        BasicCookieStore basicCookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(basicCookieStore).build();
        // 设置头部参数
        HttpGet httpGet = new HttpGet(url);
        if (header != null && header.size() > 0) {
            Set<String> keySet = header.keySet();
            for (String key : keySet) {
                httpGet.setHeader(key, header.get(key));
            }
        }
        // 发送get请求
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            List<Cookie> cookies = basicCookieStore.getCookies();
            result.put("cookies", cookies);
            result.put("JSONObject", convertResponse(response));
            return result;
        } catch (Exception e) {
            logger.error("get请求提交失败:" + url, e);
        } finally {
            httpGet.releaseConnection();
        }
        return null;
    }

    private static JSONObject convertResponse(CloseableHttpResponse response) throws IOException, ParseException {
        Logger logger = Logger.getLogger(HttpClientUtil.class);
        // 请求发送成功，并得到响应
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            // 读取服务器返回过来的json字符串数据
            HttpEntity entity = response.getEntity();
            String strResult = EntityUtils.toString(entity, "utf-8").replaceAll("\\(", "").replaceAll("\\)", "");
            // 把json字符串转换成json对象
            return JSONObject.parseObject(strResult);
        } else {
            logger.error("Json转换失败。");
        }
        return null;
    }
}