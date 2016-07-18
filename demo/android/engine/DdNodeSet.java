package org.noear.ddcat.dao.engine;

import org.noear.sited.SdNodeSet;
import org.noear.sited.SdSource;

/**
 * Created by yuety on 16/2/1.
 */
public class DdNodeSet extends SdNodeSet {


    public DdSource s(){
        return (DdSource)source;
    }



    public  String durl;//数据url（url是给外面看的；durl是真实的地址）
    public  boolean showWeb;

    public DdNodeSet(SdSource source) {
        super(source);
    }


    @Override
    public void OnDidInit() {
        showWeb = attrs.getInt("showWeb", 1) > 0;
        durl    = attrs.getString("durl", source.url);
    }
}
