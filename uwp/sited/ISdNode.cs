using System;

namespace org.noear.sited
{
    public interface ISdNode
    {
        String nodeName();
        int nodeType();
        bool isEmpty();
        SdNode nodeMatch(String url);
    }
}
