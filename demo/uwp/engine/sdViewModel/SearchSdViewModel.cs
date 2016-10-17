using ddcat.uwp.models;
using ddcat.uwp.viewModels;
using Noear.Snacks;
using org.noear.sited;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ddcat.uwp.dao.engine.sdViewModel {
    public abstract class SearchSdViewModel : ViewModelBase, ISdViewModel {
        public virtual void loadByConfig(SdNode c) {
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

        public virtual void loadByJson(SdNode c, params string[] jsons) {
            if (jsons == null || jsons.Length == 0)
                return;

            DdNode config = (DdNode)c;

            foreach (String json in jsons) { //支持多个数据块加载
                ONode data = ONode.tryLoad(json);

                if (data.isArray) {
                    foreach (ONode n in data) {
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
                        b.btag = n.get("btag").getString();

                        DdNode cfg = config.s().book(b.url);
                        b._dtype = cfg.dtype();
                        b.btype = cfg.btype();

                        doAddItem(b);
                    }
                }
            }
        }

        protected abstract bool doFilter(String name);
        protected abstract void doAddItem(BookSearchModel item);

        public abstract void clear();
        public abstract int total();
    }
}
