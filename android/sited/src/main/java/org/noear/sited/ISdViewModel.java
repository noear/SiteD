package org.noear.sited;

import java.util.Collection;
import java.util.List;

/**
 * Created by yuety on 15/8/2.
 */
public interface ISdViewModel {
    void loadByConfig(SdNode config);
    void loadByJson(SdNode config, String... jsons);
}
