using ddcat.uwp.utils;
using org.noear.sited;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ddcat.uwp.dao.engine {
    public class DbUtil {
        public static void tryReward(DdSource source) {
            if (source != null && source.reward.isEmpty() == false) {

                AddinClientApi.tryLog(source, AddinClientApi.LOG_REWARD);

                if (source.reward.isWebrun()) {
                    String webUrl = source.reward.getUrl();
                    Navigation.showOutBrowser(webUrl);
                }
                else {
                    StringBuilder sb2 = new StringBuilder();
                    List<SdNode> pl2 = new List<SdNode>();

                    StringBuilder sb = new StringBuilder();
                    foreach (SdNode n1 in source.reward.items()) {
                        if (n1.attrs.count() == 0) {
                            //空行
                            sb.Append("\r\n");
                        }
                        else {

                            //标题
                            if (TextUtils.isEmpty(n1.title) == false) {
                                sb.Append(n1.title).Append("：");
                                sb.Append("\r\n");
                            }

                            //url(账号)
                            if (TextUtils.isEmpty(n1.url) == false) {
                                sb.Append(n1.url);
                                sb.Append("\r\n");

                                pl2.Add(n1);
                            }

                            //txt
                            if (TextUtils.isEmpty(n1.txt) == false) {
                                sb.Append(n1.txt);
                                sb.Append("\r\n");
                            }
                        }
                    }

                    String sdTitle = source.title + ".v" + source.ver;
                    String okBtn = "关闭";
                    if (pl2.Count > 0) {
                        if (pl2.Count == 1) {
                            sb2.Append(pl2[0].url);
                            okBtn = "复制账号并关闭";
                        }
                        else {
                            sb2.Append(pl2[0].url);
                            okBtn = "复制[" + pl2[0].title + "]账号并关闭";
                        }
                    }

                    if (TextUtils.isEmpty(source.reward.mail)) {
                        HintUtil.alert(source.reward.title, sb.ToString(), okBtn, ()=> {
                            if (sb2.Length > 0) {
                                FileUtil.copy(sb2.ToString());
                                HintUtil.show("已复制");
                            }
                        });
                    }
                    else {
                        HintUtil.confirm(source.reward.title, sb.ToString(), okBtn, "反馈问题", ( isOk )=> {
                            if (isOk) {
                                if (sb2.Length > 0) {
                                    FileUtil.copy(sb2.ToString());
                                    HintUtil.show("已复制");
                                }
                            }
                            else {
                                Navigation.showOutEmail(source.reward.mail, "插件反馈：" + sdTitle);
                            }
                        });
                    }
                }
            }
        }
    }
}
