package org.noear.ddcat.dao.engine.sdVewModel;

import org.noear.ddcat.dao.Session;
import org.noear.ddcat.dao.SourceApi;
import org.noear.ddcat.models.BookUpdateModel;
import org.noear.ddcat.models.TxtModel;
import org.noear.ddcat.viewModels.ViewModelBase;
import org.noear.sited.ISdViewModel;
import org.noear.sited.SdNode;

import java.util.ArrayList;
import java.util.List;

import noear.snacks.ONode;

/**
 * Created by yuety on 16/9/28.
 */

public abstract class TagSdViewModel extends ViewModelBase implements ISdViewModel {
    public List<BookUpdateModel> list = new ArrayList<>();


    @Override
    public void loadByConfig(SdNode config){}

    @Override
    public void loadByJson(SdNode config, String... jsons) {
        if(jsons == null || jsons.length == 0)
            return;

        for(String json : jsons) { //支持多个数据块加载
            ONode data = ONode.tryLoad(json);

            if(data.isArray()) {
                for (ONode n : data) {

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

                    list.add(b);
                }
            }
        }
    }

    public  void clear() { list.clear(); }
    public int total() { return list.size(); }
    public BookUpdateModel get(int index){
        return list.get(index);
    }
}
