package org.noear.ddcat.dao.engine;

import org.noear.sited.SdNodeSet;
import org.noear.sited.SdSource;

/**
 * Created by yuety on 16/2/1.
 */
public class DdNodeSet extends SdNodeSet {
    private int _dtype=0;
    public  int dtype() {
        if (_dtype > 0)
            return _dtype;
        else
            return 1;
    }//数据类型

    public  String durl;//数据url（url是给外面看的；durl是真实的地址）
    public  boolean showWeb;

    public DdSource s(){
        return (DdSource)source;
    }

    public DdNodeSet(SdSource source) {
        super(source);
    }

    @Override
    public void OnDidInit() {
        showWeb = attrs.getInt("showWeb", 1) > 0;
        _dtype  = attrs.getInt("dtype");
        durl    = attrs.getString("durl", source.url);
    }
}
