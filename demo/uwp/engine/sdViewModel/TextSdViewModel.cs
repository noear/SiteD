using ddcat.uwp.models;
using ddcat.uwp.utils;
using ddcat.uwp.viewModels;
using Noear.Snacks;
using org.noear.sited;
using System;
using System.Collections.ObjectModel;

namespace ddcat.uwp.dao.engine.sdViewModel {
    public abstract class TextSdViewModel : ViewModelBase, ISdViewModel {
        public ObservableCollection<TxtModel> items { get; private set; } = new ObservableCollection<TxtModel>();
        public string referer;

        public TextSdViewModel(string referer) {
            this.referer = referer;
        }

        public void clear() {
            items.Clear();
        }
        public int total() {
            return items.Count;
        }
        public TxtModel get(int index) {
            return items[index];
        }

        //-------------------

        public void loadByConfig(SdNode config) {
            
        }

        public virtual void loadByJson(SdNode config, params string[] jsons) {
            if (jsons == null || jsons.Length == 0)
                return;

            foreach (String json in jsons) {
                loadByJsonData(config, json);
            }
        }

        protected void loadByJsonData(SdNode config, String json) {
            ONode list = null;

            ONode obj = ONode.tryLoad(json);
            if (obj.isObject) {
                list = obj.get("list").asArray();

                if (TextUtils.isEmpty(name)) {
                    name = obj.get("name").getString();
                    logo = obj.get("logo").getString();
                }
            }
            else {
                list = obj;
            }

            foreach (ONode n in list) {
                TxtModel txt = new TxtModel(referer, n.get("d").getString(), n.get("t").getInt(), n.get("c").getString());
                items.Add(txt);
            }
        }

        //从网页过来时，需要name,logo
        public String name;
        public String logo;
    }
}
