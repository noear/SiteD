using ddcat.uwp.models;
using ddcat.uwp.utils;
using ddcat.uwp.viewModels;
using Noear.Snacks;
using org.noear.sited;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ddcat.uwp.dao.engine.sdViewModel {
    public abstract class MainSdViewModel : ViewModelBase, ISdViewModel {
        public ObservableCollection<TagModel> tagList { get; private set; } = new ObservableCollection<TagModel>();
        public ObservableCollection<BookModel> hotList { get; private set; } = new ObservableCollection<BookModel>();
        public ObservableCollection<BookUpdateModel> updateList { get; private set; } = new ObservableCollection<BookUpdateModel>();

        public void clear() {
            tagList.Clear();
            hotList.Clear();
            updateList.Clear();
        }

        public int total() {
            return tagList.Count + hotList.Count + updateList.Count;
        }

        //------------------
        //

        public void loadByConfig(SdNode config) {
            if (DdSource.isHots(config)) {
                hotList.Clear();

                foreach (SdNode t1 in config.items()) {
                    BookModel b = new BookModel();
                    b.name = t1.title;
                    b.url = t1.url;
                    b.logo = t1.logo;

                    hotList.Add(b);
                }
                return;
            }

            if (DdSource.isUpdates(config)) {
                updateList.Clear();

                foreach (SdNode t1 in config.items()) {
                    BookUpdateModel b = new BookUpdateModel();
                    b.name = t1.title;
                    b.url = t1.url;
                    b.logo = t1.logo;


                    updateList.Add(b);
                }
                return;
            }

            if (DdSource.isTags(config)) {
                tagList.Clear();

                foreach (SdNode t1 in config.items()) {
                    doAddTagItem(t1);
                }
                return;
            }
        }

        public void loadByJson(SdNode config,params String[] jsons) {
            if (jsons == null || jsons.Length == 0)
                return;

            foreach (String json in jsons) { //支持多个数据块加载
                ONode data = ONode.tryLoad(json).asArray();

                if (DdSource.isHots(config)) {

                    foreach (ONode n in data) {
                        BookModel b = new BookModel();
                        b.name = n.get("name").getString();
                        b.url = n.get("url").getString();
                        b.logo = n.get("logo").getString();

                        hotList.Add(b);
                    }
                    return;
                }

                if (DdSource.isUpdates(config)) {

                    foreach (ONode n in data) {
                        BookUpdateModel b = new BookUpdateModel();
                        b.name = n.get("name").getString();
                        b.url = n.get("url").getString();
                        b.logo = n.get("logo").getString();
                        b.newSection = n.get("newSection").getString();
                        b.updateTime = n.get("updateTime").getString();

                        updateList.Add(b);
                    }
                    return;
                }

                if (DdSource.isTags(config)) {

                    foreach (ONode n in data) {
                        DdNode t1 = new DdNode(null);
                        t1.title = n.get("title").getString();
                        t1.url = n.get("url").getString();
                        t1.group = n.get("group").getString();
                        t1.logo = n.get("logo").getString();

                        doAddTagItem(t1);
                    }
                }
            }
        }

        private void doAddTagItem(SdNode t1) {
            if (TextUtils.isEmpty(t1.group) == false) {
                int temp = tagList.Count % 3;
                if (temp > 0) {
                    temp = 3 - temp;
                }

                while (temp > 0) {
                    tagList.Add(new TagModel("", null, 1, tagList.Count));
                    temp--;
                }

                tagList.Add(new TagModel("", null, 11, tagList.Count));
                tagList.Add(new TagModel(t1.group, null, 10, tagList.Count));
                tagList.Add(new TagModel("", null, 11, tagList.Count));

            }

            if (TextUtils.isEmpty(t1.title) == false) {
                tagList.Add(new TagModel(t1.title, t1.url, 0, tagList.Count));
            }
        }
    }
}
