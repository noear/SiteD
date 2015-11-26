using System;
using System.Collections.Generic;
using System.Xml.Linq;
using System.Text.RegularExpressions;

namespace org.noear.sited {
    public class SdSource {
        public  String sds { get; private set; } //插件平台服务
        public  String expr { get; private set; }//匹配源的表达式

        public  bool isDebug { get; private set; }//是否为调试模式
        public bool isPrivate { get; private set; }//是否为私密型插件

        public  int dtype { get; private set; }//数据类型

        public  String url_md5 { get; private set; }
        public  String url { get; private set; }  //源首页
        public  int ver { get; private set; } //版本号

        public  String title { get; private set; } //标题
        public  String intro { get; private set; } //介绍
        public  String alert { get; private set; } //提醒（打开时跳出）
        public  String logo { get; private set; }  //图标

        public SdNodeSet main { get; private set; }//源main节点

        internal readonly String encode;//编码
        private String _cookies;
        public String cookies() {
            return _cookies;
        }

        public virtual void setCookies(String cookies) {
            if (cookies != null) {
                _cookies = cookies;
            }
        }

        private SdJscript jscript;
        private String _ua;
        

        public String ua() {
            if (string.IsNullOrEmpty(_ua))
                return Util.defUA;
            else
                return _ua;
        }

        public void delCache(String key) {
            Util.cache.delete(key);
        }

        //给Util调用
        public static SdLogListener logListener { get; set; }

        /// <summary>
        /// 解析
        /// </summary>
        /// <param name="xml"></param>
        public SdSource(String xml) {
            Util.tryInitCache();

            main = new SdNodeSet(this);

            var root =  XDocument.Parse(xml).Root;
            
            ver = int.Parse(root.Attribute("ver").Value.Trim());
            sds = root.Attribute("sds")?.Value;
            isDebug = "1".Equals(root.Attribute("debug")?.Value);
            isPrivate = "1".Equals(root.Attribute("private")?.Value);

            expr = root.Element("expr").Value.Trim();
            url = root.Element("url").Value.Trim();
            url_md5 = Util.md5(url);


            title = root.Element("title").Value.Trim();
            intro = root.Element("intro").Value.Trim();
            alert = root.Element("alert")?.Value.Trim();

            logo = root.Element("logo").Value.Trim();
            encode = root.Element("encode").Value.Trim();
            _ua = root.Element("ua").Value.Trim();
            
            jscript = new SdJscript(this, root.Element("jscript"));

            XElement xMain = root.Element("main");
            dtype = int.Parse(xMain.Attribute("dtype").Value);
            main.loadByElement(xMain);

            js = new JsEngine();
            jscript.loadJs(js);
        }

        internal JsEngine js;//不能作为属性

        //
        //------------
        //
        public bool isMe(String url) {
            return Regex.IsMatch(url, expr);
        }

        public bool isNode(SdNode node, String url) {
            if (string.IsNullOrEmpty(node.expr) == false) {
                return Regex.IsMatch(url, node.expr);
            }
            else {
                return false;
            }
        }

        public String buildKey(SdNode config, String url) {
            if (string.IsNullOrEmpty(config.buildKey))
                return url;
            else
                return js.callJs(this, config.buildKey, url);
        }

        public String buildWeb(SdNode config, String url) {
            if (string.IsNullOrEmpty(config.buildWeb))
                return url;
            else
                return js.callJs(this, config.buildWeb, url);
        }

        public String buildUrl(SdNode config, String url) {
            if (string.IsNullOrEmpty(config.buildUrl))
                return url;
            else
                return js.callJs(this, config.buildUrl, url);
        }

        public String buildUrl(SdNode config, String url, int page) {
            if (string.IsNullOrEmpty(config.buildUrl))
                return url;
            else
                return js.callJs(this, config.buildUrl, url, page + "");
        }

        public String buildReferer(SdNode config, String url) {
            if (string.IsNullOrEmpty(config.buildRef))
                return url;
            else
                return js.callJs(this, config.buildRef, url);
        }

        public String parse(SdNode config, String url, String html) {
            if ("@null".Equals(config.parse)) //如果是@null，说明不需要通过js解析
                return html;
            else
                return js.callJs(this, config.parse, url, html);
        }

        protected String parseUrl(SdNode config, String url, String html) {
            return js.callJs(this, config.parseUrl, url, html);
        }

        //
        //---------------------------------------
        //
        public void getNodeViewModel(ISdViewModel viewModel, SdNodeSet nodeSet, bool isUpdate, SdSourceCallback callback) {
            __AsyncTag tag = new __AsyncTag();

            foreach (ISdNode node in nodeSet.nodes()) {
                SdNode n = (SdNode)node;
                doGetNodeViewModel(viewModel, isUpdate, tag, n.url, null, n, callback);
            }

            if (tag.total == 0) {
                callback(1);
            }
        }

        public void getNodeViewModel(ISdViewModel viewModel, bool isUpdate, String key, int page, SdNode config, SdSourceCallback callback) {
            page += config.addPage; //加上增减量

            String newUrl = buildUrl(config, config.url, page);

            if (key != null && string.IsNullOrEmpty(config.addKey) == false) {//如果有补充关键字
                key = key + " " + config.addKey;
            }

            Dictionary<String, String> args = null;
            if ("post".Equals(config.method)) {
                args = new Dictionary<string, string>();
                
                if (string.IsNullOrEmpty(config.args) == false) {
                    foreach (String kv in config.args.Split(';')) {
                        if (kv.Length > 3) {
                            String name = kv.Split('=')[0];
                            String value = kv.Split('=')[1];

                            if (value.Equals("@key"))
                                args.Add(name, Util.urlEncode(key, config));
                            else if (value.Equals("@page"))
                                args.Add(name, page + "");
                            else
                                args.Add(name, Util.urlEncode(value, config));
                        }
                    }
                }

            }
            else {
                newUrl = newUrl.Replace("@page", page + "");
                if (key != null) newUrl = newUrl.Replace("@key", Util.urlEncode(key, config));
            }

             String newUrl0 = newUrl;
             Dictionary< String, String > args0 = args;

            Util.http(this, isUpdate, newUrl, args0, config, (code, text) => {
                if (code == 1) {

                    if (string.IsNullOrEmpty(config.parseUrl) == false) { //url需要解析出来
                        String newUrl2 = parseUrl(config, newUrl0, text);

                        Util.http(this, isUpdate, newUrl2, args0, config, (code2, text2) => {
                            if (code2 == 1) {
                                doParse_noAddin(viewModel, config, newUrl2, text2);
                            }
                            callback(code);
                        });
                        return;//下面的代码被停掉
                    }

                    doParse_noAddin(viewModel, config, newUrl0, text);
                }

                callback(code);
            });
        }

        private void doParse_noAddin(ISdViewModel viewModel, SdNode config, String url, String text) {
            String json = this.parse(config, url, text);
            if (isDebug) {
                Util.log(this, config, url, json);
            }

            if (json != null) {
                viewModel.loadByJson(config, json);
            }
        }


        public void getNodeViewModel(ISdViewModel viewModel, bool isUpdate, int page, SdNode config, SdSourceCallback callback) {
            getNodeViewModel(viewModel, isUpdate, null, page, config, callback);
        }

        public void getNodeViewModel(ISdViewModel viewModel, bool isUpdate,  String url, SdNode config, SdSourceCallback callback) {
            //需要对url进行转换成最新的格式（可能之前的旧的格式缓存）

            __AsyncTag tag = new __AsyncTag();

            doGetNodeViewModel(viewModel, isUpdate, tag, url, null, config, callback);

            if (tag.total == 0) {
                callback(1);
            }
        }

        private void doGetNodeViewModel(ISdViewModel viewModel, bool isUpdate,  __AsyncTag tag,  String url, Dictionary<String, String> args, SdNode config, SdSourceCallback callback) {
            //需要对url进行转换成最新的格式（可能之前的旧的格式缓存）

            if (config.isEmpty())
                return;

            if (config.hasItems()) {
                viewModel.loadByConfig(config);
                return;
            }

            if (string.IsNullOrEmpty(config.parse)) //没有解析的不处理
                return;

            //------------
            if (string.IsNullOrEmpty(url)) //url为空的不处理
                return;

            {
                //2.获取主内容
                tag.total++;
                String newUrl = buildUrl(config, url);

                //有缓存的话，可能会变成同步了
                Util.http(this, isUpdate, newUrl, args, config, (code, text) => {
                    if (code == 1) {

                        if (string.IsNullOrEmpty(config.parseUrl) == false) { //url需要解析出来
                            String newUrl2 = parseUrl(config, newUrl, text);

                            Util.http(this, isUpdate, newUrl2, args, config, (code2, text2) => {
                                if (code2 == 1) {
                                    doParse_hasAddin(viewModel, config, newUrl2, text2);
                                }

                                tag.value++;
                                if (tag.total == tag.value) {
                                    callback(code);
                                }
                            });
                            return;//下面的代码被停掉
                        }

                        doParse_hasAddin(viewModel, config, newUrl, text);
                    }

                    tag.value++;
                    if (tag.total == tag.value) {
                        callback(code);
                    }
                });
            }

            if (config.hasAdds()) {
                //2.2 获取副内容（可能有多个）
                foreach (SdNode n1 in config.adds()) {
                    if (string.IsNullOrEmpty(n1.buildUrl))
                        continue;

                    tag.total++;
                    String newUrl = buildUrl(n1, url); //有url的add //add 不能有自己的独立url

                    Util.http(this, isUpdate, newUrl, args, n1, (code, text) => {
                        if (code == 1) {
                            String json = this.parse(n1, newUrl, text);
                            if (isDebug) {
                                Util.log(this, n1, newUrl, json);
                            }

                            if (json != null) {
                                viewModel.loadByJson(n1, json);
                            }
                        }

                        tag.value++;
                        if (tag.total == tag.value) {
                            callback(code);
                        }
                    });
            }
        }
    }

    private void doParse_hasAddin(ISdViewModel viewModel, SdNode config, String url, String text) {
        String json = this.parse(config, url, text);

        if (isDebug) {
            Util.log(this, config, url, json);
        }

        if (json != null) {
            viewModel.loadByJson(config, json);

            if (config.hasAdds()) { //没有url的add
                foreach (SdNode n1 in config.adds()) {
                    if (string.IsNullOrEmpty(n1.buildUrl) == false)
                        continue;

                    String json2 = this.parse(n1, url, text);
                    if (isDebug) {
                        Util.log(this, n1, url, json2);
                    }

                    if (json2 != null)
                        viewModel.loadByJson(n1, json2);
                }
            }
        }
    }
}
}
