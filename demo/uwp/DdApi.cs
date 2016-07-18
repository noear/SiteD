using ddcat.uwp.utils;
using org.noear.sited;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace ddcat.uwp.dao.engine {
    public class DdApi : SdApi {
        /*
         * v17: 增加object节点配置
         * v16: 增加buildCookie;增加处理分支；增加loop属性
         * v15: 增加login、trace支持;;;协加无目录视频插件支持（dtype=7）;;调整节点插件的处理方式
         * v14: 视插插件，支持：web://
         * v13: 架构调整：1.将节点与属性打通；2.分离成内核与业务定制2部分；3.抽象化主节点（即可以随意定名字）
         * v12: 增加多数据块的加载支持
         * v11: 优化周边插件（与阿里百川sdk进一步整合）
         * v10: 增加图片、资讯、周边插件
         * v9 : 增加对#hash的过滤处理；增加hasMicroDefine()
         * v8 : 增加parseUrl()->"xxx;xxxx;xxx"的支持
         * */
        public const int version = 16;

        public static string unsuan(string str, string key) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0, len = str.Length; i < len; i++) {
                if (i % 2 == 0) {
                    sb.Append(str[i]);
                }
            }

            str = sb.ToString();
            str = Base64Util.decode(str);
            key = key + "ro4w78Jx";

            var coder = Encoding.UTF8;

            byte[] data = coder.GetBytes(str);
            byte[] keyData = coder.GetBytes(key);
            int keyIndex = 0;

            for (int x = 0; x < data.Length; x++) {
                data[x] = (byte)(data[x] ^ keyData[keyIndex]);
                if (++keyIndex == keyData.Length) {
                    keyIndex = 0;
                }
            }
            str = coder.GetString(data);

            return Base64Util.decode(str);
        }
    }
}
