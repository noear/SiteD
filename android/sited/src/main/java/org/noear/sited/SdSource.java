package org.noear.sited;

import android.app.Application;
import android.text.TextUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yuety on 15/8/2.
 */
public class SdSource {

    public final SdAttributeList attrs = new SdAttributeList();

    public final boolean isDebug;//是否为调试模式

    public final String url_md5;
    public final String url;  //源首页
    public final String title; //标题
    public final String expr;//匹配源的表达式

    private final String _encode;//编码
    public String encode(){return _encode;}

    private final String _ua;
    public String ua(){if(TextUtils.isEmpty(_ua)){return Util.defUA;}else{ return _ua;}}

    private String _cookies;
    public String cookies(){
        return _cookies;
    }
    public void setCookies(String cookies){
        _cookies = cookies;
    }

    public void delCache(String key){
        Util.cache.delete(key);
    }
    //-------------------------------

    private final JsEngine js;//不能作为属性
    protected final SdJscript jscript;

    //
    //--------------------------------
    //
    private Element _root = null; //使用一次后不置为null
    public SdSource(Application app, String xml) throws Exception {

        Util.tryInitCache(app.getApplicationContext());

        Element root = Util.getXmlroot(xml);
        _root = root;

        {
            NamedNodeMap temp = root.getAttributes();
            for (int i = 0, len = temp.getLength(); i < len; i++) {
                Node p = temp.item(i);
                attrs.set(p.getNodeName(), p.getNodeValue());
            }
        }

        {
            NodeList temp = root.getChildNodes();
            for (int i = 0, len = temp.getLength(); i < len; i++) {
                Node p = temp.item(i);

                if (p.getNodeType() == Node.ELEMENT_NODE && p.hasAttributes() == false &&  p.hasChildNodes()) {
                    Node p2 = p.getFirstChild();
                    if(p2.getNodeType() == Node.TEXT_NODE) {
                        attrs.set(p.getNodeName(), p2.getNodeValue());
                    }
                }
            }
        }


        isDebug = attrs.getInt("debug") > 0;

        title   = attrs.getString("title");
        expr    = attrs.getString("expr");
        url     = attrs.getString("url");
        url_md5 = Util.md5(url);

        _encode = attrs.getString("encode");
        _ua     = attrs.getString("ua");

        js = new JsEngine(app, this);
        jscript = new SdJscript(this, Util.getElement(root, "jscript"));
        jscript.loadJs(app, js);

        OnDidInit();
    }

    protected SdNodeSet getBody(String name){
        SdNodeSet temp = Util.createNodeSet(this);
        temp.buildForNode(Util.getElement(_root, name));

        _root = null;

        return temp;
    }

    public void OnDidInit(){

    }


    //
    //------------
    //
    public boolean isMatch(String url) {
        Pattern pattern = Pattern.compile(expr);
        Matcher m = pattern.matcher(url);

        return m.find();
    }


    public String callJs(SdNode config, String funAttr, String... args) {
        return js.callJs(config.attrs.getString(funAttr), args);
    }
    //-------------

    public String buildArgs(SdNode config, String url, String key, int page) {
        if (TextUtils.isEmpty(config.buildArgs))
            return config.args;
        else
            return js.callJs(config.buildArgs, url, key, page + "", config.jsTag);
    }

    public String buildArgs(SdNode config, String url, int page) {
        if (TextUtils.isEmpty(config.buildArgs))
            return config.args;
        else
            return js.callJs(config.buildArgs, url, page + "", config.jsTag);
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

    public String buildUrl(SdNode config,  String url, String key, Integer page) {
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

            String _strArgs = null;
            if (key == null)
                _strArgs = buildArgs(config, url, page);
            else
                _strArgs = buildArgs(config, url, key, page);

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
                                List<String> jsonList = new ArrayList<String>();
                                for (Integer i = 1; i <= tag.total; i++) { //安排序加载内容
                                    if (dataList.containsKey(i)) {
                                        jsonList.add(dataList.get(i));
                                    }
                                }

                                String[] strAry = new String[jsonList.size()];
                                jsonList.toArray(strAry);
                                viewModel.loadByJson(config, strAry);

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
                                    List<String> jsonList = new ArrayList<String>();
                                    for (Integer i = 1; i <= tag.total; i++) { //安排序加载内容
                                        if (dataList.containsKey(i)) {
                                            jsonList.add(dataList.get(i));
                                        }
                                    }
                                    String[] strAry = new String[jsonList.size()];
                                    jsonList.toArray(strAry);
                                    viewModel.loadByJson(config, strAry);
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

    private void doParse_noAddin(ISdViewModel viewModel,SdNode config,String url,String text) {
        String json = this.parse(config, url, text);
        if (isDebug) {
            Util.log(this, config, url, json);
        }

        if (json != null) {
            viewModel.loadByJson(config, json);
        }
    }

    private void doParse_hasAddin(ISdViewModel viewModel, SdNode config, String url, String text) {
        String json = this.parse(config, url, text);

        if (isDebug) {
            Util.log(this, config, url, json);
        }

        if (json != null) {
            viewModel.loadByJson(config, json);

            if (config.hasAdds()) { //没有url的add
                for (SdNode n2 : config.adds()) {
                    if (TextUtils.isEmpty(n2.buildUrl) == false)
                        continue;

                    String json2 = this.parse(n2, url, text);
                    if (isDebug) {
                        Util.log(this, n2, url, json2);
                    }

                    if (json2 != null) {
                        viewModel.loadByJson(n2, json2);
                    }
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
