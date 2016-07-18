using System;

namespace org.noear.sited {
    public interface ISdViewModel {
        void loadByConfig(SdNode config);
        void loadByJson(SdNode config, params String[] jsons);
    }
}
