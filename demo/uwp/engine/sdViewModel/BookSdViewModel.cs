using ddcat.uwp.models;
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

   public abstract class BookSdViewModel : ViewModelBase, ISdViewModel {
        public ObservableCollection<SectionModel> sections { get; private set; } = new ObservableCollection<SectionModel>();
        
        public String name { get; set; }
        public String author { get; set; }
        public String intro { get; set; }
        public String logo { get; set; }
        public String updateTime { get; set; }

        public bool isSectionsAsc;//输出的section是不是顺排的

        public String bookUrl;

        public BookSdViewModel(String url) {
            this.bookUrl = url;
        }

        public virtual void loadByConfig(SdNode config) { }

        public virtual void loadByJson(SdNode config, params String[] jsons) {
            if (jsons == null || jsons.Length == 0)
                return;

            if (DdSource.isBook(config)) {
                String json = jsons[0]; //不支持多个数据块加载
                ONode data = ONode.tryLoad(json);

                name = data.get("name").getString();
                author = data.get("author").getString();
                intro = data.get("intro").getString();
                logo = data.get("logo").getString();
                updateTime = data.get("updateTime").getString();

                isSectionsAsc = data.get("isSectionsAsc").getInt() > 0;//默认为倒排
            }

            foreach (String json in jsons) //支持多个数据块加载
            {
                ONode data = ONode.tryLoad(json);
                ONode sl = data.get("sections").asArray();

                foreach (ONode n in sl) {
                    SectionModel sec = newSection();
                    sec.name = n.get("name").getString();
                    sec.url = n.get("url").getString();

                    sec.bookName = name;
                    sec.bookUrl = bookUrl;
                    
                    sec.orgIndex = total();


                    sections.Add(sec);

                    onAddItem(sec);
                }
            }
        }

        public virtual void clear() {
            sections.Clear();
        }

        public int total() {
            return sections.Count;
        }

        public SectionModel get(int idx) {
            if (sections == null)
                return null;

            int len = sections.Count;
            if (idx >= len || idx < 0)
                return null;
            else
                return sections[idx];
        }

        protected abstract SectionModel newSection();

        protected virtual void onAddItem(SectionModel sec) {

        }
    }
}
