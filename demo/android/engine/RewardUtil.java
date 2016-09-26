package org.noear.ddcat.dao.engine;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.noear.ddcat.Navigation;
import org.noear.ddcat.R;
import org.noear.ddcat.controller.ActivityBase;
import org.noear.ddcat.dao.AddinClientApi;
import org.noear.ddcat.utils.DisplayUtil;
import org.noear.ddcat.utils.FileUtil;
import org.noear.ddcat.dao.HintUtil;
import org.noear.ddcat.utils.ResUtil;
import org.noear.sited.SdNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuety on 16/9/11.
 */

public class RewardUtil {
    public static void tryReward(ActivityBase activity, DdSource source){
        if (source != null && source.reward.isEmpty() == false) {

            AddinClientApi.tryLog(source, AddinClientApi.LOG_REWARD);

            if (source.reward.isWebrun()) {
                String webUrl = source.reward.getUrl();
                Navigation.showBrowser(activity, webUrl);
            } else {
                LinearLayout view = new LinearLayout(activity);
                view.setOrientation(LinearLayout.VERTICAL);

                int db10 = DisplayUtil.px2dip(20);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0,db10,0,db10);


                for (SdNode n1 : source.reward.items()) {
                    TextView txt = new TextView(activity);
                    txt.setTextColor(Color.BLACK);

                    if (n1.attrs.count() == 0) {
                        view.addView(txt);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        //标题
                        if (TextUtils.isEmpty(n1.title) == false) {
                            sb.append(n1.title).append("：");
                        }

                        //url(账号)
                        if (TextUtils.isEmpty(n1.url) == false) {
                            sb.append(n1.url);

                            txt.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                            txt.setOnClickListener((v) -> {
                                FileUtil.copy(n1.url);
                                HintUtil.show(R.string.state_copy);
                            });
                        }

                        //txt
                        if (TextUtils.isEmpty(n1.txt) == false) {
                            if(sb.length()>0){
                                sb.append("\r\n");
                            }
                            sb.append(n1.txt);
                        }
                        txt.setText(sb.toString());

                        if(TextUtils.isEmpty(n1.url) == false) {
                            view.addView(txt, lp);
                        }else{
                            view.addView(txt);
                        }
                    }
                }


                if (TextUtils.isEmpty(source.reward.mail)) {
                    //ok
                    HintUtil.showView(activity, source.reward.title, view, 0, R.string.btn_close, (isOk) -> {

                    });
                } else {
                    //ok
                    HintUtil.showView(activity, source.reward.title, view, R.string.btn_feedback, R.string.btn_close, (isOk) -> {
                        if (isOk) {
                            String sdTitle = source.title + ".v" + source.ver;

                            Navigation.showOutEmail(activity, source.reward.mail,
                                    ResUtil.getString(R.string.hint_addin_feedback) + sdTitle);
                        }
                    });
                }
            }
        }
    }
}
