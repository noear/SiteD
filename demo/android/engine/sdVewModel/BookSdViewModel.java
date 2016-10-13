package org.noear.ddcat.dao.engine.sdVewModel;

import android.text.TextUtils;

import org.noear.ddcat.dao.engine.DdSource;
import org.noear.ddcat.models.SectionModel;
import org.noear.ddcat.viewModels.ViewModelBase;
import org.noear.sited.ISdViewModel;
import org.noear.sited.SdNode;

import java.util.ArrayList;
import java.util.List;

import noear.snacks.ONode;

/**
 * Created by yuety on 16/9/28.
 */

public class BookSdViewModel extends ViewModelBase implements ISdViewModel {
    public final List<SectionModel> sections = new ArrayList<>();

    public String name;
    public String author;
    public String intro;
    public String logo;
    public String updateTime;
    public boolean isSectionsAsc;//输出的section是不是顺排的

    public String bookUrl;

    public BookSdViewModel(String url){
        this.bookUrl = url;
    }


    @Override
    public void loadByConfig(SdNode config){}

    @Override
    public void loadByJson(SdNode config, String... jsons) {
        if (jsons == null || jsons.length == 0)
            return;

        for (String json : jsons) {
            loadByJsonData(config, json);
        }
    }

    private void loadByJsonData(SdNode config, String json){
        ONode data = ONode.tryLoad(json);

        if(DdSource.isBook(config)) {
            if(TextUtils.isEmpty(name)) {
                name = data.get("name").getString();
                author = data.get("author").getString();
                intro = data.get("intro").getString();
                logo = data.get("logo").getString();
                updateTime = data.get("updateTime").getString();

                isSectionsAsc = data.get("isSectionsAsc").getInt() > 0;//默认为倒排
            }
        }


        ONode sl = data.get("sections").asArray();

        for(ONode n : sl){
            SectionModel sec = new SectionModel();
            sec.name = n.get("name").getString();
            sec.url  = n.get("url").getString();

            sec.orgIndex = total();

            sections.add(sec);

            onAddItem(sec);
        }
    }

    //--------------
    public  void clear() { sections.clear();  }

    public int total()  { return sections.size(); }

    public SectionModel get(int idx)
    {
        if(sections == null)
            return null;

        int len = sections.size();
        if(idx>=len || idx<0)
            return null;
        else
            return sections.get(idx);
    }

    protected void onAddItem(SectionModel sec){

    }
}
