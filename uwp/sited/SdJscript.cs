using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace org.noear.sited {
    public class SdJscript {
        public SdNode require { get; private set; }
        public string code { get; private set; }
        public SdSource s { get; private set; }

        internal SdJscript(SdSource source, XElement node) {
            s = source;

            if (node == null) {
                code = "";
                require = new SdNode(source, null);
            }
            else {
                code = node.Element("code").Value.Trim();
                require = new SdNode(source, node.Element("require"));
            }
        }

        public void loadJs(JsEngine js) {
            if (require.isEmpty() == false) {
                foreach (SdNode n1 in require.items()) {
                    Debug.WriteLine(n1.url, "SdJscript");

                    n1.cache = 1;
                    Util.http(s, false, n1.url, null, n1, (code, text) =>
                    {
                        if (code == 1) {
                            js.loadJs(s, text);
                        }
                    });
                }
            }

            if (String.IsNullOrEmpty(code) == false) {
                js.loadJs(s, code);
            }
        }
    }
}
