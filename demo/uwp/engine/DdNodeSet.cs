using org.noear.sited;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ddcat.uwp.dao.engine {
    public class DdNodeSet :SdNodeSet{
       

        public DdSource s() {
            return (DdSource)source;
        }



        public String durl;//数据url（url是给外面看的；durl是真实的地址）
        public bool showWeb;

        public DdNodeSet(SdSource source):base(source) {
        }
        
        public override void OnDidInit() {
            showWeb = attrs.getInt("showWeb", 1) > 0;
            durl = attrs.getString("durl", source.url);
        }
    }
}
