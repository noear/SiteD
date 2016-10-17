package org.noear.ddcat.dao.engine;

import android.app.AlertDialog;
import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import org.noear.ddcat.Navigation;
import org.noear.ddcat.controller.ActivityBase;
import org.noear.ddcat.dao.Session;
import org.noear.ddcat.dao.db.DbApi;
import org.noear.ddcat.dao.db.SiteDbApi;
import org.noear.sited.ISdNode;
import org.noear.sited.SdApi;
import org.noear.sited.SdNode;
import org.noear.sited.SdNodeSet;
import org.noear.sited.SdSource;

import java.util.HashMap;

import me.noear.exts.Act1;
import me.noear.utils.HttpUtil;
import noear.snacks.ONode;

/**
 * Created by yuety on 15/8/3.
 */
public class DdSource extends SdSource {
    public final int ver; //版本号
    public final int engine;//引擎版本号
    public final String sds; //插件平台服务
    public final boolean isPrivate;//是否为私密型插件
    public final int vip;


    public final String logo;  //图标
    public final String author;
    public final String contact;
    public final String alert; //提醒（打开时跳出）
    public final String intro; //介绍
    //---------------------------------------------------
    public final DdNode reward;
    //---------------------------------------------------
    public final DdNodeSet meta;
    public final DdNodeSet main;

    public final DdNode hots;
    public final DdNode updates;
    public final DdNode search;
    public final DdNode tags;
    public final DdNodeSet home;

    private  ISdNode _tag;
    private  ISdNode _book;
    private  ISdNode _section;
    private  ISdNode _object;

    public DdNode tag(String url){
        Log.v("tag.selct::",url);
        return  (DdNode)_tag.nodeMatch(url);
    }
    public DdNode book(String url){
        Log.v("book.selct::",url);
        return  (DdNode)_book.nodeMatch(url);
    }
    public DdNode section(String url){
        Log.v("section.selct::",url);
        return  (DdNode)_section.nodeMatch(url);
    }

    public DdNode object1(String url){
        Log.v("object.selct::",url);

        return  (DdNode)_object.nodeMatch(url);
    }

    public final DdNode login;

    private final String trace_url;

    public String sited;

    public DdSource(Application app, String xml) throws Exception {
        super();

        if (xml.startsWith("sited::")) {
            int start = xml.indexOf("::") + 2;
            int end = xml.lastIndexOf("::");
            String txt = xml.substring(start, end);
            String key = xml.substring(end + 2);
            xml = DdApi.unsuan(txt, key);
        }

        sited = xml;

        doInit(app, xml);
        if(schema>0){
            xmlHeadName = "meta";
            xmlBodyName = "main";
            xmlScriptName = "script";
        }else {
            xmlHeadName = "meta";
            xmlBodyName = "main";
            xmlScriptName = "jscript";
        }
        doLoad(app);

        meta = (DdNodeSet) head;
        main = (DdNodeSet) body;


        //--------------

        sds = head.attrs.getString("sds");
        isPrivate = head.attrs.getInt("private") > 0;
        engine = head.attrs.getInt("engine");
        ver = head.attrs.getInt("ver");
        vip = head.attrs.getInt("vip");

        author = head.attrs.getString("author");
        contact = head.attrs.getString("contact");

        intro = head.attrs.getString("intro");
        logo = head.attrs.getString("logo");

        if (engine > DdApi.version())
            alert = "此插件需要更高版本引擎支持，否则会出错。建议升级！";
        else
            alert = head.attrs.getString("alert");

        //
        //---------------------
        //

        trace_url = main.attrs.getString("trace");

        home = (DdNodeSet) main.get("home");
        {
            hots = (DdNode) home.get("hots");
            updates = (DdNode) home.get("updates");
            tags = (DdNode) home.get("tags");
        }

        search = (DdNode) main.get("search");

        _tag = main.get("tag");
        _book = main.get("book");
        _section = main.get("section");
        _object = main.get("object");

        if (_object.isEmpty()) {
            if (_section.isEmpty())
                _object = _book;
            else
                _object = _section;
        }


        if(schema>0) {
            login = (DdNode) head.get("login");//登录
            reward = (DdNode) head.get("reward");//打赏
        }else{
            login = (DdNode) main.get("login");//登录
            reward = (DdNode) main.get("reward");//打赏
        }

        //-----------
        ONode json = new ONode();
        json.set("ver", DdApi.version());
        json.set("udid", Session.udid());

        json.set("uid", Session.userID);
        json.set("usex", Session.sex);
        json.set("uvip", Session.isVip);
        json.set("ulevel", Session.level);

        String jsCode = "SiteD=" + json.toJson() + ";";

        loadJs(jsCode);
    }

    private String _FullTitle;
    public String fullTitle() {
        if (_FullTitle == null) {
            if(isPrivate){
                _FullTitle = title;
            }else {
                int idx = url.indexOf('?');
                if (idx < 0)
                    _FullTitle = title + " (" + url + ")";
                else
                    _FullTitle = title + " (" + url.substring(0, idx) + ")";
            }
        }

        return _FullTitle;
    }

    public String webUrl(){
        if(TextUtils.isEmpty(main.durl))
            return url;
        else
            return main.durl;
    }

    @Override
    public void setCookies(String cookies) {
        if (cookies == null)
            return;

        Log.v("cookies", cookies);

        if (DoCheck("", cookies, false)) {
            super.setCookies(cookies);
            SiteDbApi.setSourceCookies(this);
        }
    }

    @Override
    public String cookies() {
        if (TextUtils.isEmpty(_cookies)) {
            _cookies = SiteDbApi.getSourceCookies(this);
        }

        return _cookies;
    }

    public boolean isLoggedIn(String url, String cookies) {
        return DoCheck(url, cookies, false);
    }

    @Override
    protected boolean DoCheck(String url, String cookies, boolean isFromAuto) {
        if(login.isEmpty()){
            return true;
        }else {

            if(TextUtils.isEmpty(login.check)){
                return true;
            }else {
                if (url == null || cookies == null)
                    return false;

                if(isFromAuto){
                    if(login.isAutoCheck){
                        String temp = callJs(login, "check", url, cookies);
                        return temp.equals("1");
                    }else{
                        return true;//如果不支持自动,则总是返回ok
                    }
                }
                else {
                    String temp = callJs(login, "check", url, cookies);
                    return "1".equals(temp);
                }
            }
        }
    }

    @Override
    protected void DoTraceUrl(String url, String args, SdNode config) {
        if (TextUtils.isEmpty(trace_url) == false) {
            if (TextUtils.isEmpty(url) == false) {
                try {
                    HashMap<String, String> data = new HashMap<>();
                    data.put("_uid", Session.userID + "");
                    data.put("_uname", Session.nickname);
                    data.put("_days", Session.dayNum + "");
                    data.put("_vip", Session.isVip + "");

                    data.put("url", url);
                    data.put("args", args);
                    data.put("node", config.name);

                    HttpUtil.post(trace_url, data, (code, text) -> {
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public synchronized void tryLogin(ActivityBase activity, boolean forUser) {
        if (login.isEmpty())
            return;

        doLogin(activity);
    }

    private void doLogin(ActivityBase activity){
        if(login.isWebrun()) {
            String loginUrl = login.getUrl(login.url);
            Navigation.showWebAddinLogin(activity, this, loginUrl);
        }else{

        }
    }

    public static boolean isHots(SdNode node){
        return "hots".equals(node.name);
    }

    public static boolean isUpdates(SdNode node){
        return "updates".equals(node.name);
    }

    public static boolean isTags(SdNode node){
        return "tags".equals(node.name);
    }

    public static boolean isBook(SdNode node){
        return "book".equals(node.name);
    }


    //
    //--------------------------
    //
    private boolean _isAlerted = false;
    public boolean tryAlert(ActivityBase activity,Act1<Boolean> callback) {
        if (TextUtils.isEmpty(alert))
            return false;
        else {
            if (_isAlerted == false) {
                new AlertDialog.Builder(activity)
                        .setTitle("提示")
                        .setMessage(alert)
                        .setNegativeButton("退出", (d, w) -> {
                            _isAlerted = false;
                            d.dismiss();
                            callback.run(false);
                        })
                        .setPositiveButton("继续", (d, w) -> {
                            _isAlerted = true;
                            d.dismiss();
                            callback.run(true);
                        }).setCancelable(false).show();
            }

            return true;
        }
    }
}
