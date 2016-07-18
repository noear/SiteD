using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace org.noear.sited {
    internal static class Log {
        public static void v(string tag, string message) {
            Debug.WriteLine(message, "INFO " + tag);
        }

        public static void v(string tag, string message, Exception tr) {
            Debug.WriteLine(message, "INFO " + tag);
        }
    }
}
