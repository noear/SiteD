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

    public DdNode(SdSource source){
        super(source);
    }

    @Override
    public void OnDidInit() {
        showWeb = attrs.getInt("showWeb", 1) > 0;
        screen  = attrs.getString("screen");
        loop    = attrs.getInt("loop", 0) > 0;

        //只应用于login节点
        check       = attrs.getString("check");
        isAutoCheck = attrs.getInt("auto") > 0;


        String w = attrs.getString("w");
        if (TextUtils.isEmpty(w) == false) {
            String h = attrs.getString("h");
            WHp = Float.parseFloat(w) / Float.parseFloat(h);
        }
    }


    //是否有分页
    public boolean hasPaging(){
        return hasMacro() || TextUtils.isEmpty(buildUrl)==false || "post".equals(method);
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

}
