using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace org.noear.sited {
    internal interface __ICache {
        void save(String key, String data);
        __CacheBlock get(String key);
        void delete(String key);
        bool isCached(String key);
    }
}
