package org.noear.sited;

import android.app.Application;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;

/**
 * Created by yuety on 15/8/2.
 */
class JsEngine {
    private V8 engine = null;

    protected JsEngine(Application app) {
        engine = V8.createV8Runtime(null, app.getApplicationInfo().dataDir);
    }

    public JsEngine loadJs(SdSource source, String funs) {

        try {
            engine.executeVoidScript(funs);//预加载了批函数
        } catch (Exception ex) {
            ex.printStackTrace();
            Util.log(source, "JsEngine.loadJs", ex.getMessage(), ex);
            throw ex;
        }

        return this;
    }

    //调用函数;可能传参数
    public String callJs(SdSource source, String fun, String... args) {
        V8Array params = new V8Array(engine);
        for (String p : args) {
            params.push(p);
        }

        try {
            return engine.executeStringFunction(fun, params);
        } catch (Exception ex) {
            ex.printStackTrace();
            Util.log(source, "JsEngine.callJs:" + fun, ex.getMessage(), ex);
            return null;
        }
    }
}
