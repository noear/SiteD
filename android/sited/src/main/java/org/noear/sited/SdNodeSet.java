package org.noear.sited;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuety on 15/8/21.
 */
public class SdNodeSet implements ISdNode {

    public int nodeType(){return 2;}

    Map<String,ISdNode> _items;
    SdSource _source;

    protected SdNodeSet(SdSource source){
        _items  = new HashMap<>();
        _source = source;
    }

    protected SdNodeSet(SdSource source, Element element) {
        this(source);

        loadByElement(element);
    }

    protected void loadByElement(Element element){
        if(element==null)
            return;

        _items.clear();

        NodeList xList = element.getChildNodes();

        for (int i = 0, len = xList.getLength(); i < len; i++) {
            Node n1 = xList.item(i);
            if (n1.getNodeType() == Node.ELEMENT_NODE) {
                Element e1 = (Element) n1;

                if (e1.hasAttributes()) {//说明是Node类型
                    SdNode temp = new SdNode(_source, e1);
                    this.add(e1.getTagName(), temp);
                } else {//说明是Set类型
                    SdNodeSet temp = new SdNodeSet(_source, e1);
                    this.add(e1.getTagName(), temp);
                }
            }
        }
    }

    public Iterable<ISdNode> nodes(){
        return _items.values();
    }

    public ISdNode get(String name){
        if(_items.containsKey(name))
            return _items.get(name);
        else
            return new SdNode(_source,null);
    }

    protected void add(String name, ISdNode node){
        _items.put(name,node);
    }
}
