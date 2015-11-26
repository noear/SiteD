package org.noear.sited;

/**
 * Created by yuety on 15/10/10.
 */
public interface SdLogListener {
    void run(SdSource source, String tag, String msg, Throwable tr);
}
