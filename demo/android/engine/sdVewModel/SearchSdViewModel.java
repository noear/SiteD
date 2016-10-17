package org.noear.ddcat.dao.engine.sdVewModel;

import android.text.TextUtils;

import org.noear.ddcat.dao.SourceApi;
import org.noear.ddcat.dao.engine.DdNode;
import org.noear.ddcat.models.BookSearchModel;
import org.noear.ddcat.viewModels.ViewModelBase;
import org.noear.sited.ISdViewModel;
import org.noear.sited.SdNode;

import noear.snacks.ONode;

/**
 * Created by yuety on 16/9/29.
 */

public abstract class SearchSdViewModel extends ViewModelBase implements ISdViewModel {
    @Override
    public void loadByConfig(SdNode c) {
        DdNode config = (DdNode)c;

        if (doFilter(c.title)) {
            return;
        }

        BookSearchModel b = new BookSearchModel();

        DdNode cfg = config.s().search;

        b._dtype = cfg.dtype();
        b.btype = cfg.btype();
        b.name = c.title;
        b.url = c.url;
        b.logo = c.logo;
        b.updateTime = "";
        b.newSection = "";
        b.author = "";
        b.status = "";
        b.source = config.source.title;

        doAddItem(b);
    }

    @Override
    public void loadByJson(SdNode c, String... jsons) {
        if (jsons == null || jsons.length == 0)
            return;

        DdNode config = (DdNode) c;

        for (String json : jsons) { //支持多个数据块加载
            ONode data = ONode.tryLoad(json);

            if (data.isArray()) {
                for (ONode n : data) {
                    String name = n.get("name").getString();

                    if (doFilter(name)) {
                        continue;
                    }

                    BookSearchModel b = new BookSearchModel();

                    b.name = name;
                    b.url = n.get("url").getString();
                    b.logo = n.get("logo").getString();
                    b.updateTime = n.get("updateTime").getString();
                    b.newSection = n.get("newSection").getString();
                    b.author = n.get("author").getString();
                    b.status = n.get("status").getString();
                    b.source = config.source.title;
                    b.btag   = n.get("btag").getString();

                    DdNode cfg = config.s().book(b.url);
                    b._dtype = cfg.dtype();
                    b.btype = cfg.btype();

                    doAddItem(b);
                }
            }
        }
    }

    protected abstract boolean doFilter(String name);
    protected abstract void doAddItem(BookSearchModel item);

    public abstract void clear();
    public abstract int total();

}
