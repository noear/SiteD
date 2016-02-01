package org.noear.ddcat.dao.engine;

import android.app.AlertDialog;
import android.app.Application;
import android.text.TextUtils;

import org.noear.ddcat.controller.ActivityBase;
import org.noear.ddcat.dao.db.SiteDbApi;
import org.noear.sited.SdApi;
import org.noear.sited.SdNode;
import org.noear.sited.SdNodeSet;
import org.noear.sited.SdSource;

import me.noear.exts.Act1;

/**
 * Created by yuety on 15/8/3.
 */
public class DdSource extends SdSource {
    public final int ver; //版本号
    public final  int engine;//引擎版本号
    public final  String sds; //插件平台服务
    public final  boolean isPrivate;//是否为私密型插件

    public final  String logo;  //图标
    public final  String author;
    public final  String alert; //提醒（打开时跳出）
    public final  String intro; //介绍
    //---------------------------------------------------
    public final DdNodeSet main;
    public final  DdNode hots;
    public final  DdNode updates;
    public final  DdNode search;
    public final  DdNode tags;
    public final  SdNodeSet home;

    public final  DdNode tag;
    public final  DdNode book;
    public final  DdNode section;


    public DdSource(Application app, String xml) throws Exception {
        super(app, xml);

        main = (DdNodeSet)_main;

        home = (DdNodeSet) main.get("home");

        hots = (DdNode) home.get("hots");
        updates = (DdNode) home.get("updates");
        tags = (DdNode) home.get("tags");

        search = (DdNode) main.get("search");

        book = (DdNode) main.get("book");
        section = (DdNode) main.get("section");

        DdNode temp = (DdNode) main.get("tag");
        if (temp.isEmpty()) //旧版本
            tag = tags;
        else
            tag = temp; //新版本增加的:tags负责获取tag列表；tag负责获取解析tag.url的数据
        //
        //---------------------
        //
        sds = attrs.getString("sds");
        isPrivate = attrs.getInt("private") > 0;
        engine = attrs.getInt("engine");
        ver = attrs.getInt("ver");

        author = attrs.getString("author");
        intro = attrs.getString("intro");
        logo = attrs.getString("logo");

        if (engine > DdApi.version)
            alert = "此插件需要更高版本引擎支持，否则会出错。建议升级！";
        else
            alert = attrs.getString("alert");
    }

    private String _FullTitle;
    public String fullTitle() {
        if (_FullTitle == null) {
            int idx = url.indexOf('?');
            if (idx < 0)
                _FullTitle = title + " (" + url + ")";
            else
                _FullTitle = title + " (" + url.substring(0, idx) + ")";
        }

        return _FullTitle;
    }

    @Override
    public void setCookies(String cookies) {
        super.setCookies(cookies);

        SiteDbApi.setSourceCookies(this);
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
}
