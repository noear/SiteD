using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace org.noear.sited {
   internal class __CacheBlock {
        public String value = null;
        public DateTime time = DateTime.MinValue;

        public long seconds() {
            return (DateTime.Now - time).Seconds;
        }
    }
}
