package org.noear.sited;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.impl.client.DefaultRedirectHandler;
import cz.msebera.android.httpclient.protocol.HttpContext;


/**
 * Created by yuety on 15/8/21.
 */
class Util {
    protected static final String NEXT_CALL = "CALL::";
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

    protected static void http(SdSource source, boolean isUpdate, HttpMessage msg) {

        log(source, "Util.http", msg.url);

        String cacheKey2 = null;
        String args = "";
        if (msg.form == null)
            cacheKey2 = msg.url;
        else {
            StringBuilder sb = new StringBuilder();
            sb.append(msg.url);
            for (String key : msg.form.keySet()) {
                sb.append(key).append("=").append(msg.form.get(key)).append(";");
            }
            cacheKey2 = sb.toString();
            args = cacheKey2;
        }
        final String cacheKey = cacheKey2;


        __CacheBlock block = cache.get(cacheKey);

        if (isUpdate == false && msg.config.cache > 0) {
            if (block != null && block.isOuttime(msg.config) == false) {
                final __CacheBlock block1 = block;

                new Handler().postDelayed(() -> {
                    log(source, "Util.incache.url", msg.url);
                    msg.callback.run(1, msg, block1.value, null);
                }, 100);
                return;
            }
        }

        doHttp(source, msg, block, (code, msg2, data, url302) -> {
            if (code == 1) {
                cache.save(cacheKey, data);
            }

            msg.callback.run(code, msg2, data, url302);
        });

        source.DoTraceUrl(msg.url, args, msg.config);
    }

    private static void doHttp(SdSource source, HttpMessage msg, __CacheBlock cache, HttpCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);

        client.setUserAgent(msg.ua);
        client.setURLEncodingEnabled(msg.url.indexOf(" ") > 0);

        for (String key : msg.header.keySet()){
            client.addHeader(key, msg.header.get(key));
        }

        __AsyncTag httpTag = new __AsyncTag();

        TextHttpResponseHandler responseHandler = new TextHttpResponseHandler(msg.encode) {

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String s, Throwable throwable) {

                Util.log(source,"http.onFailure",throwable.getMessage(),throwable);

                if (cache == null || cache.value == null)
                    callback.run(-2, msg, null, null);
                else
                    callback.run(1, msg, cache.value, httpTag.str0);
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String s) {
                for (Header h1 : headers) {
                    if ("Set-Cookie".equals(h1.getName())) {
                        source.setCookies(h1.getValue());
                        break;
                    }
                }

                callback.run(1, msg, s,httpTag.str0);
            }
        };


        client.setEnableRedirects(true);
        client.setRedirectHandler(new DefaultRedirectHandler(){
            @Override
            public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 302 || statusCode == 301) {
                    httpTag.str0 = response.getFirstHeader("Location").getValue();

                    if (httpTag.str0.startsWith("http") == false) {
                        Uri uri = Uri.parse(msg.url);
                        httpTag.str0 = uri.getScheme() + "://" + uri.getHost() + httpTag.str0;
                    }

                    Log.v("orgurl", msg.url);
                    Log.v("302url", httpTag.str0);
                }

                return super.isRedirectRequested(response, context);
            }
        });


        try {
            int idx = msg.url.indexOf('#'); //去除hash，即#.*
            String url2 = null;
            if (idx > 0)
                url2 = msg.url.substring(0, idx);
            else
                url2 = msg.url;

            if ("post".equals(msg.method)) {
                RequestParams postData = new RequestParams(msg.form);
                postData.setContentEncoding(msg.encode);

                client.post(url2, postData, responseHandler);
            } else {
                client.get(url2, responseHandler);
            }
        } catch (Exception ex) {
            if (cache == null)
                callback.run(-2, msg, null, null);
            else
                callback.run(1, msg, cache.value, null);
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
