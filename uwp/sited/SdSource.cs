using System;
using System.Collections.Generic;
using System.Xml.Linq;
using System.Text.RegularExpressions;

namespace org.noear.sited {
    public class SdSource {
        public readonly SdAttributeList attrs = new SdAttributeList();

        public bool isDebug { get; private set; }//是否为调试模式

        public String url_md5 { get; private set; }
        public String url { get; private set; }  //源首页
        public String title { get; private set; } //标题
        public String expr { get; private set; } //标题

        private String _ua;
        public String ua() {
            if (string.IsNullOrEmpty(_ua))
                return Util.defUA;
            else
                return _ua;
        }

        private string _encode;
        public String encode() { return _encode; }

        private String _cookies;
        public String cookies() { return _cookies; }

        public virtual void setCookies(String cookies) {
            if (cookies != null) {
                _cookies = cookies;
            }
        }

        public void delCache(String key) {
            Util.cache.delete(key);
        }

        //-------------------------------

        public  SdNodeSet body;
        private  JsEngine js;//不能作为属性
        protected  SdJscript jscript;
        //
        //--------------------------------
        //


        /// <summary>
        /// 解析
        /// </summary>
        /// <param name="xml"></param>
        public SdSource(String xml, string xmlBodyNodeName) {
            DoInit(xml, xmlBodyNodeName);
        }

        protected SdSource() {

        }

        protected virtual bool DoCheck(String url, String html, String cookies) {
            return true;
        }

        protected void DoInit(String xml, string xmlBodyNodeName) {
            Util.tryInitCache();

            var root = XDocument.Parse(xml).Root;
            foreach (var att in root.Attributes()) {
                attrs.set(att.Name.LocalName, att.Value);
            }

            foreach (var p in root.Elements()) {
                if (p.HasAttributes == false) {
                    var p2 = p.FirstNode;
                    if (p2 != null && p2.NodeType == System.Xml.XmlNodeType.Text) {
                        attrs.set(p.Name.LocalName, p.Value);
                    }
                }
            }

            isDebug = attrs.getInt("debug") > 0;

            title = attrs.getString("title");
            expr = attrs.getString("expr");
            url = attrs.getString("url");
            url_md5 = Util.md5(url);

            _encode = attrs.getString("encode");
            _ua = attrs.getString("ua");

            body = Util.createNodeSet(this);
            body.buildForNode(root.Element(xmlBodyNodeName));

            js = new JsEngine(this);
            jscript = new SdJscript(this, root.Element("jscript"));
            jscript.loadJs(js);

            OnDidInit();
        }

        public void OnDidInit() {

        }

        //
        //------------
        //
        public bool isMatch(String url) {
            return Regex.IsMatch(url, expr);
        }

        public String callJs(SdNode config, String funAttr,params String[] args) {
            return js.callJs(config.attrs.getString(funAttr), args);
        }

        //public bool isNode(SdNode node, String url) {
        //    if (string.IsNullOrEmpty(node.expr) == false) {
        //        return Regex.IsMatch(url, node.expr);
        //    }
        //    else {
        //        return false;
        //    }
        //}

        //-------------

        public String buildArgs(SdNode config, String url, String key, int page) {
            if (string.IsNullOrEmpty(config.buildArgs))
                return config.args;
            else 
                return js.callJs(config.buildArgs, url, key, page + "", config.jsTag??"");
        }

        public String buildArgs(SdNode config, String url, int page) {
            if (string.IsNullOrEmpty(config.buildArgs))
                return config.args;
            else
                return js.callJs(config.buildArgs, url, page + "", config.jsTag ?? "");
        }
        

        public String buildUrl(SdNode config, String url) {
            if (string.IsNullOrEmpty(config.buildUrl))
                return url;
            else
                return js.callJs( config.buildUrl, url, config.jsTag ?? "");
        }

        public String buildUrl(SdNode config, String url, int page) {
            if (string.IsNullOrEmpty(config.buildUrl))
                return url;
            else
                return js.callJs(config.buildUrl, url, page + "", config.jsTag ?? "");
        }

        public String buildUrl(SdNode config, String url, String key, int page) {
            if (string.IsNullOrEmpty(config.buildUrl))
                return url;
            else {
                return js.callJs(config.buildUrl, url, key, page + "", config.jsTag ?? "");
            }
        }

        public String buildReferer(SdNode config, String url) {
            if (string.IsNullOrEmpty(config.buildRef))
                return url;
            else
                return js.callJs( config.buildRef, url, config.jsTag ?? "");
        }

        public String parse(SdNode config, String url, String html) {
            Log.v("parse", html);

            if ("@null".Equals(config.parse)) //如果是@null，说明不需要通过js解析
                return html;
            else {
                return js.callJs( config.parse, url, html, config.jsTag ?? "");
            }
        }

        protected String parseUrl(SdNode config, String url, String html) {
            Log.v("parseUrl", html);
            return js.callJs( config.parseUrl, url, html, config.jsTag ?? "");
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
            
            if (key != null && string.IsNullOrEmpty(config.addKey) == false) {//如果有补充关键字
                key = key + " " + config.addKey;
            }

            String newUrl = null;
            if (key == null)
                newUrl = buildUrl(config, config.url, page);
            else
                newUrl = buildUrl(config, config.url, key, page);

            if (string.IsNullOrEmpty(newUrl)) {
                callback(-3);
                return;
            }

            Dictionary<String, String> args = null;
            if ("post".Equals(config.method)) {
                args = new Dictionary<string, string>();

                String _strArgs = null;
                if (key == null)
                    _strArgs = buildArgs(config, url, page);
                else
                    _strArgs = buildArgs(config, url, key, page);

                if (string.IsNullOrEmpty(_strArgs) == false) {
                    foreach (String kv in _strArgs.Split(';')) {
                        if (kv.Length > 3) {
                            String name = kv.Split('=')[0];
                            String value = kv.Split('=')[1];

                            if (value.Equals("@key"))
                                args.Add(name, Util.urlEncode(key, config));
                            else if (value.Equals("@page"))
                                args.Add(name, page + "");
                            else
                                args.Add(name, value);
                        }
                    }
                }
            }
            else {
                newUrl = newUrl.Replace("@page", page + "");
                if (key != null) newUrl = newUrl.Replace("@key", Util.urlEncode(key, config));
            }

            String newUrl0 = newUrl;
            Dictionary<String, String> args0 = args;

            Util.http(this, isUpdate, newUrl, args0, 0, config, (code, t, text) =>
            {
                if (code == 1) {
                    if (DoCheck(newUrl0, text, cookies()) == false) {
                        callback(99);
                        return;
                    }

                    if (string.IsNullOrEmpty(config.parseUrl) == false) { //url需要解析出来(多个用;隔开)
                        String[] newUrls = parseUrl(config, newUrl0, text).Split(';');
                        Dictionary<int, String> dataList = new Dictionary<int, string>();//如果有多个地址，需要排序
                        __AsyncTag tag = new __AsyncTag();

                        foreach (String newUrl2 in newUrls) {
                            tag.total++;
                            Util.http(this, isUpdate, newUrl2, args0, tag.total, config, (code2, t2, text2) =>
                            {
                                if (code2 == 1) {
                                    doParse_noAddinForTmp(dataList, config, newUrl2, text2, t2);
                                }

                                tag.value++;
                                if (tag.total == tag.value) {
                                    List<string> jsonList = new List<string>();
                                    for (int i = 1; i <= tag.total; i++) { //安排序加载内容
                                        if (dataList.ContainsKey(i)) {
                                            jsonList.Add(dataList[i]);
                                        }
                                    }
                                    viewModel.loadByJson(config, jsonList.ToArray());

                                    callback(code);
                                }
                            });
                        }
                        return;//下面的代码被停掉
                    }

                    doParse_noAddin(viewModel, config, newUrl0, text);
                }

                callback(code);
            });
        }


        public void getNodeViewModel(ISdViewModel viewModel, bool isUpdate, int page, SdNode config, SdSourceCallback callback) {
            getNodeViewModel(viewModel, isUpdate, null, page, config, callback);
        }

        public void getNodeViewModel(ISdViewModel viewModel, bool isUpdate, String url, SdNode config, SdSourceCallback callback) {
            //需要对url进行转换成最新的格式（可能之前的旧的格式缓存）

            __AsyncTag tag = new __AsyncTag();

            doGetNodeViewModel(viewModel, isUpdate, tag, url, null, config, callback);

            if (tag.total == 0) {
                callback(1);
            }
        }

        private void doGetNodeViewModel(ISdViewModel viewModel, bool isUpdate, __AsyncTag tag, String url, Dictionary<String, String> args, SdNode config, SdSourceCallback callback) {
            //需要对url进行转换成最新的格式（可能之前的旧的格式缓存）

            if (config.isEmpty())
                return;

            if (config.hasItems()) {
                viewModel.loadByConfig(config);
                return;
            }
            
            if (string.IsNullOrEmpty(config.parse)==false && string.IsNullOrEmpty(url)==false) {//如果没有url 和 parse，则不处理
                //2.获取主内容
                tag.total++;
                String newUrl = buildUrl(config, url);

                //有缓存的话，可能会变成同步了
                Util.http(this, isUpdate, newUrl, args, 0, config, (code, t, text) =>
                {
                    if (code == 1) {

                        if (DoCheck(newUrl, text, cookies()) == false) {
                            callback(99);
                            return;
                        }

                        if (string.IsNullOrEmpty(config.parseUrl) == false) { //url需要解析出来(多个用;隔开)
                            String[] newUrls = parseUrl(config, newUrl, text).Split(';');
                            var dataList = new Dictionary<int, String>();//如果有多个地址，需要排序

                            tag.total--;//抵消之前的++
                            foreach (String newUrl2 in newUrls) {
                                tag.total++;
                                Util.http(this, isUpdate, newUrl2, args, tag.total, config, (code2, t2, text2) =>
                                {
                                    if (code2 == 1) {
                                        doParse_noAddinForTmp(dataList, config, newUrl2, text2, t2);
                                    }

                                    tag.value++;
                                    if (tag.total == tag.value) {
                                        List<string> jsonList = new List<string>();
                                        for (int i = 1; i <= tag.total; i++) { //安排序加载内容
                                            if (dataList.ContainsKey(i)) {
                                                jsonList.Add(dataList[i]);
                                            }
                                        }
                                        viewModel.loadByJson(config,jsonList.ToArray());
                                        callback(code);
                                    }
                                });
                            }
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
                    if (string.IsNullOrEmpty(n1.buildUrl) && string.IsNullOrEmpty(n1.url))
                        continue;

                    tag.total++;
                    //如果自己有url，则使用自己的url；；如果没有，则通过父级的url进行buildUrl(url)
                    String newUrl = (string.IsNullOrEmpty(n1.url) ? buildUrl(n1, url) : n1.url);

                    Util.http(this, isUpdate, newUrl, args, 0, n1, (code, t, text) =>
                    {
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

        private void doParse_noAddin(ISdViewModel viewModel, SdNode config, String url, String text) {
            String json = this.parse(config, url, text);
            if (isDebug) {
                Util.log(this, config, url, json);
            }

            if (json != null) {
                viewModel.loadByJson(config, json);
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
                        if (string.IsNullOrEmpty(n1.buildUrl) == false || string.IsNullOrEmpty(n1.url)==false)
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

        private void doParse_noAddinForTmp(Dictionary<int, String> dataList, SdNode config, String url, String text, int tag) {
            String json = this.parse(config, url, text);

            if (isDebug) {
                Util.log(this, config, url, json);
            }

            if (json != null) {
                dataList.Add(tag, json);
            }
        }
    }
}
