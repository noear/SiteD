package org.noear.sited;

/**
 * Created by yuety on 15/12/19.
 */
public class SdApi {

    protected static SdNodeFactory _factory;
    protected static SdLogListener _listener;

    public static void tryInit(SdNodeFactory factory, SdLogListener listener) {
        _factory = factory;
        _listener = listener;
    }

    protected static void check() throws Exception {
        if (_factory == null || _listener == null) {
            throw new Exception("未初始化");
        }
    }
}
