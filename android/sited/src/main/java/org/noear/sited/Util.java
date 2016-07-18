package org.noear.sited;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.StringReader;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;


/**
 * Created by yuety on 15/8/21.
 */
class Util {
    protected static final String defUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240";

    protected static __ICache cache = null;

    protected static void tryInitCache(Context context) {
        if (cache == null) {
            cache = new __FileCache(context, "sited");
        }
    }

    protected static Element getElement(Element n, String tag) {
        NodeList temp = n.getElementsByTagName(tag);
        if (temp.getLength() > 0)
            return (Element) (temp.item(0));
        else
            return null;
    }

    protected static Element getXmlroot(String xml) throws Exception {
        StringReader sr = new StringReader(xml);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dombuild = factory.newDocumentBuilder();

        return dombuild.parse(new InputSource(sr)).getDocumentElement();
    }

    //
    //----------------------------
    //

    protected static String urlEncode(String str, SdNode config) {
        try {
            return URLEncoder.encode(str, config.encode());
        } catch (Exception ex) {
            return "";
        }
    }

    protected static void http(SdSource source, boolean isUpdate, String url, Map<String, String> params, int tag, SdNode config, HttpCallback callback) {

        log(source,"Util.http", url);

        __CacheBlock block = null;

        String cacheKey2 = null;
        if (params == null)
            cacheKey2 = url;
        else {
            StringBuilder sb = new StringBuilder();
            sb.append(url);
            for (String key : params.keySet()) {
                sb.append(key).append("=").append(params.get(key)).append(";");
            }
            cacheKey2 = sb.toString();
        }
        final String cacheKey = cacheKey2;


        if (isUpdate == false && config.cache > 0) {
            block = cache.get(cacheKey);
        }

        if (block != null) {
            if (config.cache == 1 || block.seconds() <= config.cache) {
                final __CacheBlock block1 = block;

                new Handler().postDelayed(() -> {
                    log(source,"Util.incache.url", url);
                    callback.run(1, tag, block1.value);
                }, 100);
                return;
            }
        }

        doHttp(source, url, params, tag, config, block, (code, tag2, data) -> {
            if (code == 1 && config.cache > 0) {
                cache.save(cacheKey, data);
            }

            callback.run(code, tag2, data);
        });
    }

    private static void doHttp(SdSource source, String url, Map<String, String> params, int tag, SdNode config, __CacheBlock cache, HttpCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setUserAgent(config.ua());
        client.setURLEncodingEnabled(url.indexOf(" ") > 0);

        if (config.isInCookie()) {
            String cookies = config.cookies(url);
            if (cookies != null) {
                client.addHeader("Cookie", cookies);
            }
        }

        if (config.isInReferer()) {
            client.addHeader("Referer", source.buildReferer(config, url));
        }

        if (TextUtils.isEmpty(config.header) == false) {
            for (String kv : config.header.split(";")) {
                String[] kv2 = kv.split("=");
                if (kv2.length == 2) {
                    client.addHeader(kv2[0], kv2[1]);
                }
            }
        }

        TextHttpResponseHandler responseHandler = new TextHttpResponseHandler(config.encode()) {

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String s, Throwable throwable) {
                if (cache == null)
                    callback.run(-2, tag, null);
                else
                    callback.run(1, tag, cache.value);
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String s) {
                for (Header h1 : headers) {
                    if ("Set-Cookie".equals(h1.getName())) {
                        source.setCookies(h1.getValue());
                        break;
                    }
                }

                callback.run(1, tag, s);
            }
        };


        try {
            int idx = url.indexOf('#'); //去除hash，即#.*
            String url2 = null;
            if (idx > 0)
                url2 = url.substring(0, idx);
            else
                url2 = url;

            if ("post".equals(config.method)) {
                RequestParams postData = new RequestParams(params);
                postData.setContentEncoding(config.encode());

                client.post(url2, postData, responseHandler);
            } else {
                client.get(url2, responseHandler);
            }
        } catch (Exception ex) {
            if (cache == null)
                callback.run(-2, tag, null);
            else
                callback.run(1, tag, cache.value);
        }
    }

    /*生成MD5值*/
    public static String md5(String code) {

        String s = null;

        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            byte[] code_byts = code.getBytes("UTF-8");

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(code_byts);
            byte tmp[] = md.digest();          // MD5 的计算结果是一个 128 位的长整数，
            // 用字节表示就是 16 个字节
            char str[] = new char[16 * 2];   // 每个字节用 16 进制表示的话，使用两个字符，
            // 所以表示成 16 进制需要 32 个字符
            int k = 0;                                // 表示转换结果中对应的字符位置
            for (int i = 0; i < 16; i++) {          // 从第一个字节开始，对 MD5 的每一个字节
                // 转换成 16 进制字符的转换
                byte byte0 = tmp[i];                 // 取第 i 个字节
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];  // 取字节中高 4 位的数字转换,
                // >>> 为逻辑右移，将符号位一起右移
                str[k++] = hexDigits[byte0 & 0xf];            // 取字节中低 4 位的数字转换
            }
            s = new String(str);                                 // 换后的结果转换为字符串

        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    //
    //--------------------------------
    //

    public static void log(SdSource source, SdNode node, String url, String json) {
        if (url == null)
            log(source, node.name, "url=null");
        else
            log(source, node.name, url);

        if (json == null)
            log(source, node.name, "json=null");
        else
            log(source, node.name, json);
    }

    public static void log(SdSource source, String tag, String msg) {
        Log.v(tag, msg);

        if (SdApi._listener != null) {
            SdApi._listener.run(source, tag, msg, null);
        }
    }

    public static void log(SdSource source, String tag, String msg, Throwable tr) {
        Log.v(tag, msg, tr);

        if (SdApi._listener != null) {
            SdApi._listener.run(source, tag, msg, tr);
        }
    }

    //-------------
    //
    public static SdNode createNode(SdSource source) {
        return SdApi._factory.createNode(source);
    }

    public static SdNodeSet createNodeSet(SdSource source) {
        return SdApi._factory.createNodeSet(source);
    }
}
