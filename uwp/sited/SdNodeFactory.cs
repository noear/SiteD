using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace org.noear.sited {
   public  class SdNodeFactory {
        public virtual SdNode createNode(SdSource source) {
            return new SdNode(source);
        }

        public virtual SdNodeSet createNodeSet(SdSource source) {
            return new SdNodeSet(source);
        }
    }
}
