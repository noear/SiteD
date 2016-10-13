package org.noear.ddcat.dao.engine.sdVewModel;

import android.text.TextUtils;

import org.noear.ddcat.models.TxtModel;
import org.noear.ddcat.utils.ListEx;
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

public class TextSdViewModel extends ViewModelBase implements ISdViewModel {
    public final List<TxtModel> items = new ArrayList<>();
    public final String referer;

    public TextSdViewModel(String referer){
        this.referer = referer;
    }



    public void clear(){
        items.clear();
    }
    public int total(){
        return items.size();
    }
    public TxtModel get(int index){
        return items.get(index);
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

    protected void loadByJsonData(SdNode config, String json) {
        ONode list = null;

        ONode obj = ONode.tryLoad(json);
        if(obj.isObject()){
            list = obj.get("list").asArray();

            if(TextUtils.isEmpty(name)) {
                name = obj.get("name").getString();
                logo = obj.get("logo").getString();
            }
        }else{
            list = obj;
        }

        for (ONode n : list) {
            TxtModel txt = new TxtModel(referer, n.get("d").getString(), n.get("t").getInt(),  n.get("c").getString());
            items.add(txt);
        }
    }

    //从网页过来时，需要name,logo
    public String name;
    public String logo;

}
