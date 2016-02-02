package org.noear.sited;

import android.app.Application;

/**
 * Created by yuety on 16/2/1.
 */
public class SdNodeFactory {
    public SdNode createNode(SdSource source) {
        return new SdNode(source);
    }

    public SdNodeSet createNodeSet(SdSource source) {
        return new SdNodeSet(source);
    }
}
