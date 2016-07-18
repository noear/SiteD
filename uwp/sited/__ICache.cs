using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace org.noear.sited {
    internal interface __ICache {
        Task save(String key, String data);
        Task<__CacheBlock> get(String key);
        Task delete(String key);
        Task<bool> isCached(String key);
    }
}
