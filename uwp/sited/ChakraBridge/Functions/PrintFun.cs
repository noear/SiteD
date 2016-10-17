using ChakraBridge;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace org.noear.sited.ChakraBridge.Functions {
    internal static  class PrintFun {
        public static JavaScriptValue PrintJavaScriptNativeFunction(JavaScriptValue callee, bool isConstructCall, [MarshalAs(UnmanagedType.LPArray, SizeParamIndex = 3)] JavaScriptValue[] arguments, ushort argumentCount, IntPtr callbackData) {
            // setTimeout signature is (callback, after)
            //JavaScriptValue callbackValue = arguments[1];

            //JavaScriptValue afterValue = arguments[2].ConvertToNumber();
            //var after = Math.Max(afterValue.ToDouble(), 1);

            //uint refCount;
            //Native.JsAddRef(callbackValue, out refCount);
            //Native.JsAddRef(callee, out refCount);

            //ExecuteAsync((int)after, callbackValue, callee);


            if (arguments.Length > 0) {
                String str = arguments[0].ConvertToString().ToString();
                Debug.WriteLine("JsEngine.print", str);
            }

            return JavaScriptValue.True;
        }
    }
}
