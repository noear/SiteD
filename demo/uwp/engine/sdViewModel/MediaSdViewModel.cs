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
    public class MediaSdViewModel : ViewModelBase, ISdViewModel {
        public ObservableCollection<MediaModel> items { get; private set; } = new ObservableCollection<MediaModel>();


        public int total() { return items.Count; }
        public void clear() {
            items.Clear();
        }
        public MediaModel get(int index) {
            return items[index];
        }

        //-----------------------------------
        //

        public void loadByConfig(SdNode config) {
            
        }

        public virtual void loadByJson(SdNode config, params string[] jsons) {
            if (jsons == null || jsons.Length == 0)
                return;

            foreach (String json in jsons) {
                if (json.StartsWith("{") || json.StartsWith("[")) {
                    ONode jList = null;

                    ONode obj = ONode.tryLoad(json);
                    if (obj.isObject) {
                        jList = obj.get("list").asArray();

                        if (TextUtils.isEmpty(name)) {
                            name = obj.get("name").getString();
                            logo = obj.get("logo").getString();
                        }
                    }
                    else {
                        jList = obj;
                    }

                    foreach (ONode n1 in jList) {
                        items.Add(new MediaModel(n1.get("url").getString(),
                                n1.get("type").getString(),
                                n1.get("mime").getString(),
                                n1.get("logo").getString()));
                    }
                }
                else {
                    foreach (String url in json.Split(';')) {
                        if (url.Length > 6) {
                            items.Add(new MediaModel(url));
                        }
                    }
                }
            }
        }

        //从网页过来时，需要name,logo
        public String name;
        public String logo;
    }
}
