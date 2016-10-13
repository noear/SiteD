package org.noear.ddcat.dao.engine.sdVewModel;

import android.text.TextUtils;

import org.noear.ddcat.models.PicModel;
import org.noear.ddcat.models.SectionModel;
import org.noear.ddcat.utils.StateTag;
import org.noear.ddcat.viewModels.ViewModelBase;
import org.noear.sited.ISdViewModel;
import org.noear.sited.SdNode;

import java.util.ArrayList;
import java.util.List;

import noear.snacks.ONode;

/**
 * Created by yuety on 16/9/28.
 */

public abstract class PictureSdViewModel extends ViewModelBase implements ISdViewModel {
    public String bgUrl;

    public final List<PicModel> items = new ArrayList<>();

    public void clear(){
        items.clear();
    }

    public int total(){
        return items.size();
    }

    public PicModel get(int index){
        return items.get(index);
    }


    @Override
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
    @Override
    public void loadByJson(SdNode config, String... jsons) {
        if (jsons == null || jsons.length == 0)
            return;

        for (String json : jsons) {

            StateTag state = new StateTag();

            loadByJsonData(config, json, state);
        }
    }

    protected void loadByJsonData(SdNode config, String json, StateTag state) {
        ONode list = null;
        ONode obj = ONode.tryLoad(json);

        if (obj.isObject()) {
            list = obj.get("list").asArray();
            String bg = obj.get("bg").getString();

            if (TextUtils.isEmpty(bg) == false) {
                bgUrl = bg;
            }

            if(TextUtils.isEmpty(name)) {
                name = obj.get("name").getString();
                logo = obj.get("logo").getString();
            }

        } else {
            list = obj;
        }

        for (ONode n : list) {
            PicModel pic = null;

            if (n.isObject()) {
                pic = new PicModel(section(), n.get("url").getString(), n.get("time").getInt(), state.value);
            } else {
                pic = new PicModel(section(), n.getString(), 0, state.value);
            }

            pic.cacheID = items.size();
            doAddItem(pic, state);

            state.value++;
        }
    }

    //从网页过来时，需要name,logo
    public String name;
    public String logo;

    protected abstract void doAddItem(PicModel pic, StateTag state);

    protected abstract SectionModel section();
}
