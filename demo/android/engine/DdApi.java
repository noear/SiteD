package org.noear.ddcat.dao.engine;

import org.noear.sited.SdApi;

/**
 * Created by yuety on 16/2/1.
 */
public class DdApi extends SdApi {
    /*
    * v13: 架构调整：1.将节点与属性打通；2.分离成内核与业务定制2部分；3.抽象化主节点（即可以随意定名字）
    * v12: 增加多数据块的加载支持
    * v11: 优化周边插件（与阿里百川sdk进一步整合）
    * v10: 增加图片、资讯、周边插件
    * v9 : 增加对#hash的过滤处理；增加hasMicroDefine()
    * v8 : 增加parseUrl()->"xxx;xxxx;xxx"的支持
    * */
    public final static int version = 13;
}
