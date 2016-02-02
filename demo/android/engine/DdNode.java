package org.noear.ddcat.dao.engine;

import android.text.TextUtils;

import org.noear.sited.SdNode;
import org.noear.sited.SdSource;

/**
 * Created by yuety on 16/2/1.
 */
public class DdNode extends SdNode {
    private  String trySuffix;
    private int _dtype;
    public int dtype(){
        if(_dtype>0)
            return _dtype;
        else
            return s().main.dtype();
    }

    public boolean showWeb=true;
    public String screen;
    //宽高比例
    public float WHp = 0;

    public  DdNode(SdSource source){
        super(source);
    }

    public DdSource s(){
        return (DdSource)source;
    }

    @Override
    public void OnDidInit() {

        _dtype   = attrs.getInt("dtype");
        showWeb = attrs.getInt("showWeb", 1) > 0;
        screen  = attrs.getString("screen");

        {
            String temp = attrs.getString("cache");
            if(TextUtils.isEmpty(temp)==false){
                int len = temp.length();
                if(len==1){
                    cache = Integer.parseInt(temp);
                }else if(len>1){
                    cache = Integer.parseInt(temp.substring(0,len-1));

                    String p = temp.substring(len-1);
                    switch (p){
                        case "d":cache=cache*24*60*60;break;
                        case "h":cache=cache*60*60;break;
                        case "m":cache=cache*60;break;
                    }
                }
            }
        }

        String w = attrs.getString("w");
        if(TextUtils.isEmpty(w)==false){
            String h = attrs.getString("h");
            WHp = Float.parseFloat(w)/Float.parseFloat(h);
        }
    }


    public  String[] getSuffixUrl(String url) {
        if(trySuffix == null)
            trySuffix = attrs.getString("trySuffix");

        if (TextUtils.isEmpty(trySuffix) || TextUtils.isEmpty(url))
            return new String[]{url};
        else {
            String[] exts = trySuffix.split("\\|");
            String[] urls = new String[exts.length];
            for (int i=0,len=exts.length; i<len; i++) {
                urls[i] = url.replaceAll(trySuffix, exts[i]);
            }
            return urls;
        }
    }

    public boolean isWebrun(){
        String run = attrs.getString("run");

        if(run==null)
            return false;

        return run.indexOf("web")>=0;
    }

    public boolean isOutWebrun(){
        String run = attrs.getString("run");

        if(run==null)
            return false;

        return run.indexOf("outweb")>=0;
    }

    public String getWebOnloadCode(){
        return attrs.getString("web_onload");
    }
}
