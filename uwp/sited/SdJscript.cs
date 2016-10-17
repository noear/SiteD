using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;
using Windows.Storage;
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

        public  void loadJs(JsEngine js) {
            if (require.isEmpty() == false) {
                foreach (SdNode n1 in require.items()) {
                    //1.如果本地可以加载并且没有出错
                    if (String.IsNullOrEmpty(n1.lib) == false) {
                        if (loadLib(js, n1.lib))
                            continue;
                    }

                    //2.尝试网络加载
                    Log.v("SdJscript", n1.url);

                    if (n1.cache == 0) {
                        n1.cache = 1;
                    }

                    HttpMessage msg = new HttpMessage(n1, n1.url);
                    msg.callback = (code, sender, text, url302) => {
                        if (code == 1) {
                            js.loadJs(text);
                        }
                    };

                    Util.http(s, false, msg);
                }
            }

            if (String.IsNullOrEmpty(code) == false) {
                js.loadJs(code);
            }
        }

        bool loadLib(JsEngine js, String lib) {
            //for debug
            switch (lib) {
                case "md5":
                    return  tryLoadLibItem("md5.js", js);

                case "sha1":
                    return  tryLoadLibItem("sha1.js", js);

                case "base64":
                    return  tryLoadLibItem("base64.js", js);

                case "cheerio":
                    return  tryLoadLibItem("cheerio.js", js);

                default:
                    return false;
            }
        }

        static bool tryLoadLibItem(String name, JsEngine js) {
            try {
                //Uri fileUri = new Uri("ms-appx:///raw/" + uri);
                //StorageFile file = StorageFile.GetFileFromApplicationUriAsync(fileUri).GetResults();
                //String code =  FileIO.ReadTextAsync(file).GetResults();

                Assembly asset = Assembly.Load(new AssemblyName("sited"));
                Stream stream = asset.GetManifestResourceStream("org.noear.sited.raw." + name);

                string code = new StreamReader(stream, Encoding.UTF8).ReadToEnd();

                js.loadJs(code);

                return true;
            }
            catch (Exception) {
                return false;
            }
        }
        
    }
}
