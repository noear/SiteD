package org.noear.ddcat.dao.engine;

import org.noear.ddcat.App;
import org.noear.sited.ISdFactory;
import org.noear.sited.SdNode;
import org.noear.sited.SdNodeSet;
import org.noear.sited.SdSource;

/**
 * Created by yuety on 16/2/1.
 */
public class DdFactory implements ISdFactory {
    @Override
    public SdNode createNode(SdSource source) {
        return new DdNode(source);
    }

    @Override
    public SdNodeSet createNodeSet(SdSource source) {
        return new DdNodeSet(source);
    }

    @Override
    public SdSource createSource(String xml) throws Exception {
        return new DdSource(App.getCurrent(), xml);
    }
}
