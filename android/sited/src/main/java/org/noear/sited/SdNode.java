package org.noear.sited;

import android.text.TextUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;


/**
 * Created by yuety on 15/8/2.
 */
public class SdNode implements ISdNode{

    public int nodeType(){return 1;}

    //info
    public String name; //节点名称
    public String title;//标题
    public String url; //url
    public String logo; //logo
    public String expr;
    public String group;

    //http
    public String header;   //http header 头需求: cookies|accept
    protected String method;//http method
    protected String accept;//http accept

    private String _encode;   //http 编码
    private String _ua;     //http ua
    private String _run;


    //cache
    protected int cache=1;//单位为秒(0不缓存；1不限时间)

    //parse
    protected String parse; //解析函数
    protected String parseUrl; //解析出真正在请求的Url

    //build
    protected String buildUrl;
    protected String buildKey;
    protected String buildRef;//
    protected String buildWeb;

    //add prop for search or tag
    protected String addKey; //需要添加的关键字
    protected int    addPage;//需要添加的页数值
    public String args;

    //ext prop (for post)
//    public String pop1;
//    public String pop2;

    //宽高比例
    public float WHp = 0;

    public final SdSource source;

    private boolean _isEmpty;
    public boolean isEmpty(){
        return _isEmpty;
    }

    public boolean isWebrun(){
        return "web".equals(_run);
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
            return source.encode;
        else
            return _encode;
    }


    //获取referer url
    public String referer(String url) {
        return source.buildReferer(this, url);
    }

    //获取cookies
    public String cookies() {
        return source.cookies();
    }


    protected SdNode(SdSource source,Element cfg) {
        this.source = source;
        _isEmpty = (cfg == null);

        if (cfg != null) {

            this.name = cfg.getTagName();//默认为标签名

            this.title   = cfg.getAttribute("title");
            this.method  = cfg.getAttribute("method");
            this.parse   = cfg.getAttribute("parse");
            this.parseUrl= cfg.getAttribute("parseUrl");
            this.url     = cfg.getAttribute("url");
            this.expr    = cfg.getAttribute("expr");
            this._run    = cfg.getAttribute("run");
            this._encode = cfg.getAttribute("encode");
            this._ua     = cfg.getAttribute("ua");

            if(TextUtils.isEmpty(this.method)){
                this.method = "get";
            }

            {
                String temp = cfg.getAttribute("cache");
                if(TextUtils.isEmpty(temp)==false){
                    int len = temp.length();
                    if(len==1){
                        cache = Integer.parseInt(temp);
                    }else if(len>1){
                        cache = Integer.parseInt(temp.substring(0,len-1));

                        String p = temp.substring(len-1);
                        switch (p){
                            case "d":cache=cache*24*60*60;break;
                            case "h":cache=cache*60*60;break;
                            case "m":cache=cache*60;break;
                        }
                    }
                }
            }


            //book,section 特有
            this.accept = cfg.getAttribute("accept");
            this.header = cfg.getAttribute("header");
            if(this.header == null)
                this.header="";

            String w = cfg.getAttribute("w");
            if(TextUtils.isEmpty(w)==false){
                String h = cfg.getAttribute("h");
                WHp = Float.parseFloat(w)/Float.parseFloat(h);
            }

            this.buildRef = cfg.getAttribute("buildRef");
            this.buildUrl = cfg.getAttribute("buildUrl");
            this.buildKey = cfg.getAttribute("buildKey");
            this.buildWeb = cfg.getAttribute("buildWeb");

            this.args    = cfg.getAttribute("args");
            this.addKey  = cfg.getAttribute("addKey");

            String _addPage = cfg.getAttribute("addPage");
            if(TextUtils.isEmpty(_addPage)==false){
                this.addPage = Integer.parseInt(_addPage);
            }


            //搜索物有
            //this.pop1 = cfg.getAttribute("pop1");//
            //this.pop2 = cfg.getAttribute("pop2");//

            if (cfg.hasChildNodes()) {
                _items = new ArrayList<SdNode>();
                _adds  = new ArrayList<SdNode>();
                NodeList list = cfg.getChildNodes();
                for (int i=0,len=list.getLength(); i<len; i++){
                    Node n1 = list.item(i);
                    if(n1.getNodeType()==Node.ELEMENT_NODE) {
                        Element e1 = (Element)n1;

                        if(e1.getTagName().equals("item")) {
                            SdNode temp = new SdNode(source).buildForItem(e1, this);
                            _items.add(temp);
                        }
                        else{
                            SdNode temp = new SdNode(source).buildForAdd(e1, this);
                            _adds.add(temp);
                        }
                    }
                }
            }
        }
    }

    private SdNode(SdSource source){
        this.source = source;
    }

    public SdNode(){
        this.source = null;
    }

    //item(继承父节点)
    private SdNode buildForItem(Element cfg, SdNode p) {
        this.name    = p.name;

        this.title   = cfg.getAttribute("title");//可能为null
        this.url     = cfg.getAttribute("url");//
        this.logo    = cfg.getAttribute("logo");
        this.group   = cfg.getAttribute("group");
        this._encode = cfg.getAttribute("encode");

        return this;
    }

    //add (不继承父节点)
    private SdNode buildForAdd(Element cfg, SdNode p) { //add不能有自己独立的url //定义为同一个page的数据获取(可能需要多个ajax)
        this.name = cfg.getTagName();//默认为标签名

        this.method  = cfg.getAttribute("method");
        this.accept  = cfg.getAttribute("accept");
        this._encode = cfg.getAttribute("encode");
        this._ua     = cfg.getAttribute("ua");

        //--------
        this.title    = cfg.getAttribute("title");//可能为null
        this.parse    = cfg.getAttribute("parse");
        this.buildUrl = cfg.getAttribute("buildUrl");
        this.buildRef = cfg.getAttribute("buildRef");

        return this;
    }
}
