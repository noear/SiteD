using ddcat.uwp.utils;
using org.noear.sited;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

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

        public DdNode(SdSource source) : base(source) {

        }

        protected override void OnDidInit() {
            
            showWeb = attrs.getInt("showWeb", 1) > 0;
            screen = attrs.getString("screen");


            String w = attrs.getString("w");
            if (TextUtils.isEmpty(w) == false) {
                String h = attrs.getString("h");
                WHp = float.Parse(w) / float.Parse(h);
            }
        }


        private String _trySuffix;
        public String[] getSuffixUrl(String url) {
            if (_trySuffix == null)
                _trySuffix = attrs.getString("trySuffix");

            if (string.IsNullOrEmpty(_trySuffix) || string.IsNullOrEmpty(url))
                return new String[] { url };
            else {
                String[] exts = _trySuffix.Split('|');
                String[] urls = new String[exts.Length];
                for (int i = 0, len = exts.Length; i < len; i++) {
                    urls[i] = Regex.Replace(url, _trySuffix, exts[i]);
                }
                return urls;
            }
        }

        //是否有分页
        public bool hasPaging() {
            return hasMacro() || string.IsNullOrEmpty(buildUrl) == false;
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

        public String getWebOnloadCode() {
            return attrs.getString("web_onload");
        }
    }
}
