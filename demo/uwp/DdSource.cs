using ddcat.uwp.dao.db;
using ddcat.uwp.utils;
using org.noear.sited;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Controls;

namespace ddcat.uwp.dao.engine {
    public class DdSource : SdSource {
        public int ver { get; private set; } //版本号
        public int engine { get; private set; }//引擎版本号
        public String sds { get; private set; } //插件平台服务
        public bool isPrivate { get; private set; }//是否为私密型插件
        

        public String logo { get; private set; }  //图标
        public String author { get; private set; }
        public String alert { get; private set; } //提醒（打开时跳出）
        public String intro { get; private set; } //介绍
                                                  //---------------------------------------------------
        public DdNodeSet main { get; private set; }
        public DdNode hots { get; private set; }
        public DdNode updates { get; private set; }
        public DdNode search { get; private set; }
        public DdNode tags { get; private set; }
        public SdNodeSet home { get; private set; }

        private ISdNode _tag;
        private ISdNode _book;
        private ISdNode _section;
        private ISdNode _object;

        public DdNode tag(String url) {
            Log.v("tag.selct::", url);
            return (DdNode)_tag.nodeMatch(url);
        }
        public DdNode book(String url) {
            Log.v("book.selct::", url);
            return (DdNode)_book.nodeMatch(url);
        }
        public DdNode section(String url) {
            Log.v("section.selct::", url);
            return (DdNode)_section.nodeMatch(url);
        }
        public DdNode object1(String url) {
            Log.v("object.selct::", url);
            return (DdNode)_object.nodeMatch(url);
        }

    public DdNode login { get; private set; }
        private String trace_url;

        public string sited { get; set; }

        public DdSource(String xml):base() {

            if (xml.StartsWith("sited::")) {
                int start = xml.IndexOf("::") + 2;
                int end = xml.LastIndexOf("::");
                var txt = xml.Substring(start, end - start);
                var key = xml.Substring(end + 2);
                xml = DdApi.unsuan(txt, key);
            }

            sited = xml;

            DoInit(xml, "main");

            sds = attrs.getString("sds");
            isPrivate = attrs.getInt("private") > 0;
            engine = attrs.getInt("engine");
            ver = attrs.getInt("ver");

            author = attrs.getString("author");
            intro = attrs.getString("intro");
            logo = attrs.getString("logo");

            if (engine > DdApi.version)
                alert = "此插件需要更高版本引擎支持，否则会出错。建议升级！";
            else
                alert = attrs.getString("alert");

            //
            //---------------------
            //

            main = (DdNodeSet)body;
            trace_url = main.attrs.getString("trace");
            

            home = (DdNodeSet)main.get("home");

            hots = (DdNode)home.get("hots");
            updates = (DdNode)home.get("updates");
            tags = (DdNode)home.get("tags");

            search = (DdNode)main.get("search");

            _tag = main.get("tag");
            _book = main.get("book");
            _section = main.get("section");
            _object = main.get("object");

            if (_object.isEmpty()) {
                if (_section.isEmpty())
                    _object = _book;
                else
                    _object = _section;
            }

            login = (DdNode)main.get("login");
        }

        private String _FullTitle;
        public String fullTitle() {
            if (_FullTitle == null) {
                if (isPrivate) {
                    _FullTitle = title;
                }
                else {
                    int idx = url.IndexOf('?');
                    if (idx < 0)
                        _FullTitle = title + " (" + url + ")";
                    else
                        _FullTitle = title + " (" + url.Substring(0, idx) + ")";
                }
            }

            return _FullTitle;
        }

        public override void setCookies(String cookies) {
            if (cookies == null)
                return;

            Log.v("cookies", cookies);

            if (DoCheck("", cookies, false))
            {
                base.setCookies(cookies);
                SiteDbApi.setSourceCookies(this);
            }
        }

        public override string cookies() {
            if (TextUtils.isEmpty(_cookies))
            {
                _cookies = SiteDbApi.getSourceCookies(this);
            }

            return _cookies;
        }

        public bool isLoggedIn(String url, String cookies) {
            return DoCheck(url, cookies, false);
        }

        protected override bool DoCheck(String url, String cookies, bool isFromAuto) {
            if (login.isEmpty())
            {
                return true;
            }
            else
            {
                if (TextUtils.isEmpty(login.check))
                {
                    return true;
                }
                else
                {
                    if (url == null || cookies == null)
                        return false;

                    if (isFromAuto)
                    {
                        if (login.isAutoCheck)
                        {
                            String temp = callJs(login, "check", url, cookies);
                            return temp.Equals("1");
                        }
                        else
                        {
                            return true;//如果不支持自动,则总是返回ok
                        }
                    }
                    else
                    {
                        String temp = callJs(login, "check", url, cookies);
                        return temp.Equals("1");
                    }
                }
            }
        }

        protected override void DoTraceUrl(String url, String args, SdNode config)
        {
            if (TextUtils.isEmpty(trace_url) == false)
            {
                if (TextUtils.isEmpty(url) == false)
                {
                    try
                    {
                        var data = new Dictionary<String, String>();
                        data.Add("_uid", Session.userID + "");
                        data.Add("_uname", Session.nickname);
                        data.Add("_days", Session.dayNum + "");
                        data.Add("_vip", Session.isVip + "");

                        data.Add("url", url);
                        data.Add("args", args);
                        data.Add("node", config.name);

                        HttpUtil.post(trace_url, data, (code, text) =>
                        {
                        });
                    }
                    catch (Exception)
                    {

                    }
                }
            }
        }

        public void tryLogin(bool forUser)
        {
            if (login.isEmpty())
                return;
            
            doLogin();
        }

        private void doLogin() {
            if (login.isWebrun()) {
                String loginUrl = buildUrl(login, login.url);
                Navigation.showWebAddinLogin(this, loginUrl);
            }
            else {

            }
        }

        public static bool isHots(SdNode node) {
            return "hots".Equals(node.name);
        }

        public static bool isUpdates(SdNode node) {
            return "updates".Equals(node.name);
        }

        public static bool isTags(SdNode node) {
            return "tags".Equals(node.name);
        }

        public static bool isBook(SdNode node) {
            return "book".Equals(node.name);
        }


        //
        //--------------------------
        //
        private bool _isAlerted = false;
        public bool tryAlert(Page page, Action<Boolean> callback) {
            if (TextUtils.isEmpty(alert))
                return false;
            else {
                if (_isAlerted == false) {
                    HintUtil.confirm(alert, "继续", "退出", (isOk) =>
                    {
                        _isAlerted = isOk;
                        callback(isOk);
                    });
                }
                return true;
            }
        }

        public String buildWeb(SdNode config, String url) {
            if (config.attrs.contains("buildWeb") == false)
                return url;
            else
                return callJs(config, "buildWeb", url, config.jsTag);
        }
    }
}
