package org.noear.sited;

/**
 * Created by yuety on 16/2/1.
 */
public interface ISdFactory {
    SdNode createNode(SdSource source);
    SdNodeSet createNodeSet(SdSource source);
    SdSource createSource(String xml) throws Exception;
}
