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

        public JsEngine() {
            engine = new ChakraHost();
        }

        public JsEngine loadJs(SdSource source, string funs)
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
        public string callJs(SdSource source, string fun, params object[] args)
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
