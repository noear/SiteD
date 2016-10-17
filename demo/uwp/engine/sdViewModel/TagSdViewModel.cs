using ddcat.uwp.models;
using ddcat.uwp.viewModels;
using Noear.Snacks;
using org.noear.sited;
using System;
using System.Collections.ObjectModel;

namespace ddcat.uwp.dao.engine.sdViewModel {
    public abstract class TagSdViewModel : ViewModelBase, ISdViewModel {
        public ObservableCollection<BookUpdateModel> list { get; private set; } = new ObservableCollection<BookUpdateModel>();

        //-------------------
        //
        public void loadByConfig(SdNode c) {

        }

        public void loadByJson(SdNode c, params string[] jsons) {
            if (jsons == null || jsons.Length == 0)
                return;

            foreach (String json in jsons) { //支持多个数据块加载
                ONode data = ONode.tryLoad(json);

                if (data.isArray) {
                    foreach (ONode n in data) {

                        String name = n.get("name").getString();

                        if (Session.isVip == 0 && SourceApi.isFilter(name)) //非vip进行过滤
                            continue;

                        BookUpdateModel b = new BookUpdateModel();

                        b.name = name;
                        b.url = n.get("url").getString();
                        b.logo = n.get("logo").getString();
                        b.author = n.get("author").getString();
                        b.newSection = n.get("newSection").getString();
                        b.updateTime = n.get("updateTime").getString();
                        b.status = n.get("status").getString();

                        list.Add(b);
                    }
                }
            }
        }

        public void clear() { list.Clear(); }
        public int total() { return list.Count; }
        public BookUpdateModel get(int index) {
            return list[index];
        }
    }
}
