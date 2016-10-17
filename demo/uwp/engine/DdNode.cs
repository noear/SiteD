using ddcat.uwp.utils;
using org.noear.sited;
using System;

namespace ddcat.uwp.dao.engine {
    public class DdNode : SdNode {
        public DdSource s() {
            return (DdSource)source;
        }

        //是否支持全部下载(book[1,2,3])
        public bool donwAll = true;
        //是否显示导航能力（用于：section[1,2,3]）
        public bool showNav = true;
        //是否显示图片（null：默认；0：不显示；1：显示小图；2：显示大图）
        public String showImg;
        //是否显示S按钮
        public bool showWeb = true;
        //屏幕方向（v/h）
        public String screen;
        //宽高比例
        public float WHp = 0;
        //是否循环播放
        public bool loop = false;

        //只应用于login节点
        internal String check;
        internal bool isAutoCheck;

        public String mail;
        public int style;

        public static int STYLE_VIDEO = 11;
        public static int STYLE_AUDIO = 12;
        public static int STYLE_INWEB = 13;

        public DdNode(SdSource source) : base(source) {

        }

        protected override void OnDidInit() {
            donwAll = attrs.getInt("donwAll", 1) > 0;
            showNav = attrs.getInt("showNav", 1) > 0;
            showImg = attrs.getString("showImg");
            showWeb = attrs.getInt("showWeb", 1) > 0;
            screen = attrs.getString("screen");
            loop = attrs.getInt("loop", 0) > 0;

            //只应用于login节点
            check = attrs.getString("check");
            isAutoCheck = attrs.getInt("auto") > 0;

            mail = attrs.getString("mail");

            style = attrs.getInt("style", STYLE_VIDEO);

            if (TextUtils.isEmpty(screen) && style == STYLE_AUDIO) {
                screen = "v";
            }

            String w = attrs.getString("w");
            if (TextUtils.isEmpty(w) == false) {
                String h = attrs.getString("h");
                WHp = float.Parse(w) / float.Parse(h);
            }
        }

       

        //是否有分页
        public bool hasPaging() {
            return hasMacro() || string.IsNullOrEmpty(buildUrl) == false || "post" == method;
        }


        public bool isWebrun() {
            String run = attrs.getString("run");

            if (run == null)
                return false;

            return run.IndexOf("web") >= 0;
        }

        public bool isOutWebrun() {
            String run = attrs.getString("run");

            if (run == null)
                return false;

            return run.IndexOf("outweb") >= 0;
        }

        public String getWebUrl(String url) {
            if (attrs.contains("buildWeb") == false)
                return url;
            else
                return source.callJs(this, "buildWeb", url);
        }
    }
}
