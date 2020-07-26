package com.zh;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.WriterException;
import com.zh.utils.HttpClientUtil;
import com.zh.utils.QRCodeGeneratorUtil;
import org.apache.http.cookie.Cookie;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Main.class);
        final String generateCodeURL = "https://passport.douyu.com/scan/generateCode";
        final Map<String, String> param = new HashMap<>();
        param.put("client_id", "7");
        JSONObject generateCodeResult = (JSONObject)HttpClientUtil.httpPostForm(generateCodeURL, param).get("JSONObject");
        logger.info(generateCodeResult);

        String data = generateCodeResult.getString("data");
        JSONObject jsonData = JSON.parseObject(data);
        String loginUrl = jsonData.getString("url");
        String code = jsonData.getString("code");

        final String QR_CODE_IMAGE_PATH = "src\\main\\java\\com\\zh\\QRCodes\\MyQRCode.png";
        try {
            QRCodeGeneratorUtil.generateQRCodeImage(loginUrl, 350, 350, QR_CODE_IMAGE_PATH);
        } catch (WriterException e) {
            System.out.println("Could not generate QR Code, WriterException :: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Could not generate QR Code, IOException :: " + e.getMessage());
        }

        Timer timer = new Timer();
        long startTimeMillis = System.currentTimeMillis();
        long endTimeMillis = startTimeMillis + 5 * 60 * 1000;
        TimerTask timerTask = new TimerTask() {
            public void run() {
                String verifyUrl = "https://passport.douyu.com/lapi/passport/qrcode/check?time=" + System.currentTimeMillis() + "&code=" + code;
                JSONObject verifyResult = (JSONObject) HttpClientUtil.httpGet(verifyUrl).get("JSONObject");
                String verifyResultError = verifyResult.getString("error");
                logger.info(verifyResult);
                if (System.currentTimeMillis() > endTimeMillis || "-1".equals(verifyResultError)) {
                    timer.cancel();
                    logger.info("二维码不存在或者是已经过期。" + "\n起始时间戳：" + startTimeMillis + "\n终止时间戳：" + endTimeMillis);
                }
                if ("0".equals(verifyResultError)) {
                    timer.cancel();
                    logger.info("扫码成功。");
                    String verifyResultData = verifyResult.getString("data");
                    JSONObject verifySuccessData = JSON.parseObject(verifyResultData);
                    String getTokenUrl = verifySuccessData.getString("url");

                    //获取cookies
                    Map<String, Object> resultMap = HttpClientUtil.httpGet("https:" + getTokenUrl);
                    List<Cookie> cookies = (List<Cookie>) resultMap.get("cookies");
                    for (Cookie cookie : cookies) {
                        try {
                            String name = URLDecoder.decode(cookie.getName(), "UTF-8");
                            String value = URLDecoder.decode(cookie.getValue(), "UTF-8");
                            logger.info(name + "=" + value);
                        } catch (UnsupportedEncodingException e) {
                            logger.error("cookies解码失败。",e);
                        }
                    }
                }
            }
        };
        timer.schedule(timerTask, 1000, 1000);
    }
}
