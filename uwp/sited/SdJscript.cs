using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;
using Windows.UI.Xaml;

namespace org.noear.sited {
    public class SdJscript {
        public SdNode require { get; private set; }
        public string code { get; private set; }
        public SdSource s { get; private set; }

        internal SdJscript(SdSource source, XElement node) {
            s = source;

            if (node == null) {
                code = "";
                require = Util.createNode(source).buildForNode(null);
            }
            else {
                code = node.Element("code").Value.Trim();
                require = Util.createNode(source).buildForNode(node.Element("require"));
            }
        }

        public void loadJs(JsEngine js) {
            if (require.isEmpty() == false) {
                foreach (SdNode n1 in require.items()) {
                    //1.如果本地可以加载并且没有出错
                    if (String.IsNullOrEmpty(n1.lib) == false) {
                        if (loadLib(js, n1.lib))
                            continue;
                    }

                    //2.尝试网络加载
                    Log.v("SdJscript", n1.url);

                    n1.cache = 1;
                    Util.http(s, false, n1.url, null, 0, n1, (code,t, text) =>
                    {
                        if (code == 1) {
                            js.loadJs(text);
                        }
                    });
                }
            }

            if (String.IsNullOrEmpty(code) == false) {
                js.loadJs(code);
            }
        }
        
        bool loadLib(JsEngine js, String lib) {
            
            /*
            //for debug
            Resources asset = app.getResources();

            switch (lib) {
                case "md5":
                    return tryLoadLibItem(asset, R.raw.md5, js);

                case "sha1":
                    return tryLoadLibItem(asset, R.raw.sha1, js);

                case "base64":
                    return tryLoadLibItem(asset, R.raw.base64, js);

                case "cheerio":
                    return tryLoadLibItem(asset, R.raw.cheerio, js);

                default:
                    return false;
            }*/

            return false;
        }
        /*
        static boolean tryLoadLibItem(Resources asset, int resID, JsEngine js) {
            try {
                InputStream is = asset.openRawResource(resID);
                BufferedReader in = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String code = doToString(in);
                js.loadJs(code);

                return true;
            }
            catch (Exception ex) {
                return false;
            }
        }

        static String doToString(BufferedReader in) throws IOException
        {
            StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null){
            buffer.append(line);
        }
        return buffer.toString();
    }*/
    }
}
