using ddcat.uwp.utils;
using org.noear.sited;
using System;

namespace ddcat.uwp.dao.engine {
    public class DdNode : SdNode {
        public DdSource s() {
            return (DdSource)source;
        }

        //临时数据寄存（任意）
        public int dataTag;

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

        public DdNode(SdSource source) : base(source) {

        }

        protected override void OnDidInit() {
            
            showWeb = attrs.getInt("showWeb", 1) > 0;
            screen = attrs.getString("screen");
            loop = attrs.getInt("loop", 0) > 0;

            //只应用于login节点
            check = attrs.getString("check");
            isAutoCheck = attrs.getInt("auto") > 0;

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
    }
}
