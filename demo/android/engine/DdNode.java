package org.noear.ddcat.dao.engine;

import android.text.TextUtils;

import org.noear.sited.SdNode;
import org.noear.sited.SdSource;

/**
 * Created by yuety on 16/2/1.
 */
public class DdNode extends SdNode {

    public DdSource s(){
        return (DdSource)source;
    }

    //临时数据寄存（任意）
    public int dataTag;

    public String showImg;

    //是否显示S按钮
    public boolean showWeb=true;
    //屏幕方向（v/h）
    public String screen;
    //首页图片显示的宽高比例
    public float WHp = 0;
    //是否循环播放
    public boolean loop = false;

    //只应用于login节点
    protected String check;
    protected boolean isAutoCheck = true;

    public String mail;
    public int style;

    public static final int STYLE_VIDEO = 11;
    public static final int STYLE_AUDIO = 12;

    public DdNode(SdSource source){
        super(source);
    }

    @Override
    public void OnDidInit() {
        showImg = attrs.getString("showImg");
        showWeb = attrs.getInt("showWeb", 1) > 0;
        screen  = attrs.getString("screen");
        loop    = attrs.getInt("loop", 0) > 0;

        //只应用于login节点
        check = attrs.getString("check");
        isAutoCheck = attrs.getInt("auto") > 0;

        mail  = attrs.getString("mail");

        style = attrs.getInt("style", STYLE_VIDEO);

        if(TextUtils.isEmpty(screen) && style == STYLE_AUDIO) {
            screen = "v";
        }

        String w = attrs.getString("w");
        if (TextUtils.isEmpty(w) == false) {
            String h = attrs.getString("h");
            WHp = Float.parseFloat(w) / Float.parseFloat(h);
        }
    }




    //是否内部WEB运行
    public boolean isWebrun(){
        String run = attrs.getString("run");

        if(run==null)
            return false;

        return run.indexOf("web")>=0;
    }

    //是否外部WEB运行
    public boolean isOutWebrun(){
        String run = attrs.getString("run");

        if(run==null)
            return false;

        return run.indexOf("outweb")>=0;
    }

    public String getWebUrl(String url) {
        if (attrs.contains("buildWeb")==false)
            return url;
        else
            return source.callJs(this, "buildWeb", url);
    }
}
