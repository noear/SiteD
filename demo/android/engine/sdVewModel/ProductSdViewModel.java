package org.noear.ddcat.dao.engine.sdVewModel;

import android.text.TextUtils;

import org.noear.ddcat.dao.engine.DdSource;
import org.noear.ddcat.models.PicModel;
import org.noear.ddcat.models.SectionModel;
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

public class ProductSdViewModel extends ViewModelBase implements ISdViewModel {
    public final List<PicModel> pictures = new ArrayList<>();

    public String logo;
    public String name;
    public String shop;
    public String intro;

    public String buyUrl;

    public String bookUrl;

    public ProductSdViewModel(String url){
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

    protected void loadByJsonData(SdNode config, String json){
        ONode data = ONode.tryLoad(json);

        if (DdSource.isBook(config)) {
            if(TextUtils.isEmpty(shop)) {
                logo = data.get("logo").getString();
                name = data.get("name").getString();
                shop = data.get("shop").getString();
                intro = data.get("intro").getString();

                buyUrl = data.get("buyUrl").getString();
            }
        }

        ONode sl = data.get("pictures").asArray();

        for (ONode n : sl) {
            PicModel pic = new PicModel(bookUrl, n.getString());

            pictures.add(pic);
        }
    }


    //--------------
    public  void clear() {
        pictures.clear();
    }
    public int total()
    {
        return pictures.size();
    }
    public PicModel get(int index){
        return pictures.get(index);
    }

}
