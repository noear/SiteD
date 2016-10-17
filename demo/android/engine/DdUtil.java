package org.noear.ddcat.dao.engine;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.noear.ddcat.Navigation;
import org.noear.ddcat.R;
import org.noear.ddcat.controller.ActivityBase;
import org.noear.ddcat.controller.FragmentBase;
import org.noear.ddcat.dao.AddinClientApi;
import org.noear.ddcat.dao.ImgLoader;
import org.noear.ddcat.utils.Base32Util;
import org.noear.ddcat.utils.DisplayUtil;
import org.noear.ddcat.utils.FileUtil;
import org.noear.ddcat.dao.HintUtil;
import org.noear.ddcat.utils.ResUtil;
import org.noear.ddcat.viewModels.HomeViewModel;
import org.noear.sited.SdNode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuety on 16/9/11.
 */

public class DdUtil {
    private static TextView getTxtView(ActivityBase activity) {
        TextView txt = new TextView(activity);
        txt.setTextColor(Color.BLACK);

        return txt;
    }

    private static boolean showExpr(FragmentBase from, String expr) {
        if (TextUtils.isEmpty(expr) == false) {
            if (expr.startsWith("sited://")) {

                if(dialog!= null && dialog.get()!=null) {
                    dialog.get().cancel();
                }

                HomeViewModel.loadByUri(from, expr);
            } else {
                Navigation.showOutBrowser(from.activity, expr);
            }
            return true;
        } else {
            return false;
        }
    }

    static WeakReference<AlertDialog> dialog = null;

    public static void tryReward(FragmentBase from, DdSource source){
        if (source != null && source.reward.isEmpty() == false) {
            ActivityBase activity = from.activity;


            AddinClientApi.tryLog(source, AddinClientApi.LOG_REWARD);

            if (source.reward.isWebrun()) {
                String webUrl = source.reward.getUrl();
                Navigation.showBrowser(activity, webUrl);
            } else {
                LinearLayout view = new LinearLayout(activity);
                view.setOrientation(LinearLayout.VERTICAL);

                for (SdNode n1 : source.reward.items()) {

                    if (n1.attrs.count() == 0) {
                        TextView txt = getTxtView(activity);
                        view.addView(txt);
                    } else {
                        //标题
                        if (TextUtils.isEmpty(n1.title) == false) {
                            TextView txt = getTxtView(activity);
                            txt.getPaint().setFakeBoldText(true);
                            txt.setText(n1.title);
                            view.addView(txt);
                        }

                        //url(账号)
                        if (TextUtils.isEmpty(n1.url) == false) {
                            TextView txt = getTxtView(activity);
                            txt.setText(n1.url);

                            txt.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                            txt.setOnClickListener((v) -> {
                                if(showExpr(from, n1.expr) == false){
                                    FileUtil.copy(n1.url);
                                    HintUtil.show(R.string.state_copy);
                                }
                            });

                            txt.setOnLongClickListener((v)->{
                                FileUtil.copy(n1.url);
                                HintUtil.show(R.string.state_copy);
                                return true;
                            });

                            view.addView(txt);
                        }

                        //txt
                        if (TextUtils.isEmpty(n1.txt) == false) {
                            TextView txt = getTxtView(activity);
                            txt.setText(n1.txt);
                            view.addView(txt);
                        }

                        //logo(图片)
                        if (TextUtils.isEmpty(n1.logo) == false) {
                            int dp100 = DisplayUtil.dip2px(100);
                            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(dp100, dp100);

                            ImageView img = new ImageView(activity);
                            img.setBackgroundColor(Color.WHITE);
                            view.addView(img, lp2);

                            ImgLoader.displayImage(img, n1.logo, null);

                            img.setOnClickListener((v)->{
                                showExpr(from, n1.expr);
                            });
                        }
                    }
                }


                AlertDialog d1 = null;
                if (TextUtils.isEmpty(source.reward.mail)) {
                    //ok
                    d1 = HintUtil.showView(activity, source.reward.title, view, 0, R.string.btn_close, (isOk) -> {

                    });
                } else {
                    //ok
                    d1 = HintUtil.showView(activity, source.reward.title, view, R.string.btn_feedback, R.string.btn_close, (isOk) -> {
                        if (isOk) {
                            String sdTitle = source.title + ".v" + source.ver;

                            Navigation.showOutEmail(activity, source.reward.mail,
                                    ResUtil.getString(R.string.hint_addin_feedback) + sdTitle);
                        }
                    });
                }

                dialog = new WeakReference<AlertDialog>(d1);
            }
        }
    }
}
