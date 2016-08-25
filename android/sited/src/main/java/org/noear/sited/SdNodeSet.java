package org.noear.sited;

import android.util.Log;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuety on 15/8/21.
 */
public class SdNodeSet implements ISdNode{

    List<ISdNode> _items = new ArrayList<>();

    public final SdSource source;

    //---------------

    public SdNodeSet(SdSource s){
        source = s;
    }

    public void OnDidInit(){

    }

    private int _dtype=0;
    public  int dtype() {
        if (_dtype > 0)
            return _dtype;
        else
            return 1;
    }//数据类型

    public int nodeType(){return 2;}
    public String nodeName(){return name;}
    @Override
    public boolean  isEmpty(){
        return _items.size()==0;
    }

    public String name;
    public final SdAttributeList attrs = new SdAttributeList();


    protected SdNodeSet buildForNode(Element element) {
        if(element==null)
            return this;

        name = element.getTagName();

        _items.clear();
        attrs.clear();

        {
            NamedNodeMap temp = element.getAttributes();
            for (int i = 0, len = temp.getLength(); i < len; i++) {
                Node p = temp.item(i);
                attrs.set(p.getNodeName(), p.getNodeValue());
            }
        }

        _dtype  = attrs.getInt("dtype");


        NodeList xList = element.getChildNodes();

        for (int i = 0, len = xList.getLength(); i < len; i++) {
            Node n1 = xList.item(i);
            if (n1.getNodeType() == Node.ELEMENT_NODE) {
                Element e1 = (Element) n1;

                if (e1.hasAttributes()) {//说明是Node类型
                    SdNode temp = Util.createNode(source).buildForNode(e1);
                    this.add(temp);
                } else {//说明是Set类型
                    SdNodeSet temp = Util.createNodeSet(source).buildForNode(e1);
                    this.add(temp);
                }
            }
        }

        OnDidInit();
        return this;
    }

    public Iterable<ISdNode> nodes(){
        return _items;
    }

    public ISdNode get(String name){
        for(ISdNode n : _items){
            if(name.equals(n.nodeName()))
                return n;
        }

        return Util.createNode(source).buildForNode(null);
    }

    public SdNode nodeMatch(String url){
        for(ISdNode n : _items){
            SdNode n1 = (SdNode)n;
            if(n1.isMatch(url)) {
                Log.v("nodeMatch.select",n1.expr);
                return n1;
            }
        }

        return Util.createNode(source).buildForNode(null);
    }

    protected void add(ISdNode node){
        _items.add(node);
    }
}
