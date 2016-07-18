package org.noear.ddcat.dao.engine;

import android.app.AlertDialog;
import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import org.noear.ddcat.Navigation;
import org.noear.ddcat.controller.ActivityBase;
import org.noear.ddcat.dao.db.SiteDbApi;
import org.noear.sited.ISdNode;
import org.noear.sited.SdApi;
import org.noear.sited.SdNode;
import org.noear.sited.SdNodeSet;
import org.noear.sited.SdSource;

import java.util.HashMap;

import me.noear.exts.Act1;
import me.noear.utils.HttpUtil;

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
    public final String alert; //提醒（打开时跳出）
    public final String intro; //介绍
    //---------------------------------------------------
    public final DdNodeSet main;
    public final DdNode hots;
    public final DdNode updates;
    public final DdNode search;
    public final DdNode tags;
    public final SdNodeSet home;

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

    public DdNode object(String url){
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

        doInit(app, xml, "main");

        sds       = attrs.getString("sds");
        isPrivate = attrs.getInt("private") > 0;
        engine    = attrs.getInt("engine");
        ver       = attrs.getInt("ver");
        vip       = attrs.getInt("vip");

        author    = attrs.getString("author");
        intro     = attrs.getString("intro");
        logo      = attrs.getString("logo");

        if (engine > DdApi.version)
            alert = "此插件需要更高版本引擎支持，否则会出错。建议升级！";
        else
            alert = attrs.getString("alert");

        //
        //---------------------
        //

        main = (DdNodeSet) body;
        trace_url = main.attrs.getString("trace");


        home = (DdNodeSet) main.get("home");

        hots = (DdNode) home.get("hots");
        updates = (DdNode) home.get("updates");
        tags = (DdNode) home.get("tags");

        search = (DdNode) main.get("search");

        _tag = main.get("tag");
        _book = main.get("book");
        _section = main.get("section");
        _object = main.get("object");

        if(_object.isEmpty()){
            if(_section.isEmpty())
                _object = _book;
            else
                _object = _section;
        }


        login = (DdNode) main.get("login");


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

    @Override
    public void setCookies(String cookies) {
        super.setCookies(cookies);

        SiteDbApi.setSourceCookies(this);
    }

    @Override
    protected boolean DoCheck(String url, String html, String cookies) {
        if(login.isEmpty()){
            return true;
        }else {
            String temp = callJs(login, "check", url, html, cookies);

            return temp.equals("1");
        }
    }

    public void tryLogin(ActivityBase activity, boolean isMust){
        if(login.isEmpty())
            return;

        if(isMust){
            login.dataTag=0;
            doLogin(activity);
        }else {
            if (login.dataTag == 0) {
                login.dataTag = 1;
                doLogin(activity);
            }
        }
    }

    private void doLogin(ActivityBase activity){
        if(login.isWebrun()) {
            Navigation.showWebOnly(activity, login.url);
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

    public String buildWeb(SdNode config,String url) {
        if (config.attrs.contains("buildWeb")==false)
            return url;
        else
            return callJs(config, "buildWeb", url, config.jsTag);
    }

    public void traceUrl(String url ,SdNode confg) {
        if (TextUtils.isEmpty(trace_url) == false) {
            HashMap<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("node", confg.name);

            HttpUtil.post(trace_url, data, (code, text) -> {
            });
        }
    }
}
