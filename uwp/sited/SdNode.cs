using System;
using System.Collections.Generic;
using System.Xml.Linq;
using System.Text.RegularExpressions;

namespace org.noear.sited {
    public class SdNode : ISdNode {

        public SdNode(SdSource source) {
            this.source = source;
        }

        protected virtual void OnDidInit() {

        }

        private int _dtype;
        public int dtype() {
            if (_dtype > 0)
                return _dtype;
            else {
                return source.body.dtype();
            }
        }

        private int _btype;
        public int btype() {
            if (_btype > 0)
                return _btype;
            else
                return dtype();
        }

        public int nodeType() { return 1; }
        public String nodeName() { return name; }
        public SdNode nodeMatch(String url) { return this; }
        public SdAttributeList attrs = new SdAttributeList();

        //info
        public string name { get; private set; } //节点名称
        public string title { get; set; }//标题
        public string url { get; set; } //url
        public string txt { get; set; } //url
        public string logo { get; set; } //logo
        public string expr { get; private set; }
        public string group { get;  set; }

        internal String lib;

        

        //http
        public string header { get; private set; }  //http header 头需求: cookies|accept
        protected internal string method { get; private set; }//http method

        private string _encode;   //http 编码
        private string _ua;     //http ua
        

        //cache
        internal int cache = 1;//单位为秒(0不缓存；1不限时间)

        //parse
        internal string parse; //解析函数
        internal string parseUrl; //解析出真正在请求的Url

        //build
        protected internal string buildArgs;
        protected internal string buildUrl;
        protected internal string buildRef;//
        protected internal String buildHeader;

        //add prop for search or tag
        internal string addCookie; //需要添加的关键字
        internal string addKey; //需要添加的关键字
        internal int addPage;//需要添加的页数值

        //ext prop (for post)
        public string args;
        
        public  SdSource source { get; private set; }

        private bool _isEmpty;
        public bool isEmpty() {
            return _isEmpty;
        }

        //下属项目
        private List<SdNode> _items;
        public IList<SdNode> items() {
            return _items;
        }

        //下属数据节点
        private List<SdNode> _adds;
        public IList<SdNode> adds() {
            return _adds;
        }


        //是否有宏定义@key,@page
        public bool hasMacro() {
            if (url == null || url.IndexOf('@') < 0)
                return false;
            else
                return true;
        }

        public bool isMatch(String url) {
            if (string.IsNullOrEmpty(expr) == false) {
                return Regex.IsMatch(url, expr);
            }
            else {
                return false;
            }
        }


        public bool isEquals(SdNode node) {
            if (name == null)
                return false;

            return name.Equals(node.name);
        }

        public bool isInCookie() {
            if (header == null)
                return false;
            else
                return header.IndexOf("cookie") >= 0;
        }

        public bool isInReferer() {
            if (header == null)
                return false;
            else
                return header.IndexOf("referer") >= 0;
        }

        public bool hasItems() {
            if (_items == null || _items.Count == 0)
                return false;
            else
                return true;
        }

        public bool hasAdds() {
            if (_adds == null || _adds.Count == 0)
                return false;
            else
                return true;
        }

        public string ua() {
            if (string.IsNullOrEmpty(_ua))
                return source.ua();
            else
                return _ua;
        }

        public string encode() {
            if (string.IsNullOrEmpty(_encode))
                return source.encode();
            else
                return _encode;
        }

       

        //获取cookies
        public string cookies(string uri) {
            var cookies = source.cookies();

            if (attrs.contains("buildCookie")) {
                cookies = source.callJs(this, "buildCookie", uri, (cookies == null ? "" : cookies));
            }

            if (string.IsNullOrEmpty(addCookie) == false) {
                if (string.IsNullOrEmpty(cookies)) {
                    cookies = addCookie + "; Path=/; Domain=" + new Uri(uri).Host;
                }
                else {
                    cookies = addCookie + "; " + cookies;
                }
            }

            return cookies;
        }

        public SdNode() {
            this.source = null;
        }

        

        internal SdNode buildForNode(XElement cfg) {
            _isEmpty = (cfg == null);

            if (cfg != null) {
                this.name = cfg.Name.LocalName;

                foreach(XAttribute att in cfg.Attributes()) {
                    attrs.set(att.Name.LocalName, att.Value);
                }

                _dtype = attrs.getInt("dtype");
                _btype = attrs.getInt("btype");

                this.title = attrs.getString("title");
                this.method = attrs.getString("method", "get");
                this.parse = attrs.getString("parse");
                this.parseUrl = attrs.getString("parseUrl");
                this.url = attrs.getString("url");
                this.lib = attrs.getString("lib");
                this.expr = attrs.getString("expr");

                this._encode = attrs.getString("encode");
                this._ua = attrs.getString("ua");

                //book,section 特有
                this.header = attrs.getString("header", "");

                this.buildArgs = attrs.getString("buildArgs");
                this.buildRef = attrs.getString("buildRef");
                this.buildUrl = attrs.getString("buildUrl");
                this.buildHeader = attrs.getString("buildHeader");

                this.args = attrs.getString("args");

                this.addCookie = attrs.getString("addCookie");
                this.addKey = attrs.getString("addKey");
                this.addPage = attrs.getInt("addPage");

                {
                    string temp = cfg.Attribute("cache")?.Value;
                    if (string.IsNullOrEmpty(temp) == false) {
                        int len = temp.Length;
                        if (len == 1) {
                            cache = int.Parse(temp);
                        }
                        else if (len > 1) {
                            cache = int.Parse(temp.Substring(0, len - 1));

                            string p = temp.Substring(len - 1);
                            switch (p) {
                                case "d": cache = cache * 24 * 60 * 60; break;
                                case "h": cache = cache * 60 * 60; break;
                                case "m": cache = cache * 60; break;
                            }
                        }
                    }
                }

                

                if (cfg.HasElements) {
                    _items = new List<SdNode>();
                    _adds = new List<SdNode>();

                    foreach (XElement e1 in cfg.Elements()) {
                        if (e1.Name.LocalName.Equals("item")) {
                            SdNode temp = Util.createNode(source).buildForItem(e1, this);
                            _items.Add(temp);
                        }
                        else if (e1.HasAttributes) {
                            SdNode temp = Util.createNode(source).buildForAdd(e1, this);
                            _adds.Add(temp);
                        }
                        else {
                            attrs.set(e1.Name.LocalName, e1.Value);
                        }
                    }
                }
            }

            OnDidInit();

            return this;
        }

        

        //item(继承父节点)
        private SdNode buildForItem(XElement cfg, SdNode p) {
            foreach(var att in cfg.Attributes()) {
                attrs.set(att.Name.LocalName, att.Value);
            }
            this.name = p.name;

            this.title = attrs.getString("title");//可能为null
            this.group = attrs.getString("group");
            this.url   = attrs.getString("url");//
            this.txt = attrs.getString("txt");//
            this.lib   = attrs.getString("lib");
            this.logo  = attrs.getString("logo");
            this._encode = attrs.getString("encode");
            

            return this;
        }

        //add (不继承父节点)
        private SdNode buildForAdd(XElement cfg, SdNode p) { //add不能有自己独立的url //定义为同一个page的数据获取(可能需要多个ajax)
            foreach (var att in cfg.Attributes()) {
                attrs.set(att.Name.LocalName, att.Value);
            }

            this.name = cfg.Name.LocalName;//默认为标签名

            this.url = attrs.getString("url");
            this.txt = attrs.getString("txt");//
            this.method = attrs.getString("method");
            this.header = attrs.getString("header");
            this._encode = attrs.getString("encode");
            this._ua = attrs.getString("ua");

            //--------
            this.title = attrs.getString("title");//可能为null
            this.parse = attrs.getString("parse");
            this.buildUrl = attrs.getString("buildUrl");
            this.buildRef = attrs.getString("buildRef");
            this.buildHeader = attrs.getString("buildHeader");

            return this;
        }

        //
        //-------------------------------------------------------------
        //

        public String getArgs(String url, String key, int page) {
            if (string.IsNullOrEmpty(this.buildArgs))
                return this.args;
            else
                return source.js.callJs(this.buildArgs, url, key, page + "");
        }

        public String getArgs( String url, int page) {
            if (string.IsNullOrEmpty(this.buildArgs))
                return this.args;
            else
                return source.js.callJs(this.buildArgs, url, page + "");
        }

        public String getUrl() {
            if (string.IsNullOrEmpty(this.buildUrl))
                return this.url;
            else
                return source.js.callJs(this.buildUrl, this.url);
        }

        public String getUrl(String url) {
            if (string.IsNullOrEmpty(this.buildUrl))
                return url;
            else
                return source.js.callJs(this.buildUrl, url ?? "");
        }

        public String getUrl(String url, int page) {
            if (string.IsNullOrEmpty(this.buildUrl))
                return url;
            else
                return source.js.callJs(this.buildUrl, url, page + "");
        }

        public String getUrl(String url, String key, int page) {
            if (string.IsNullOrEmpty(this.buildUrl))
                return url;
            else {
                return source.js.callJs(this.buildUrl, url, key, page + "");
            }
        }

        public String getReferer(String url) {
            if (string.IsNullOrEmpty(this.buildRef))
                return url;
            else
                return source.js.callJs(this.buildRef, url);
        }
        public String getHeader(String url) {
            if (string.IsNullOrEmpty(this.buildHeader))
                return header;
            else
                return source.js.callJs(this.buildHeader, url);
        }

        public bool isEmptyUrl() {
            return string.IsNullOrEmpty(this.buildUrl) && string.IsNullOrEmpty(url);
        }

        public bool isEmptyHeader() {
            return string.IsNullOrEmpty(this.buildHeader) && string.IsNullOrEmpty(header);
        }
    }
}
