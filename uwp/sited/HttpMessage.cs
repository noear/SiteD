using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace org.noear.sited {
    public class HttpMessage {
        public Dictionary<String, String> header = new Dictionary<String, String>();
        public Dictionary<String, String> form = new Dictionary<String, String>();
        public String url;

        public int tag;

        public HttpCallback callback;

        public SdNode config;


        //可由cfg实始化
        public String encode;
        public String ua;
        public String method;

        public HttpMessage() {

        }


        public HttpMessage(SdNode cfg, String url, int tag, Dictionary<String, String> args) {
            this.config = cfg;
            this.url = url;
            this.tag = tag;

            if (args != null) {
                form = args;
            }

            rebuild(null);
        }

        public HttpMessage(SdNode cfg, String url) {
            this.config = cfg;
            this.url = url;

            rebuild(null);
        }

        public void rebuild(SdNode cfg) {
            if (cfg != null) {
                this.config = cfg;
            }

            ua = config.ua();
            encode = config.encode();
            method = config.method;


            if (config.isInCookie()) {
                String cookies = config.cookies(url);
                if (cookies != null) {
                    header.Add("Cookie", cookies);
                }
            }

            if (config.isInReferer()) {
                header.Add("Referer", config.getReferer(url));
            }

            if (config.isEmptyHeader() == false) {
                foreach (String kv in config.getHeader(url).Split(';')) {
                    String[] kv2 = kv.Split('=');
                    if (kv2.Length == 2) {
                        header.Add(kv2[0], kv2[1]);
                    }
                }
            }
        }


        public void rebuildForm() {
            rebuildForm(0, null);
        }

        public void rebuildForm(int page, String key) {
            if ("post".Equals(config.method)) {
                String _strArgs = null;
                if (key != null) {
                    config.getArgs(url, key, page);
                }
                else {
                    config.getArgs(url, page);
                }

                Log.v("Post.Args", _strArgs);

                if (string.IsNullOrEmpty(_strArgs) == false) {
                    foreach (String kv in _strArgs.Split(';')) {
                        if (kv.Length > 3) {
                            String name = kv.Split('=')[0];
                            String value = kv.Split('=')[1];

                            if (value.Equals("@key"))
                                form.Add(name, key);
                            else if (value.Equals("@page"))
                                form.Add(name, page + "");
                            else
                                form.Add(name, value);
                        }
                    }
                }

            }
        }
    }
}
