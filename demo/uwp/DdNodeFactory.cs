using org.noear.sited;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ddcat.uwp.dao.engine {
    public class DdNodeFactory : SdNodeFactory {
        public override SdNode createNode(SdSource source) {
            return new DdNode(source);
        }

        public override SdNodeSet createNodeSet(SdSource source) {
            return new DdNodeSet(source);
        }
    }
}
