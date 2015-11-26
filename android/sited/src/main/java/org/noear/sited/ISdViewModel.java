package org.noear.sited;

/**
 * Created by yuety on 15/8/2.
 */
public interface ISdViewModel {
    void loadByConfig(SdNode config);
    void loadByJson(SdNode config, String json);
}
