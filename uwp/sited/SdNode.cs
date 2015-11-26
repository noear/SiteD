using System.Collections.Generic;
using System.Xml.Linq;

namespace org.noear.sited {
    public class SdNode : ISdNode {
        public int nodeType() { return 1; }

        //info
        public string name { get; private set; } //节点名称
        public string title { get; set; }//标题
        public string url { get; set; } //url
        public string logo { get; private set; } //logo
        public string expr { get; private set; }
        public string group { get;  set; }

        //http
        public string header { get; private set; }  //http header 头需求: cookies|accept
        internal string method { get; private set; }//http method
        internal string accept { get; private set; }//http accept

        private string _encode;   //http 编码
        private string _ua;     //http ua
        private string _run;


        //cache
        internal int cache = 1;//单位为秒(0不缓存；1不限时间)

        //parse
        internal string parse; //解析函数
        internal string parseUrl; //解析出真正在请求的Url

        //build
        internal string buildUrl;
        internal string buildKey;
        internal string buildRef;//
        internal string buildWeb;

        //add prop for search or tag
        internal string addKey; //需要添加的关键字
        internal int addPage;//需要添加的页数值

        //ext prop (for post)
        public string args;

        //宽高比例
        public float WHp { get; private set; }

        public  SdSource source { get; private set; }

        private bool _isEmpty;
        public bool isEmpty() {
            return _isEmpty;
        }

        public bool isWebrun() {
            return "web".Equals(_run);
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
                return source.encode;
            else
                return _encode;
        }


        //获取referer url
        public string referer(string url) {
            return source.buildReferer(this, url);
        }

        //获取cookies
        public string cookies() {
            return source.cookies();
        }

        public SdNode() {
        }

        public SdNode(SdSource source, XElement cfg) {
            this.source = source;
            _isEmpty = (cfg == null);

            if (cfg != null) {
                this.name = cfg.Name.LocalName;

                this.title = cfg.Attribute("title")?.Value;
                this.method = cfg.Attribute("method")?.Value;
                this.parse = cfg.Attribute("parse")?.Value;
                this.parseUrl = cfg.Attribute("parseUrl")?.Value;
                this.url = cfg.Attribute("url")?.Value;
                this.expr = cfg.Attribute("expr")?.Value;
                this._run = cfg.Attribute("run")?.Value;
                this._encode = cfg.Attribute("encode")?.Value;
                this._ua = cfg.Attribute("ua")?.Value;

                if (string.IsNullOrEmpty(this.method)) {
                    this.method = "get";
                }

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

                this.accept = cfg.Attribute("accept")?.Value;
                this.header = cfg.Attribute("header")?.Value;
                if (this.header == null)
                    this.header = "";

                string w = cfg.Attribute("w")?.Value;
                if (string.IsNullOrEmpty(w) == false) {
                    string h = cfg.Attribute("h")?.Value;
                    WHp = float.Parse(w) / float.Parse(h);
                }

                this.buildRef = cfg.Attribute("buildRef")?.Value;
                this.buildUrl = cfg.Attribute("buildUrl")?.Value;
                this.buildKey = cfg.Attribute("buildKey")?.Value;
                this.buildWeb = cfg.Attribute("buildWeb")?.Value;

                this.addKey = cfg.Attribute("addKey")?.Value;
                string _addPage = cfg.Attribute("addPage")?.Value;
                if (string.IsNullOrEmpty(_addPage) == false) {
                    this.addPage = int.Parse(_addPage);
                }

                //搜索物有
                this.args = cfg.Attribute("args")?.Value;

                if (cfg.HasElements) {
                    _items = new List<SdNode>();
                    _adds = new List<SdNode>();

                    foreach (XElement e1 in cfg.Elements()) {
                        if (e1.Name.LocalName.Equals("item")) {
                            SdNode temp = new SdNode(source).buildForItem(e1, this);
                            _items.Add(temp);
                        }
                        else {
                            SdNode temp = new SdNode(source).buildForAdd(e1, this);
                            _adds.Add(temp);
                        }
                    }
                }
            }
        }

        private SdNode(SdSource source) {
            this.source = source;
        }

        //item(继承父节点)
        private SdNode buildForItem(XElement cfg, SdNode p) {
            this.name = p.name;

            this.title   = cfg.Attribute("title")?.Value;//可能为null
            this.url     = cfg.Attribute("url")?.Value;//
            this.logo    = cfg.Attribute("logo")?.Value;
            this.group   = cfg.Attribute("group")?.Value;
            this._encode = cfg.Attribute("encode")?.Value;

            return this;
        }

        //add (不继承父节点)
        private SdNode buildForAdd(XElement cfg, SdNode p) { //add不能有自己独立的url //定义为同一个page的数据获取(可能需要多个ajax)
            this.name = cfg.Name.LocalName;//默认为标签名

            this.method = cfg.Attribute("method")?.Value;
            this.accept = cfg.Attribute("accept")?.Value;
            this._encode = cfg.Attribute("encode")?.Value;
            this._ua = cfg.Attribute("ua")?.Value;

            //--------
            this.title = cfg.Attribute("title")?.Value;//可能为null
            this.parse = cfg.Attribute("parse")?.Value;
            this.buildUrl = cfg.Attribute("buildUrl")?.Value;
            this.buildRef = cfg.Attribute("buildRef")?.Value;

            return this;
        }
    }
}
