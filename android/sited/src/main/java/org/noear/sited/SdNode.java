package org.noear.sited;

import android.text.TextUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by yuety on 15/8/2.
 */
public class SdNode implements ISdNode{

    public SdNode(SdSource source){
        this.source = source;
    }

    protected void OnDidInit(){

    }


    public int nodeType(){return 1;}
    public final SdAttributeList attrs = new SdAttributeList();

    //info
    public String name; //节点名称
    public String title;//标题
    public String url; //url
    public String logo; //logo
    public String expr;
    public String group;

    protected String lib;


    //http
    public String header;   //http header 头需求: cookies|accept
    protected String method;//http method

    private String _encode;   //http 编码
    private String _ua;     //http ua

    public String jsTag="";//传递给js函数的扩展参数

    //cache
    protected int cache=1;//单位为秒(0不缓存；1不限时间)

    //parse
    protected String parse; //解析函数
    protected String parseUrl; //解析出真正在请求的Url

    //build
    protected String buildArgs;
    protected String buildUrl;
    protected String buildRef;//

    //add prop for search or tag
    protected String addCookie; //需要添加的关键字
    protected String addKey; //需要添加的关键字
    protected int    addPage;//需要添加的页数值


    //ext prop (for post)
    public String args;

    public final SdSource source;

    private boolean _isEmpty;
    public boolean isEmpty(){
        return _isEmpty;
    }


    //下属项目
    private List<SdNode> _items;
    public List<SdNode> items(){
        return _items;
    }

    //下属数据节点
    private List<SdNode> _adds;
    public List<SdNode> adds(){
        return _adds;
    }


    //是否有宏定义@key,@page
    public boolean hasMacro(){
        if(url== null || url.indexOf('@')<0)
            return false;
        else
            return true;
    }

    public boolean isMatch(String url){
        if(TextUtils.isEmpty(expr)==false){
            Pattern pattern = Pattern.compile(expr);
            Matcher m = pattern.matcher(url);

            return m.find();
        }else {
            return false;
        }
    }

    public boolean isEquals(SdNode node)
    {
        if(name==null)
            return false;

        return name.equals(node.name);
    }

    public boolean isInCookie()
    {
        if(header==null)
            return false;
        else
            return header.indexOf("cookie")>=0;
    }

    public boolean isInReferer()
    {
        if(header==null)
            return false;
        else
            return header.indexOf("referer")>=0;
    }

    public boolean hasItems(){
        if(_items == null || _items.size()==0)
            return false;
        else
            return true;
    }

    public boolean hasAdds(){
        if(_adds == null || _adds.size()==0)
            return false;
        else
            return true;
    }

    public String ua(){
        if(TextUtils.isEmpty(_ua))
            return source.ua();
        else
            return _ua;
    }

    public String encode(){
        if(TextUtils.isEmpty(_encode))
            return source.encode();
        else
            return _encode;
    }


    //获取referer url
    public String referer(String uri) {
        return source.buildReferer(this, uri);
    }

    //获取cookies
    public String cookies(String uri) {
        String cookies = source.cookies();

        if (TextUtils.isEmpty(addCookie) == false) {
            if (TextUtils.isEmpty(cookies)) {
                cookies = addCookie + "; Path=/; Domain=" + URI.create(uri).getHost();
            } else {
                cookies = addCookie + "; " + cookies;
            }
        }

        return cookies;
    }

    protected SdNode buildForNode(Element cfg) {
        _isEmpty = (cfg == null);

        if (cfg != null) {

            this.name = cfg.getTagName();//默认为标签名

            NamedNodeMap nnMap = cfg.getAttributes();
            for(int i=0,len=nnMap.getLength(); i<len; i++) {
                Node att = nnMap.item(i);
                attrs.set(att.getNodeName(), att.getNodeValue());
            }

            this.title   = attrs.getString("title");
            this.method  = attrs.getString("method","get");
            this.parse   = attrs.getString("parse");
            this.parseUrl= attrs.getString("parseUrl");
            this.url     = attrs.getString("url");
            this.lib     = attrs.getString("lib");
            this.expr    = attrs.getString("expr");

            this._encode = attrs.getString("encode");
            this._ua     = attrs.getString("ua");

            //book,section 特有
            this.header = attrs.getString("header","");

            this.buildArgs = attrs.getString("buildArgs");
            this.buildRef  = attrs.getString("buildRef");
            this.buildUrl  = attrs.getString("buildUrl");

            this.args    = attrs.getString("args");

            this.addCookie  = attrs.getString("addCookie");
            this.addKey     = attrs.getString("addKey");
            this.addPage    = attrs.getInt("addPage");

            if (cfg.hasChildNodes()) {
                _items = new ArrayList<SdNode>();
                _adds  = new ArrayList<SdNode>();

                NodeList list = cfg.getChildNodes();
                for (int i=0,len=list.getLength(); i<len; i++){
                    Node n1 = list.item(i);
                    if(n1.getNodeType()==Node.ELEMENT_NODE) {
                        Element e1 = (Element)n1;

                        if(e1.getTagName().equals("item")) {
                            SdNode temp = Util.createNode(source).buildForItem(e1, this);
                            _items.add(temp);
                        }
                        else if(e1.hasAttributes()){
                            SdNode temp = Util.createNode(source).buildForAdd(e1, this);
                            _adds.add(temp);
                        }else {
                            attrs.set(e1.getTagName(), e1.getTextContent());
                        }
                    }
                }
            }
        }

        OnDidInit();

        return this;
    }

    //item(不继承父节点)
    private SdNode buildForItem(Element cfg, SdNode p) {
        NamedNodeMap nnMap = cfg.getAttributes();
        for(int i=0,len=nnMap.getLength(); i<len; i++) {
            Node att = nnMap.item(i);
            attrs.set(att.getNodeName(), att.getNodeValue());
        }

        this.name    = p.name;

        this.title   = attrs.getString("title");//可能为null
        this.group   = attrs.getString("group");
        this.url     = attrs.getString("url");//
        this.lib     = attrs.getString("lib");
        this.logo    = attrs.getString("logo");
        this._encode = attrs.getString("encode");

        return this;
    }

    //add (不继承父节点)
    private SdNode buildForAdd(Element cfg, SdNode p) { //add不能有自己独立的url //定义为同一个page的数据获取(可能需要多个ajax)
        NamedNodeMap nnMap = cfg.getAttributes();
        for(int i=0,len=nnMap.getLength(); i<len; i++) {
            Node att = nnMap.item(i);
            attrs.set(att.getNodeName(), att.getNodeValue());
        }

        this.name = cfg.getTagName();//默认为标签名

        this.method  = attrs.getString("method");
        this._encode = attrs.getString("encode");
        this._ua     = attrs.getString("ua");

        //--------
        this.title    = attrs.getString("title");//可能为null
        this.parse    = attrs.getString("parse");
        this.buildUrl = attrs.getString("buildUrl");
        this.buildRef = attrs.getString("buildRef");

        return this;
    }
}
