package org.noear.sited;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuety on 15/8/21.
 */
public class SdNodeSet implements ISdNode{

    Map<String,ISdNode> _items;

    public final SdSource source;

    //---------------

    public SdNodeSet(SdSource s){
        _items  = new HashMap<>();
        source = s;
    }

    public void OnDidInit(){

    }

    public int nodeType(){return 2;}
    public final SdAttributeList attrs = new SdAttributeList();


    protected SdNodeSet buildForNode(Element element) {
        if(element==null)
            return this;

        _items.clear();
        attrs.clear();

        {
            NamedNodeMap temp = element.getAttributes();
            for (int i = 0, len = temp.getLength(); i < len; i++) {
                Node p = temp.item(i);
                attrs.set(p.getNodeName(), p.getNodeValue());
            }
        }


        NodeList xList = element.getChildNodes();

        for (int i = 0, len = xList.getLength(); i < len; i++) {
            Node n1 = xList.item(i);
            if (n1.getNodeType() == Node.ELEMENT_NODE) {
                Element e1 = (Element) n1;

                if (e1.hasAttributes()) {//说明是Node类型
                    SdNode temp = Util.createNode(source).buildForNode(e1);
                    this.add(e1.getTagName(), temp);
                } else {//说明是Set类型
                    SdNodeSet temp = Util.createNodeSet(source).buildForNode(e1);
                    this.add(e1.getTagName(), temp);
                }
            }
        }

        OnDidInit();
        return this;
    }

    public Iterable<ISdNode> nodes(){
        return _items.values();
    }

    public ISdNode get(String name){
        if(_items.containsKey(name))
            return _items.get(name);
        else
            return Util.createNode(source).buildForNode(null);
    }

    protected void add(String name, ISdNode node){
        _items.put(name,node);
    }
}
