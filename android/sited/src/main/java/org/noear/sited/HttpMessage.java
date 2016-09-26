package org.noear.sited;

import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuety on 16/9/9.
 */

public class HttpMessage {
    public Map<String, String> header = new HashMap<>();
    public Map<String, String> form = new HashMap<>();
    public String url;

    public int tag;

    public HttpCallback callback;

    public SdNode config;


    //可由cfg实始化
    public String encode;
    public String ua;
    public String method;

    public HttpMessage() {

    }


    public HttpMessage(SdNode cfg, String url, int tag, Map<String, String> args) {
        this.config = cfg;
        this.url = url;
        this.tag = tag;

        if (args != null) {
            form = args;
        }

        rebuild(null);
    }

    public HttpMessage(SdNode cfg, String url) {
        this.config = cfg;
        this.url = url;

        rebuild(null);
    }

    public void rebuild(SdNode cfg) {
        if (cfg != null) {
            this.config = cfg;
        }

        ua = config.ua();
        encode = config.encode();
        method = config.method;


        if (config.isInCookie()) {
            String cookies = config.cookies(url);
            if (cookies != null) {
                header.put("Cookie", cookies);
            }
        }

        if (config.isInReferer()) {
            header.put("Referer", config.getReferer(url));
        }

        if (config.isEmptyHeader() == false) {
            for (String kv : config.getHeader(url).split(";")) {
                String[] kv2 = kv.split("=");
                if (kv2.length == 2) {
                    header.put(kv2[0], kv2[1]);
                }
            }
        }
    }


    public void rebuildForm() {
        rebuildForm(0, null);
    }

    public void rebuildForm(int page, String key) {
        if ("post".equals(config.method)) {
            String _strArgs = null;
            if (key != null) {
                config.getArgs(url, key, page);
            } else {
                config.getArgs(url, page);
            }

            Log.v("Post.Args", _strArgs);

            if (TextUtils.isEmpty(_strArgs) == false) {
                for (String kv : _strArgs.split(";")) {
                    if (kv.length() > 3) {
                        String name = kv.split("=")[0];
                        String value = kv.split("=")[1];

                        if (value.equals("@key"))
                            form.put(name, key);
                        else if (value.equals("@page"))
                            form.put(name, page + "");
                        else
                            form.put(name, value);
                    }
                }
            }

        }
    }
}
