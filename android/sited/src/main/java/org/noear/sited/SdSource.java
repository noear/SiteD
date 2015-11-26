package org.noear.sited;

import android.app.Application;
import android.text.TextUtils;

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yuety on 15/8/2.
 */
public class SdSource {

    public final String sds; //插件平台服务
    public final String expr;//匹配源的表达式

    public final boolean isDebug;//是否为调试模式
    public final boolean isPrivate;//是否为私密型插件

    public final int dtype;//数据类型

    public final String url_md5;
    public final String url;  //源首页
    public final Integer ver; //版本号

    public final String title; //标题
    public final String intro; //介绍
    public final String alert; //提醒（打开时跳出）
    public final String logo;  //图标

    public final SdNodeSet main;//源main节点

    protected final String encode;//编码
    private String _cookies;
    public String cookies(){
        return _cookies;
    }

    public void setCookies(String cookies){
        _cookies = cookies;
    }

    private SdJscript jscript;
    private String _ua;

    public String ua() {
        if (TextUtils.isEmpty(_ua))
            return Util.defUA;
        else
            return _ua;
    }

    public void delCache(String key){
        Util.cache.delete(key);
    }

    //给Util调用
    public static SdLogListener logListener;

    //解析
    public SdSource(Application app, String xml) throws Exception {

        Util.tryInitCache(app.getApplicationContext());

        main = new SdNodeSet(this);

        Element root = Util.getXmlroot(xml);

        ver = Integer.parseInt(root.getAttribute("ver"));
        sds = root.getAttribute("sds");
        isDebug   = "1".equals(root.getAttribute("debug"));
        isPrivate = "1".equals(root.getAttribute("private"));

        expr = Util.getElement(root, "expr").getTextContent();
        url = Util.getElement(root, "url").getTextContent();
        url_md5   = Util.md5(url);

        title = Util.getElement(root, "title").getTextContent();
        intro = Util.getElement(root, "intro").getTextContent();
        Element xAlert = Util.getElement(root, "alert");
        if(xAlert!=null) {
            alert = xAlert.getTextContent();
        }else{
            alert = null;
        }


        logo = Util.getElement(root, "logo").getTextContent();
        encode = Util.getElement(root, "encode").getTextContent();
        _ua = Util.getElement(root, "ua").getTextContent();

        jscript = new SdJscript(this,Util.getElement(root, "jscript"));

        Element xMain = Util.getElement(root, "main");
        dtype = Integer.parseInt(xMain.getAttribute("dtype"));
        main.loadByElement(xMain);

        js = new JsEngine(app);
        jscript.loadJs(js);
    }

    private JsEngine js;//不能作为属性

    //
    //------------
    //
    public boolean isMe(String url) {

        Pattern pattern = Pattern.compile(expr);
        Matcher m = pattern.matcher(url);

        return m.find();
    }

    public boolean isNode(SdNode node, String url){
        if(TextUtils.isEmpty(node.expr)==false){
            Pattern pattern = Pattern.compile(node.expr);
            Matcher m = pattern.matcher(url);

            return m.find();
        }else {
            return false;
        }
    }

    public String buildKey(SdNode config, String url) {
        if (TextUtils.isEmpty(config.buildKey))
            return url;
        else
            return js.callJs(this,config.buildKey, url);
    }

    public String buildWeb(SdNode config, String url) {
        if (TextUtils.isEmpty(config.buildWeb))
            return url;
        else
            return js.callJs(this,config.buildWeb, url);
    }

    public String buildUrl(SdNode config, String url) {
        if (TextUtils.isEmpty(config.buildUrl))
            return url;
        else
            return js.callJs(this,config.buildUrl, url);
    }

    public String buildUrl(SdNode config, String url, Integer page) {
        if (TextUtils.isEmpty(config.buildUrl))
            return url;
        else
            return js.callJs(this, config.buildUrl, url, page + "");
    }

    public String buildReferer(SdNode config, String url) {
        if (TextUtils.isEmpty(config.buildRef))
            return url;
        else
            return js.callJs(this,config.buildRef, url);
    }

    public String parse(SdNode config, String url, String html) {
        if("@null".equals(config.parse)) //如果是@null，说明不需要通过js解析
            return html;
        else
            return js.callJs(this, config.parse, url, html);
    }

    protected String parseUrl(SdNode config, String url, String html){
        return js.callJs(this, config.parseUrl, url, html);
    }


    //
    //---------------------------------------
    //
    public void getNodeViewModel(ISdViewModel viewModel, SdNodeSet nodeSet, boolean isUpdate, SdSourceCallback callback) {
        __AsyncTag tag = new __AsyncTag();

        for (ISdNode node : nodeSet.nodes()) {
            SdNode n = (SdNode) node;
            doGetNodeViewModel(viewModel, isUpdate, tag, n.url, null, n, callback);
        }

        if (tag.total == 0) {
            callback.run(1);
        }
    }

    public void getNodeViewModel(ISdViewModel viewModel, boolean isUpdate, String key, int page, SdNode config, SdSourceCallback callback) {
        page += config.addPage; //加上增减量

        String newUrl = buildUrl(config, config.url, page);

        if (key != null && TextUtils.isEmpty(config.addKey) == false) {//如果有补充关键字
            key = key + " " + config.addKey;
        }

        Map<String, String> args = null;
        if ("post".equals(config.method)) {
            args = new HashMap<String, String>();

            if (TextUtils.isEmpty(config.args) == false) {
                for (String kv : config.args.split(";")) {
                    if (kv.length() > 3) {
                        String name = kv.split("=")[0];
                        String value = kv.split("=")[1];

                        if (value.equals("@key"))
                            args.put(name, key);
                        else if (value.equals("@page"))
                            args.put(name, page + "");
                        else
                            args.put(name, value);
                    }
                }
            }

        } else {
            newUrl = newUrl.replace("@page", page + "");
            if (key != null) newUrl = newUrl.replace("@key", Util.urlEncode(key, config));
        }

        final String newUrl0 = newUrl;
        final Map<String, String> args0 = args;

        Util.http(this, isUpdate, newUrl, args0, config, (code, text) -> {
            if (code == 1) {

                if (TextUtils.isEmpty(config.parseUrl) == false) { //url需要解析出来
                    String newUrl2 = parseUrl(config, newUrl0, text);

                    Util.http(this, isUpdate, newUrl2, args0, config, (code2, text2) -> {
                        if (code2 == 1) {
                            doParse_noAddin(viewModel, config, newUrl2, text2);
                        }
                        callback.run(code);
                    });
                    return;//下面的代码被停掉
                }

                doParse_noAddin(viewModel, config, newUrl0, text);
            }

            callback.run(code);
        });
    }

    private void doParse_noAddin(ISdViewModel viewModel,SdNode config,String url,String text) {
        String json = this.parse(config, url, text);
        if (isDebug) {
            Util.log(this, config, url, json);
        }

        if (json != null) {
            viewModel.loadByJson(config, json);
        }
    }


    public void getNodeViewModel(ISdViewModel viewModel, boolean isUpdate, int page, SdNode config, SdSourceCallback callback) {
        getNodeViewModel(viewModel, isUpdate, null, page, config, callback);
    }

    public void getNodeViewModel(ISdViewModel viewModel, boolean isUpdate,  String url, SdNode config, SdSourceCallback callback) {
        //需要对url进行转换成最新的格式（可能之前的旧的格式缓存）

        __AsyncTag tag = new __AsyncTag();

        doGetNodeViewModel(viewModel, isUpdate, tag, url, null, config, callback);

        if (tag.total == 0) {
            callback.run(1);
        }
    }

    private void doGetNodeViewModel(ISdViewModel viewModel, boolean isUpdate, final __AsyncTag tag,  String url, Map<String, String> args, SdNode config, SdSourceCallback callback) {
        //需要对url进行转换成最新的格式（可能之前的旧的格式缓存）

        if (config.isEmpty())
            return;

        if (config.hasItems()) {
            viewModel.loadByConfig(config);
            return;
        }

        if (TextUtils.isEmpty(config.parse)) //没有解析的不处理
            return;

        //------------
        if (TextUtils.isEmpty(url)) //url为空的不处理
            return;

        {
            //2.获取主内容
            tag.total++;
            String newUrl = buildUrl(config, url);

            //有缓存的话，可能会变成同步了
            Util.http(this, isUpdate, newUrl, args, config, (code, text) -> {
                if (code == 1) {

                    if (TextUtils.isEmpty(config.parseUrl) == false) { //url需要解析出来
                        String newUrl2 = parseUrl(config, newUrl, text);

                        Util.http(this, isUpdate, newUrl2, args, config, (code2, text2) -> {
                            if (code2 == 1) {
                                doParse_hasAddin(viewModel, config, newUrl2, text2);
                            }

                            tag.value++;
                            if (tag.total == tag.value) {
                                callback.run(code);
                            }
                        });
                        return;//下面的代码被停掉
                    }

                    doParse_hasAddin(viewModel, config, newUrl, text);
                }

                tag.value++;
                if (tag.total == tag.value) {
                    callback.run(code);
                }
            });
        }

        if (config.hasAdds()) {
            //2.2 获取副内容（可能有多个）
            for (SdNode n1 : config.adds()) {
                if (TextUtils.isEmpty(n1.buildUrl))
                    continue;

                tag.total++;
                String newUrl = buildUrl(n1, url); //有url的add //add 不能有自己的独立url

                Util.http(this, isUpdate, newUrl, args, n1, (code, text) -> {
                    if (code == 1) {
                        String json = this.parse(n1, newUrl, text);
                        if (isDebug) {
                            Util.log(this, n1, newUrl, json);
                        }

                        if (json != null) {
                            viewModel.loadByJson(n1, json);
                        }
                    }

                    tag.value++;
                    if (tag.total == tag.value) {
                        callback.run(code);
                    }
                });
            }
        }
    }

    private void doParse_hasAddin(ISdViewModel viewModel, SdNode config, String url, String text){
        String json = this.parse(config, url, text);

        if(isDebug) {
            Util.log(this,config, url, json);
        }

        if (json != null) {
            viewModel.loadByJson(config, json);

            if (config.hasAdds()) { //没有url的add
                for (SdNode n1 : config.adds()) {
                    if (TextUtils.isEmpty(n1.buildUrl) == false)
                        continue;

                    String json2 = this.parse(n1, url, text);
                    if(isDebug) {
                        Util.log(this,n1, url, json2);
                    }

                    if (json2 != null)
                        viewModel.loadByJson(n1, json2);
                }
            }
        }
    }
}
