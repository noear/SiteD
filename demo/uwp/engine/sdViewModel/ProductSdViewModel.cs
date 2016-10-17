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
    public abstract class ProductSdViewModel : ViewModelBase, ISdViewModel {
        public ObservableCollection<PicModel> pictures { get; private set; } = new ObservableCollection<PicModel>();

        public String logo { get; set; }
        public String name { get; set; }
        public String shop { get; set; }
        public String intro { get; set; }

        public String buyUrl { get; set; }

        public String bookUrl;

        public ProductSdViewModel(String url) {
            this.bookUrl = url;
        }

        public void loadByConfig(SdNode config) { }

        public virtual void loadByJson(SdNode config, params String[] jsons) {
            if (jsons == null || jsons.Length == 0)
                return;

            foreach (String json in jsons) {
                loadByJsonData(config, json);
            }
           
        }

        protected void loadByJsonData(SdNode config, String json) {
            ONode data = ONode.tryLoad(json);

            if (DdSource.isBook(config)) {
                if (TextUtils.isEmpty(shop)) {
                    logo = data.get("logo").getString();
                    name = data.get("name").getString();
                    shop = data.get("shop").getString();
                    intro = data.get("intro").getString();

                    buyUrl = data.get("buyUrl").getString();
                }
            }

            ONode sl = data.get("pictures").asArray();

            foreach (ONode n in sl) {
                PicModel pic = new PicModel(bookUrl, n.getString());

                pictures.Add(pic);
            }
        }


        //--------------
        public void clear() {
            pictures.Clear();
        }

        public int count() {
            return pictures.Count;
        }
       
    }
}
