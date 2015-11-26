package org.noear.sited;

import android.text.TextUtils;
import android.util.Log;

import org.w3c.dom.Element;

/**
 * Created by yuety on 15/8/26.
 */
public class SdJscript {
    public final SdNode require;
    public final String code;
    public final SdSource s;

    protected SdJscript(SdSource source, Element node) {
        s = source;

        if(node == null){
            code = "";
            require = new SdNode(source,null);
        }else {
            code = Util.getElement(node, "code").getTextContent();
            require = new SdNode(source, Util.getElement(node, "require"));
        }

    }

    public void loadJs(JsEngine js) {
        if (require.isEmpty() == false) {
            for (SdNode n1 : require.items()) {

                Log.v("SdJscript", n1.url);

                n1.cache = 1; //长久缓存js文件
                Util.http(s, false, n1.url, null, n1, (code, text) -> {
                    if (code == 1) {
                        js.loadJs(s, text);
                    }
                });
            }
        }

        if(TextUtils.isEmpty(code) == false) {
            js.loadJs(s, code);
        }
    }
}
