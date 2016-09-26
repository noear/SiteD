package org.noear.sited;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

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

    public boolean isDebug;//是否为调试模式

    public String url_md5;
    public String url;  //源首页
    public String title; //标题
    public String expr;//匹配源的表达式

    private String _encode;//编码
    public String encode() {
        return _encode;
    }

    private String _ua;
    public String ua() {
        if (TextUtils.isEmpty(_ua)) {
            return Util.defUA;
        } else {
            return _ua;
        }
    }

    protected String _cookies;
    public String cookies() {
        return _cookies;
    }
    public void setCookies(String cookies) {
        _cookies = cookies;
    }

    public void delCache(String key) {
        Util.cache.delete(key);
    }
    //-------------------------------

    public SdNodeSet body;

    protected JsEngine js;//不能作为属性
    protected SdJscript jscript;

    //
    //--------------------------------
    //
    protected SdSource() {

    }

    public SdSource(Application app, String xml, String xmlBodyNodeName) throws Exception {
        doInit(app, xml, xmlBodyNodeName);
    }

    protected void doInit(Application app, String xml, String xmlBodyNodeName) throws Exception {

        Util.tryInitCache(app.getApplicationContext());

        Element root = Util.getXmlroot(xml);

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

                if (p.getNodeType() == Node.ELEMENT_NODE && p.hasAttributes() == false && p.hasChildNodes()) {
                    Node p2 = p.getFirstChild();
                    if (p2.getNodeType() == Node.TEXT_NODE) {
                        attrs.set(p.getNodeName(), p2.getNodeValue());
                    }
                }
            }
        }


        isDebug = attrs.getInt("debug") > 0;

        title = attrs.getString("title");
        expr = attrs.getString("expr");
        url = attrs.getString("url");
        url_md5 = Util.md5(url);

        _encode = attrs.getString("encode");
        _ua = attrs.getString("ua");

        body = Util.createNodeSet(this);
        body.buildForNode(Util.getElement(root, xmlBodyNodeName));

        js = new JsEngine(app, this);
        jscript = new SdJscript(this, Util.getElement(root, "jscript"));
        jscript.loadJs(app, js);

        OnDidInit();
    }

    protected boolean DoCheck(String url, String cookies, boolean isFromAuto) {
        return true;
    }

    protected void DoTraceUrl(String url, String args, SdNode config) {
    }

    ;

    public void OnDidInit() {

    }


    //
    //------------
    //
    public boolean isMatch(String url) {
        Pattern pattern = Pattern.compile(expr);
        Matcher m = pattern.matcher(url);

        return m.find();
    }

    public void loadJs(String jsCode){
        js.loadJs(jsCode);
    }

    public String callJs(SdNode config, String funAttr, String... args) {
        return js.callJs(config.attrs.getString(funAttr), args);
    }
    //-------------



    public String parse(SdNode config, String url, String html) {

        Log.v("parse", url);
        Log.v("parse", html == null ? "null" : html);

        if(TextUtils.isEmpty(config.parse)){
            return html;
        }

        if ("@null".equals(config.parse)) //如果是@null，说明不需要通过js解析
            return html;
        else {
            String temp = js.callJs(config.parse, url, html);

            if (temp == null) {
                Log.v("parse.rst", "null" + "\r\n\n");
            } else {
                Log.v("parse.rst", temp + "\r\n\n");
            }
            return temp;
        }
    }

    protected String parseUrl(SdNode config, String url, String html) {
        Log.v("parseUrl", url);
        Log.v("parseUrl", html == null ? "null" : html);

        return js.callJs(config.parseUrl, url, html);
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

        try {
            doGetNodeViewModel(viewModel, isUpdate, key, page, config, callback);
        }catch (Exception ex){
            callback.run(1);
        }
    }

    private void doGetNodeViewModel(ISdViewModel viewModel, boolean isUpdate, String key, int page, SdNode config, SdSourceCallback callback) {

        HttpMessage msg = new HttpMessage();

        page += config.addPage; //加上增减量

        if (key != null && TextUtils.isEmpty(config.addKey) == false) {//如果有补充关键字
            key = key + " " + config.addKey;
        }

        if (key == null)
            msg.url = config.getUrl(config.url, page);
        else
            msg.url = config.getUrl(config.url, key, page);

        if (TextUtils.isEmpty(msg.url)) {
            callback.run(-3);
            return;
        }

        msg.rebuild(config);

        if ("post".equals(config.method)) {
            msg.rebuildForm(page, key);
        } else {
            msg.url = msg.url.replace("@page", page + "");
            if (key != null) {
                msg.url = msg.url.replace("@key", Util.urlEncode(key, config));
            }
        }

        final int pageX = page;
        final String keyX = key;
        msg.callback = (code, sender, text, url302) -> {
            if (code == 1) {

                if(TextUtils.isEmpty(url302)) {
                    url302 = sender.url;
                }

                if (TextUtils.isEmpty(config.parseUrl) == false) { //url需要解析出来(多个用;隔开)
                    List<String> newUrls = new ArrayList<>();
                    String[] rstUrls = parseUrl(config, url302, text).split(";");

                    for(String url1 : rstUrls){
                        if(url1.length() == 0)
                            continue;

                        if(url1.startsWith(Util.NEXT_CALL)){
                            HttpMessage msg0 = new HttpMessage();
                            msg0.url = url1.replace(Util.NEXT_CALL,"")
                                           .replace("GET::","")
                                           .replace("POST::","");

                            msg0.rebuild(config);

                            if(url1.indexOf("POST::")>0){
                                msg0.method = "post";
                                msg0.rebuildForm(pageX, keyX);
                            }else{
                                msg0.method = "get";
                            }

                            msg0.callback = msg.callback;

                            Util.http(this, isUpdate, msg0);
                        }else{
                            newUrls.add(url1);
                        }
                    }

                    if(newUrls.size()>0) {
                        __AsyncTag tag = new __AsyncTag();
                        Map<Integer, String> dataList = new HashMap<>();//如果有多个地址，需要排序
                        doParseUrl_Aft(viewModel, config, isUpdate, newUrls, sender.form, tag, dataList, callback);
                    }

                    return;
                }
                else {
                    doParse_noAddin(viewModel, config, url302, text);
                }
            }

            callback.run(code);
        };


        Util.http(this, isUpdate, msg);
    }


    public void getNodeViewModel(ISdViewModel viewModel, boolean isUpdate, int page, String url, SdNode config, SdSourceCallback callback) {
        config.url = url;
        doGetNodeViewModel(viewModel, isUpdate, null, page, config, callback);
    }

    public void getNodeViewModel(ISdViewModel viewModel, boolean isUpdate, String url, SdNode config, SdSourceCallback callback) {
        //需要对url进行转换成最新的格式（可能之前的旧的格式缓存）

        try {
            if (DoCheck(url, cookies(), true) == false) {

                callback.run(99);
                return;
            }

            __AsyncTag tag = new __AsyncTag();

            doGetNodeViewModel(viewModel, isUpdate, tag, url, null, config, callback);

            if (tag.total == 0) {
                callback.run(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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

        if("@null".equals(config.method)) {
            viewModel.loadByJson(config, config.getUrl(url));
            return;
        }

        if (TextUtils.isEmpty(config.parse) == false && TextUtils.isEmpty(url) == false) {//如果没有url 和 parse，则不处理
            tag.total++;

            HttpMessage msg = new HttpMessage();
            if(args!=null) {
                msg.form = args;
            }

            //2.获取主内容
            msg.url = config.getUrl(url);
            msg.callback = (code, sender, text, url302) -> {
                if (code == 1) {

                    if(TextUtils.isEmpty(url302)) {
                        url302 = sender.url;
                    }

                    if (TextUtils.isEmpty(config.parseUrl) == false) { //url需要解析出来(多个用;隔开)
                        List<String> newUrls = new ArrayList<>();
                        String[] rstUrls = parseUrl(config, url302, text).split(";");

                        for(String url1 : rstUrls) {
                            if (url1.length() == 0)
                                continue;

                            if (url1.startsWith(Util.NEXT_CALL)) {
                                Util.log(this, "CALL::url=", url1);

                                HttpMessage msg0 = new HttpMessage();
                                msg0.url = url1.replace(Util.NEXT_CALL, "")
                                        .replace("GET::", "")
                                        .replace("POST::", "");

                                msg0.rebuild(config);

                                if (url1.indexOf("POST::") > 0) {
                                    msg0.method = "post";
                                    msg0.rebuildForm();
                                } else {
                                    msg0.method = "get";
                                }
                                msg0.callback = msg.callback;

                                Util.http(this, isUpdate, msg0);
                            } else {
                                newUrls.add(url1);
                            }
                        }

                        if(newUrls.size()>0) {
                            Map<Integer, String> dataList = new HashMap<>();//如果有多个地址，需要排序

                            tag.total--;//抵消之前的++
                            doParseUrl_Aft(viewModel, config, isUpdate, newUrls, args, tag, dataList, callback);
                        }
                        return;//下面的代码被停掉
                    }
                    else {
                        doParse_hasAddin(viewModel, config, url302, text);
                    }
                }

                tag.value++;
                if (tag.total == tag.value) {
                    callback.run(code);
                }
            };

            //有缓存的话，可能会变成同步了
            msg.rebuild(config);
            Util.http(this, isUpdate, msg);
        }

        if (config.hasAdds()) {
            //2.2 获取副内容（可能有多个）
            for (SdNode n1 : config.adds()) {
                if (n1.isEmptyUrl())
                    continue;

                tag.total++;
                HttpMessage msg = new HttpMessage();
                //如果自己有url，则使用自己的url；；如果没有，则通过父级的url进行buildUrl(url)
                msg.url = (TextUtils.isEmpty(n1.url) ? n1.getUrl(url) : n1.url);
                msg.callback = (code, sender, text, url302) -> {
                    if (code == 1) {
                        if(TextUtils.isEmpty(url302)) {
                            url302 = msg.url;
                        }

                        String json = this.parse(n1, url302, text);
                        if (isDebug) {
                            Util.log(this, n1, url302, json);
                        }

                        if (json != null) {
                            viewModel.loadByJson(n1, json);
                        }
                    }

                    tag.value++;
                    if (tag.total == tag.value) {
                        callback.run(code);
                    }
                };

                msg.rebuild(config);
                Util.http(this, isUpdate, msg);
            }
        }
    }

    private void doParseUrl_Aft(ISdViewModel viewModel, SdNode config, boolean isUpdate, List<String> newUrls, Map<String, String> args, __AsyncTag tag, Map<Integer, String> dataList, SdSourceCallback callback) {
        for (String newUrl2 : newUrls) {
            tag.total++;

            HttpMessage msg = new HttpMessage(config, newUrl2, tag.total, args);

            msg.callback = (code2, sender, text2, url302) -> {
                if (code2 == 1) {
                    if(TextUtils.isEmpty(url302)) {
                        url302 = newUrl2;
                    }

                    doParse_noAddinForTmp(dataList, config, url302, text2, sender.tag);
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

                    callback.run(code2);
                }
            };

            Util.http(this, isUpdate, msg);
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

    private void doParse_hasAddin(ISdViewModel viewModel, SdNode config, String url, String text) {
        String json = this.parse(config, url, text);

        if (isDebug) {
            Util.log(this, config, url, json);
        }

        if (json != null) {
            viewModel.loadByJson(config, json);

            if (config.hasAdds()) { //没有url的add
                for (SdNode n2 : config.adds()) {
                    if (n2.isEmptyUrl() == false)
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

    private void doParse_noAddinForTmp(Map<Integer, String> dataList, SdNode config, String url, String text, int tag) {
        String json = this.parse(config, url, text);

        if (isDebug) {
            Util.log(this, config, url, json);
        }

        if (json != null) {
            dataList.put(tag, json);
        }
    }
}
