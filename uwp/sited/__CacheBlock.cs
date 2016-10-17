using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace org.noear.sited {
   internal class __CacheBlock {
        public String value = null;
        public DateTime time = DateTime.MinValue;
        
        public bool isOuttime(SdNode config) {
            if (time == null || value == null) {
                return true;
            }
            else {
                if (config.cache == 1)
                    return false;
                else {
                    long seconds = ( DateTime.Now - time ).Seconds;
                    return seconds > config.cache;
                }
            }
        }
    }
}
