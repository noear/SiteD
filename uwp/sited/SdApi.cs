using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace org.noear.sited {
    public class SdApi {
        internal static SdNodeFactory _factory;
        internal static SdLogListener _listener;

        static JsEngine _js = null;
        public static void tryInit(SdNodeFactory factory, SdLogListener listener) {
            if (_js == null)
                _js = new JsEngine(null);//先让引擎进入工作状态

            if (_factory == null)
                _factory = factory;

            if (_listener == null)
                _listener = listener;
        }

        internal static void check() {
            if (_factory == null || _listener == null) {
                throw new Exception("未初始化");
            }
        }
    }
}
