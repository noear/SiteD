package org.noear.ddcat.dao.engine.sdVewModel;

import android.text.TextUtils;

import org.noear.ddcat.dao.engine.DdNode;
import org.noear.ddcat.dao.engine.DdSource;
import org.noear.ddcat.models.BookModel;
import org.noear.ddcat.models.BookUpdateModel;
import org.noear.ddcat.models.TagModel;
import org.noear.ddcat.viewModels.ViewModelBase;
import org.noear.sited.ISdViewModel;
import org.noear.sited.SdNode;

import java.util.ArrayList;
import java.util.List;

import noear.snacks.ONode;

/**
 * Created by yuety on 16/9/28.
 */

public class MainSdViewModel extends ViewModelBase implements ISdViewModel {
    public final List<TagModel> tagList = new ArrayList<>();
    public final List<BookModel> 	    hotList = new ArrayList<>();
    public final List<BookUpdateModel>  updateList  = new ArrayList<>();

    public void clear() {
        tagList.clear();
        hotList.clear();
        updateList.clear();
    }

    public int total(){
        return tagList.size() + hotList.size() + updateList.size();
    }

    @Override
    public void loadByConfig(SdNode config) {
        if (DdSource.isHots(config)) {
            hotList.clear();

            for (SdNode t1 : config.items()) {
                BookModel b = new BookModel();
                b.name = t1.title;
                b.url = t1.url;
                b.logo = t1.logo;

                hotList.add(b);
            }
            return;
        }

        if (DdSource.isUpdates(config)) {
            updateList.clear();

            for (SdNode t1 : config.items()) {
                BookUpdateModel b = new BookUpdateModel();
                b.name = t1.title;
                b.url = t1.url;
                b.logo = t1.logo;


                updateList.add(b);
            }
            return;
        }

        if (DdSource.isTags(config)) {
            tagList.clear();

            for (SdNode t1 : config.items()) {
                doAddTagItem(t1);
            }
            return;
        }
    }

    @Override
    public void loadByJson(SdNode config, String... jsons) {
        if(jsons == null || jsons.length==0)
            return;

        for(String json : jsons){ //支持多个数据块加载
            ONode data = ONode.tryLoad(json).asArray();

            if(DdSource.isHots(config)) {

                for(ONode n:data){
                    BookModel b = new BookModel();
                    b.name   = n.get("name").getString();
                    b.url    = n.get("url").getString();
                    b.logo   = n.get("logo").getString();

                    hotList.add(b);
                }
                return;
            }

            if(DdSource.isUpdates(config)){

                for(ONode n:data){
                    BookUpdateModel b = new BookUpdateModel();
                    b.name   	 = n.get("name").getString();
                    b.url    	 = n.get("url").getString();
                    b.logo       = n.get("logo").getString();
                    b.newSection = n.get("newSection").getString();
                    b.updateTime = n.get("updateTime").getString();

                    updateList.add(b);
                }
                return;
            }

            if(DdSource.isTags(config)){

                for(ONode n:data){
                    DdNode t1 = new DdNode(null);
                    t1.title = n.get("title").getString();
                    t1.url   = n.get("url").getString();
                    t1.group = n.get("group").getString();
                    t1.logo  = n.get("logo").getString();

                    doAddTagItem(t1);
                }
            }
        }
    }

    private  void doAddTagItem(SdNode t1) {
        if (TextUtils.isEmpty(t1.group) == false) {
            int temp = tagList.size() % 3;
            if (temp > 0) {
                temp = 3 - temp;
            }

            while (temp > 0) {
                tagList.add(new TagModel("", null, 1));
                temp--;
            }

            tagList.add(new TagModel("", null, 11));
            tagList.add(new TagModel(t1.group, null, 10));
            tagList.add(new TagModel("", null, 11));

        }

        if (TextUtils.isEmpty(t1.title) == false) {
            tagList.add(new TagModel(t1.title, t1.url, 0));
        }
    }
}
