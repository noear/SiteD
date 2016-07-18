using ChakraBridge;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Controls;


namespace org.noear.sited {
    public sealed class JsEngine
    {
        private ChakraHost engine = null;
        private SdSource source = null;

        public JsEngine(SdSource s) {
            source = s;
            engine = new ChakraHost();

            //if (s != null) {
            //    engine.RegisterFunction("print", (args) =>
            //    {
            //        if (args.Length > 0) {
            //            Util.log(source, "JsEngine.print", args[0].ConvertToString().ToString());
            //        }
            //    });
            //}
        }

        public JsEngine loadJs(string funs)
        {
            try
            {
                engine.RunScript(funs);//预加载了批函数
            }
            catch (Exception ex)
            {
                Util.log(source, "JsEngine.loadJs", ex.Message, ex);
                throw ex;
            }

            return this;
        }


        //调用函数
        public string callJs(string fun, params object[] args)
        {
            try
            {
                return engine.CallFunction(fun, args.ToArray()).ToString();
            }
            catch (Exception ex) {
                Util.log(source, "JsEngine.callJs:" + fun, ex.Message, ex);
                return null;
            }
        }
    }
}
