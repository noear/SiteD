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
    public final String durl;//数据url（url是给外面看的；durl是真实的地址）

    public final int engine;

    public final String url_md5;
    public final String url;  //源首页
    public final Integer ver; //版本号

    public final String author;
    public final String title; //标题
    public final String intro; //介绍
    public final String alert; //提醒（打开时跳出）
    public final String logo;  //图标

    public final SdNodeSet main;//源main节点

    public final boolean showWeb;

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

        ver = Util.parseInt(root.getAttribute("ver"));
        engine = Util.parseInt(root.getAttribute("engine"));

        sds = root.getAttribute("sds");
        isDebug = "1".equals(root.getAttribute("debug"));
        isPrivate = "1".equals(root.getAttribute("private"));

        expr = Util.getElementVal(root, "expr");
        url = Util.getElementVal(root, "url");
        url_md5 = Util.md5(url);


        author = Util.getElementVal(root, "author");
        title = Util.getElementVal(root, "title");
        intro = Util.getElementVal(root, "intro");

        if (engine > SdApi.version)
            alert = "此插件需要更高版本引擎支持，否则会出错。建议升级！";
        else
            alert = Util.getElementVal(root, "alert");

        logo = Util.getElementVal(root, "logo");
        encode = Util.getElementVal(root, "encode");
        _ua = Util.getElementVal(root, "ua");

        jscript = new SdJscript(this, Util.getElement(root, "jscript"));

        Element xMain = Util.getElement(root, "main");
        showWeb = ("0".equals(xMain.getAttribute("showWeb"))) == false;
        dtype = Integer.parseInt(xMain.getAttribute("dtype"));
        String temp = xMain.getAttribute("durl");
        if (TextUtils.isEmpty(temp))
            durl = url;
        else
            durl = temp;

        main.loadByElement(xMain);

        js = new JsEngine(app, this);
        jscript.loadJs(app, js);
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

    public String buildArgs(SdNode config, String url, String key, int page) {
        if (TextUtils.isEmpty(config.buildArgs))
            return config.args;
        else
            return js.callJs(config.buildArgs, url, key, page + "", config.jsTag);
    }

    public String buildWeb(SdNode config, String url) {
        if (TextUtils.isEmpty(config.buildWeb))
            return url;
        else
            return js.callJs(config.buildWeb, url, config.jsTag);
    }

    public String buildUrl(SdNode config, String url) {
        if (TextUtils.isEmpty(config.buildUrl))
            return url;
        else
            return js.callJs(config.buildUrl, url, config.jsTag);
    }

    public String buildUrl(SdNode config, String url, Integer page) {
        if (TextUtils.isEmpty(config.buildUrl))
            return url;
        else {
            return js.callJs(config.buildUrl, url, page + "", config.jsTag);
        }
    }

    public String buildUrl(SdNode config, String url, String key, Integer page) {
        if (TextUtils.isEmpty(config.buildUrl))
            return url;
        else {
            return js.callJs(config.buildUrl, url, key, page + "", config.jsTag);
        }
    }

    public String buildReferer(SdNode config, String url) {
        if (TextUtils.isEmpty(config.buildRef))
            return url;
        else
            return js.callJs(config.buildRef, url, config.jsTag);
    }

    public String parse(SdNode config, String url, String html) {
        if("@null".equals(config.parse)) //如果是@null，说明不需要通过js解析
            return html;
        else
            return js.callJs(config.parse, url, html, config.jsTag);
    }

    protected String parseUrl(SdNode config, String url, String html){
        return js.callJs(config.parseUrl, url, html, config.jsTag);
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

        if (key != null && TextUtils.isEmpty(config.addKey) == false) {//如果有补充关键字
            key = key + " " + config.addKey;
        }

        String newUrl = null;
        if(key == null)
            newUrl = buildUrl(config, config.url, page);
        else
            newUrl = buildUrl(config, config.url, key, page);



        Map<String, String> args = null;
        if ("post".equals(config.method)) {
            args = new HashMap<String, String>();

            String _strArgs = buildArgs(config,url,key,page);

            if (TextUtils.isEmpty(_strArgs) == false) {
                for (String kv : _strArgs.split(";")) {
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

        Util.http(this, isUpdate, newUrl, args0, 0, config, (code, t, text) -> {
            if (code == 1) {

                if (TextUtils.isEmpty(config.parseUrl) == false) { //url需要解析出来(多个用;隔开)
                    String[] newUrls = parseUrl(config, newUrl0, text).split(";");
                    Map<Integer, String> dataList = new HashMap<>();//如果有多个地址，需要排序
                    __AsyncTag tag = new __AsyncTag();

                    for (String newUrl2 : newUrls) {
                        tag.total++;
                        Util.http(this, isUpdate, newUrl2, args0, tag.total, config, (code2, t2, text2) -> {
                            if (code2 == 1) {
                                doParse_noAddinForTmp(dataList, config, newUrl2, text2, t2);
                            }

                            tag.value++;
                            if (tag.total == tag.value) {
                                for (Integer i = 1; i <= tag.total; i++) { //安排序加载内容
                                    if (dataList.containsKey(i)) {
                                        viewModel.loadByJson(config, dataList.get(i));
                                    }
                                }

                                callback.run(code);
                            }
                        });
                    }
                    return;//下面的代码被停掉
                }

                doParse_noAddin(viewModel, config, newUrl0, text);
            }

            callback.run(code);
        });
    }


    public void getNodeViewModel(ISdViewModel viewModel, boolean isUpdate, int page, SdNode config, SdSourceCallback callback) {
        getNodeViewModel(viewModel, isUpdate, null, page, config, callback);
    }

    public void getNodeViewModel(ISdViewModel viewModel, boolean isUpdate, String url, SdNode config, SdSourceCallback callback) {
        //需要对url进行转换成最新的格式（可能之前的旧的格式缓存）

        __AsyncTag tag = new __AsyncTag();

        doGetNodeViewModel(viewModel, isUpdate, tag, url, null, config, callback);

        if (tag.total == 0) {
            callback.run(1);
        }
    }

    private void doGetNodeViewModel(ISdViewModel viewModel, boolean isUpdate, final __AsyncTag tag, String url, Map<String, String> args, SdNode config, SdSourceCallback callback) {
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
            Util.http(this, isUpdate, newUrl, args, 0, config, (code, t, text) -> {
                if (code == 1) {

                    if (TextUtils.isEmpty(config.parseUrl) == false) { //url需要解析出来(多个用;隔开)
                        String[] newUrls = parseUrl(config, newUrl, text).split(";");
                        Map<Integer,String> dataList = new HashMap<>();//如果有多个地址，需要排序

                        tag.total--;//抵消之前的++
                        for (String newUrl2 : newUrls) {
                            tag.total++;
                            Util.http(this, isUpdate, newUrl2, args, tag.total, config, (code2, t2, text2) -> {
                                if (code2 == 1) {
                                    doParse_noAddinForTmp(dataList, config, newUrl2, text2,t2);
                                }

                                tag.value++;
                                if (tag.total == tag.value) {
                                    for (Integer i = 1; i <= tag.total; i++) { //安排序加载内容
                                        if (dataList.containsKey(i)) {
                                            viewModel.loadByJson(config, dataList.get(i));
                                        }
                                    }
                                    callback.run(code);
                                }
                            });
                        }
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

                Util.http(this, isUpdate, newUrl, args, 0, n1, (code, t, text) -> {
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

    private void doParse_noAddin(ISdViewModel viewModel, SdNode config, String url, String text) {
        String json = this.parse(config, url, text);
        if (isDebug) {
            Util.log(this, config, url, json);
        }

        if (json != null) {
            viewModel.loadByJson(config, json);
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

    private void doParse_noAddinForTmp(Map<Integer,String> dataList, SdNode config, String url, String text, int tag){
        String json = this.parse(config, url, text);

        if(isDebug) {
            Util.log(this,config, url, json);
        }

        if (json != null) {
            dataList.put(tag,json);
        }
    }
}
