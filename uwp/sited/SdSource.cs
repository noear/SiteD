using System;
using System.Collections.Generic;
using System.Xml.Linq;
using System.Text.RegularExpressions;

namespace org.noear.sited {
    public class SdSource {
        private SdAttributeList attrs = new SdAttributeList();

        public int schema { get; private set; } = 0;
        public bool isDebug { get; private set; }//是否为调试模式

        public String url_md5 { get; private set; }
        public String url { get; private set; }  //源首页
        public String title { get; private set; } //标题
        public String expr { get; private set; } //标题

        private string _encode;
        public String encode() { return _encode; }

        private String _ua;
        public String ua() {
            if (string.IsNullOrEmpty(_ua))
                return Util.defUA;
            else
                return _ua;
        }

        protected String _cookies;
        public virtual String cookies() { return _cookies; }

        public virtual void setCookies(String cookies) {
            if (cookies != null) {
                _cookies = cookies;
            }
        }

        public void delCache(String key) {
            Util.cache.delete(key);
        }

        //-------------------------------

        public SdNodeSet head;
        public SdNodeSet body;

        internal JsEngine js;//不能作为属性
        protected SdJscript jscript;

        private XElement root;
        protected String xmlBodyName;
        protected String xmlHeadName;
        protected String xmlScriptName;
        //
        //--------------------------------
        //

        protected SdSource() { }

        /// <summary>
        /// 解析
        /// </summary>
        /// <param name="xml"></param>
        public SdSource(String xml) {
            DoInit(xml);

            xmlHeadName = "head";
            xmlBodyName = "body";
            xmlScriptName = "script";

            DoLoad();
        }
        

        protected void DoInit(String xml) {
            Util.tryInitCache();

            {
                root = XDocument.Parse(xml).Root;
                foreach (var att in root.Attributes()) {
                    attrs.set(att.Name.LocalName, att.Value);
                }
            }

            {
                foreach (var p in root.Elements()) {
                    if (p.HasAttributes == false) {
                        if (new List<XNode>(p.Nodes()).Count == 1) {
                            var p2 = p.FirstNode;
                            if (p2 != null && p2.NodeType == System.Xml.XmlNodeType.Text) {
                                attrs.set(p.Name.LocalName, p.Value);
                            }
                        }
                    }
                }
            }

            schema = attrs.getInt("schema");
            isDebug = attrs.getInt("debug") > 0;
        }

        public void DoLoad() {
            xmlHeadName = attrs.getString("head", xmlHeadName);
            xmlBodyName = attrs.getString("body", xmlBodyName);
            xmlScriptName = attrs.getString("script", xmlScriptName);

            head = Util.createNodeSet(this);
            head.buildForNode(root.Element(xmlHeadName));

            body = Util.createNodeSet(this);
            body.buildForNode(root.Element(xmlBodyName));

            if (schema == 0) {
                head.attrs = this.attrs;
            }
            else {
                head.attrs.addAll(this.attrs);
            }

            title = head.attrs.getString("title");
            expr = head.attrs.getString("expr");
            url = head.attrs.getString("url");
            url_md5 = Util.md5(url);

            _encode = head.attrs.getString("encode");
            _ua = head.attrs.getString("ua");

            //----------
            //放后面
            //
            js = new JsEngine(this);
            jscript = new SdJscript(this, root.Element(xmlScriptName));
            jscript.loadJs(js);

            root = null;
        }

        protected virtual bool DoCheck(String url, String cookies, bool isFromAuto) {
            return true;
        }

        protected internal virtual void DoTraceUrl(String url, String args, SdNode config) {

        }


        //
        //------------
        //
        public bool isMatch(String url) {
            return Regex.IsMatch(url, expr);
        }

        public void loadJs(String jsCode) {
            js.loadJs(jsCode);
        }

        public String callJs(SdNode config, String funAttr, params String[] args) {
            return js.callJs(config.attrs.getString(funAttr), args);
        }
        
        public String parse(SdNode config, String url, String html) {
            Log.v("parse", url);
            Log.v("parse", html == null ? "null" : html);

            if (string.IsNullOrEmpty(config.parse)) {
                return html;
            }

            if ("@null".Equals(config.parse)) //如果是@null，说明不需要通过js解析
                return html;
            else {
                String temp = js.callJs(config.parse, url, html);

                if (temp == null) {
                    Log.v("parse.rst", "null" + "\r\n\n");
                }
                else {
                    Log.v("parse.rst", temp + "\r\n\n");
                }
                return temp;
            }
        }

        protected String parseUrl(SdNode config, String url, String html) {
            Log.v("parseUrl", url);
            Log.v("parseUrl", html == null ? "null" : html);

            return js.callJs(config.parseUrl, url, html);
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

            try {
                doGetNodeViewModel(viewModel, isUpdate, key, page, config, callback);
            }
            catch (Exception) {
                callback(1);
            }
        }
        
        private void doGetNodeViewModel(ISdViewModel viewModel, bool isUpdate, String key, int page, SdNode config, SdSourceCallback callback) {
            HttpMessage msg = new HttpMessage();

            page += config.addPage; //加上增减量

            if (key != null && string.IsNullOrEmpty(config.addKey) == false) {//如果有补充关键字
                key = key + " " + config.addKey;
            }

            if (key == null)
                msg.url = config.getUrl(config.url, page);
            else
                msg.url = config.getUrl(config.url, key, page);

            if (string.IsNullOrEmpty(msg.url)) {
                callback(-3);
                return;
            }
            msg.rebuild(config);

            if ("post".Equals(config.method)) {
                msg.rebuildForm(page, key);
            }
            else {
                msg.url = msg.url.Replace("@page", page + "");
                if (key != null) {
                    msg.url = msg.url.Replace("@key", Util.urlEncode(key, config));
                }
            }

            //为doParseUrl_Aft服务(要在外围)
            var dataList = new Dictionary<int, String>();//如果有多个地址，需要排序
            var tag = new __AsyncTag();
            

            int pageX = page;
            String keyX = key;

            tag.total++;

            msg.callback = (code, sender, text, url302) =>{
                tag.value++;
                if (code == 1) {

                    if (string.IsNullOrEmpty(url302)) {
                        url302 = sender.url;
                    }

                    if (string.IsNullOrEmpty(config.parseUrl) == false) { //url需要解析出来(多个用;隔开)
                        List<String> newUrls = new List<String>();
                        String[] rstUrls = parseUrl(config, url302, text).Split(';');

                        foreach (String url1 in rstUrls) {
                            if (url1.Length == 0)
                                continue;

                            if (url1.StartsWith(Util.NEXT_CALL)) {
                                HttpMessage msg0 = new HttpMessage();
                                msg0.url = url1.Replace(Util.NEXT_CALL, "")
                                               .Replace("GET::", "")
                                               .Replace("POST::", "");

                                msg0.rebuild(config);

                                if (url1.IndexOf("POST::") > 0) {
                                    msg0.method = "post";
                                    msg0.rebuildForm(pageX, keyX);
                                }
                                else {
                                    msg0.method = "get";
                                }

                                msg0.callback = msg.callback;

                                tag.total++;
                                Util.http(this, isUpdate, msg0);
                            }
                            else {
                                newUrls.Add(url1);
                            }
                        }

                        if (newUrls.Count > 0) {
                            doParseUrl_Aft(viewModel, config, isUpdate, newUrls, sender.form, tag, dataList, callback);
                        }

                        return;
                    }
                    else {
                        doParse_noAddin(viewModel, config, url302, text);
                    }
                }

                callback(code);
            };


            Util.http(this, isUpdate, msg);
        }


        public void getNodeViewModel(ISdViewModel viewModel, bool isUpdate, int page, String url, SdNode config, SdSourceCallback callback) {
            config.url = url;
            getNodeViewModel(viewModel, isUpdate, null, page, config, callback);
        }

        public void getNodeViewModel(ISdViewModel viewModel, bool isUpdate, String url, SdNode config, SdSourceCallback callback) {
            //需要对url进行转换成最新的格式（可能之前的旧的格式缓存）
            try {
                if (DoCheck(url, cookies(), true) == false) {
                    callback(99);
                    return;
                }

                __AsyncTag tag = new __AsyncTag();

                doGetNodeViewModel(viewModel, isUpdate, tag, url, null, config, callback);

                if (tag.total == 0) {
                    callback(1);
                }
            }
            catch {
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

            if ("@null".Equals(config.method)) {
                viewModel.loadByJson(config, config.getUrl(url));
                return;
            }

            if (string.IsNullOrEmpty(config.parse) == false && string.IsNullOrEmpty(url) == false) {//如果没有url 和 parse，则不处理
                
                HttpMessage msg = new HttpMessage();
                if (args != null) {
                    msg.form = args;
                }

                var dataList = new Dictionary<int, String>();//如果有多个地址，需要排序

                //2.获取主内容
                tag.total++;
                
                msg.url = config.getUrl(url);
                msg.callback = (code, sender, text, url302) => {
                    tag.value++;

                    if (code == 1) {

                        if (string.IsNullOrEmpty(url302)) {
                            url302 = sender.url;
                        }

                        if (string.IsNullOrEmpty(config.parseUrl) == false) { //url需要解析出来(多个用;隔开)
                            List<String> newUrls = new List<String>();
                            String[] rstUrls = parseUrl(config, url302, text).Split(';');

                            foreach (String url1 in rstUrls) {
                                if (url1.Length == 0)
                                    continue;

                                if (url1.StartsWith(Util.NEXT_CALL)) {
                                    Util.log(this, "CALL::url=", url1);

                                    HttpMessage msg0 = new HttpMessage();
                                    msg0.url = url1.Replace(Util.NEXT_CALL, "")
                                            .Replace("GET::", "")
                                            .Replace("POST::", "");

                                    msg0.rebuild(config);

                                    if (url1.IndexOf("POST::") > 0) {
                                        msg0.method = "post";
                                        msg0.rebuildForm();
                                    }
                                    else {
                                        msg0.method = "get";
                                    }
                                    msg0.callback = msg.callback;

                                    tag.total++;
                                    Util.http(this, isUpdate, msg0);
                                }
                                else {
                                    newUrls.Add(url1);
                                }
                            }

                            if (newUrls.Count > 0) {
                                doParseUrl_Aft(viewModel, config, isUpdate, newUrls, args, tag, dataList, callback);
                            }
                            return;//下面的代码被停掉
                        }
                        else {
                            doParse_hasAddin(viewModel, config, url302, text);
                        }
                    }
                    
                    if (tag.total == tag.value) {
                        callback(code);
                    }
                };

                //有缓存的话，可能会变成同步了
                msg.rebuild(config);
                Util.http(this, isUpdate, msg);
            }

            if (config.hasAdds()) {
                //2.2 获取副内容（可能有多个）
                foreach (SdNode n1 in config.adds()) {
                    if (n1.isEmptyUrl())
                        continue;

                    tag.total++;
                    HttpMessage msg = new HttpMessage();
                    //如果自己有url，则使用自己的url；；如果没有，则通过父级的url进行buildUrl(url)
                    msg.url = ( string.IsNullOrEmpty(n1.url) ? n1.getUrl(url) : n1.url );
                    msg.callback = (code, sender, text, url302) => {
                        if (code == 1) {
                            if (string.IsNullOrEmpty(url302)) {
                                url302 = msg.url;
                            }

                            String json = this.parse(n1, url302, text);
                            if (isDebug) {
                                Util.log(this, n1, url302, json, 0);
                            }

                            if (json != null) {
                                viewModel.loadByJson(n1, json);
                            }
                        }

                        tag.value++;
                        if (tag.total == tag.value) {
                            callback(code);
                        }
                    };

                    msg.rebuild(config);
                    Util.http(this, isUpdate, msg);
                }
            }
        }

        private void doParseUrl_Aft(ISdViewModel viewModel, SdNode config, bool isUpdate, List<String> newUrls, Dictionary<String, String> args, __AsyncTag tag, Dictionary<int, String> dataList, SdSourceCallback callback) {
            foreach (String newUrl2 in newUrls) {
                tag.total++;

                HttpMessage msg = new HttpMessage(config, newUrl2, tag.total, args);

                msg.callback = (code2, sender, text2, url302) => {
                    tag.value++;

                    if (code2 == 1) {
                        if (string.IsNullOrEmpty(url302)) {
                            url302 = newUrl2;
                        }

                        doParse_noAddinForTmp(dataList, config, url302, text2, sender.tag);
                    }
                    
                    if (tag.total == tag.value) {
                        List<String> jsonList = new List<String>();

                        for (int i = 1; i <= tag.total; i++) { //安排序加载内容
                            if (dataList.ContainsKey(i)) {
                                jsonList.Add(dataList[i]);
                            }
                        }


                        String[] strAry = jsonList.ToArray();
                        viewModel.loadByJson(config, strAry);

                        callback(code2);
                    }
                };

                Util.http(this, isUpdate, msg);
            }
        }

        private void doParse_noAddin(ISdViewModel viewModel, SdNode config, String url, String text) {
            String json = this.parse(config, url, text);
            if (isDebug) {
                Util.log(this, config, url, json, 0);
            }

            if (json != null) {
                viewModel.loadByJson(config, json);
            }
        }

        private void doParse_hasAddin(ISdViewModel viewModel, SdNode config, String url, String text) {
            String json = this.parse(config, url, text);

            if (isDebug) {
                Util.log(this, config, url, json, 0);
            }

            if (json != null) {
                viewModel.loadByJson(config, json);

                if (config.hasAdds()) { //没有url的add
                    foreach (SdNode n1 in config.adds()) {
                        if (string.IsNullOrEmpty(n1.buildUrl) == false || string.IsNullOrEmpty(n1.url) == false)
                            continue;

                        String json2 = this.parse(n1, url, text);
                        if (isDebug) {
                            Util.log(this, n1, url, json2, 0);
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
                Util.log(this, config, url, json, tag);
            }

            if (json != null) {
                dataList.Add(tag, json);
            }
        }
    }
}
