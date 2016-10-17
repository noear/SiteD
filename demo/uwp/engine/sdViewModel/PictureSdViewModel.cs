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
    public abstract class PictureSdViewModel : ViewModelBase, ISdViewModel {
        public String bgUrl;

        public ObservableCollection<PicModel> items { get; private set; } = new ObservableCollection<PicModel>();

        public virtual void clear() {
            items.Clear();
        }

        public int total() {
            return items.Count;
        }

        public PicModel get(int index) {
            return items[index];
        }

        //=================
        //
        public void loadByConfig(SdNode config) {

        }

        /*
        支持
        ["","",""]
        或
        {bg:"",list:["","",""]}
        或
        {bg:"",list:[{url:"",time:"mm::ss.xx"},{...}]}
         或
        {bg:"",logo:"",name:"",list:[{url:"",time:"mm::ss.xx"},{...}]}
        */

        public virtual void loadByJson(SdNode config, params String[] jsons) {
            if (jsons == null || jsons.Length == 0)
                return;

            foreach (String json in jsons) {

                StateTag state = new StateTag();

                loadByJsonData(config, json, state);
            }
        }

        protected void loadByJsonData(SdNode config, String json, StateTag state) {
            ONode list = null;
            ONode obj = ONode.tryLoad(json);

            if (obj.isObject) {
                list = obj.get("list").asArray();
                String bg = obj.get("bg").getString();

                if (TextUtils.isEmpty(bg) == false) {
                    bgUrl = bg;
                }

                if (TextUtils.isEmpty(name)) {
                    name = obj.get("name").getString();
                    logo = obj.get("logo").getString();
                }

            }
            else {
                list = obj;
            }

            foreach (ONode n in list) {
                PicModel pic = null;

                if (n.isObject) {
                    pic = new PicModel(section(), n.get("url").getString(), n.get("time").getInt(), state.value);
                }
                else {
                    pic = new PicModel(section(), n.getString(), 0, state.value);
                }

                pic.cacheID = items.Count;
                doAddItem(pic, state);

                state.value++;
            }
        }

        //从网页过来时，需要name,logo
        public String name { get; set; }
        public String logo { get; set; }

        protected abstract void doAddItem(PicModel pic, StateTag state);

        protected abstract SectionModel section();
    }
}
