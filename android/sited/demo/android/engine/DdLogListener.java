package org.noear.ddcat.dao.engine;

import org.noear.ddcat.dao.Setting;
import org.noear.ddcat.utils.LogWriter;
import org.noear.sited.SdLogListener;
import org.noear.sited.SdSource;

/**
 * Created by yuety on 16/2/1.
 */
public class DdLogListener implements SdLogListener {
    @Override
    public void run(SdSource source, String tag, String msg, Throwable tr) {
        if(Setting.isDeveloperModel()) {
            LogWriter.tryInit();
            LogWriter.loger.print(tag, msg, tr);

            if (tr != null) {
//                    HintUtil.show(tag + ".error::" + msg);
                LogWriter.error.print(source.url + "::\r\n" + tag, msg, null);
            }
        }
    }
}
