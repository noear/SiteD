package org.noear.ddcat.dao.engine.sdVewModel;

import android.text.TextUtils;

import org.noear.ddcat.models.MediaModel;
import org.noear.ddcat.viewModels.ViewModelBase;
import org.noear.sited.ISdViewModel;
import org.noear.sited.SdNode;

import java.util.ArrayList;
import java.util.List;

import noear.snacks.ONode;

/**
 * Created by yuety on 16/9/28.
 */

public class MediaSdViewModel extends ViewModelBase implements ISdViewModel {
    public final List<MediaModel> items = new ArrayList<>();

    public int total(){ return items.size(); }
    public void clear(){
        items.clear();
    }
    public MediaModel get(int index){
        return items.get(index);
    }



    @Override
    public void loadByConfig(SdNode config) {

    }

    @Override
    public void loadByJson(SdNode config, String... jsons) {
        if(jsons == null || jsons.length==0)
            return;

        for(String json : jsons){
            if(json.startsWith("{") || json.startsWith("[")){
                ONode jList =  null;

                ONode obj = ONode.tryLoad(json);
                if(obj.isObject()){
                    jList = obj.get("list").asArray();

                    if(TextUtils.isEmpty(name)) {
                        name = obj.get("name").getString();
                        logo = obj.get("logo").getString();
                    }
                }else{
                    jList = obj;
                }

                for(ONode n1 : jList){
                    items.add(new MediaModel(n1.get("url").getString(),
                            n1.get("type").getString(),
                            n1.get("mime").getString(),
                            n1.get("logo").getString()));
                }
            }
            else {
                for (String url : json.split(";")) {
                    if (url.length() > 6) {
                        items.add(new MediaModel(url));
                    }
                }
            }
        }
    }

    //从网页过来时，需要name,logo
    public String name;
    public String logo;

}
