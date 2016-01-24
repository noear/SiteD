package org.noear.sited;

import android.text.TextUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by yuety on 15/8/2.
 */
public class SdNode implements ISdNode {

    public int nodeType(){return 1;}

    //info
    public String name; //节点名称
    public String title;//标题
    public String url; //url
    public String logo; //logo
    public String expr;
    public String group;

    protected String lib;

    private int _dtype;
    public int dtype(){
        if(_dtype==0)
            return source.dtype;
        else
            return _dtype;
    }

    //http
    public String header;   //http header 头需求: cookies|accept
    protected String method;//http method
    //protected String accept;//http accept

    private String _encode;   //http 编码
    private String _ua;     //http ua
    private String _run;

    public String jsTag="";//传递给js函数的扩展参数
    public boolean showWeb=true;

    //cache
    protected int cache=1;//单位为秒(0不缓存；1不限时间)

    //parse
    protected String parse; //解析函数
    protected String parseUrl; //解析出真正在请求的Url

    //build
    protected String buildArgs;
    public String buildUrl;
    protected String buildRef;//
    protected String buildWeb;

    //add prop for search or tag
    protected String addCookie; //需要添加的关键字
    protected String addKey; //需要添加的关键字
    protected int    addPage;//需要添加的页数值

    protected String trySuffix;//".jpg|.png" 尝试不同的扩展名
    public String screen;
    protected Map<String,String> attrs = new HashMap<>();


    //ext prop (for post)
    public String args;

    //宽高比例
    public float WHp = 0;

    public final SdSource source;

    private boolean _isEmpty;
    public boolean isEmpty(){
        return _isEmpty;
    }

    public boolean isWebrun(){
        if(_run==null)
            return false;

        return _run.indexOf("web")>=0;
    }

    public boolean isOutWebrun(){
        if(_run==null)
            return false;

        return _run.indexOf("outweb")>=0;
    }

    //是否有宏定义@key,@page
    public boolean hasMacroDefine(){
        if(url== null || url.indexOf('@')<0)
            return false;
        else
            return true;
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

    //下属函数
    private Map<String,String> _funs;
    public String funs(String key)
    {
        if(_funs==null)
            return null;

        if(_funs.containsKey(key))
            return _funs.get(key);
        else
            return null;
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

    public  String[] getSuffixUrl(String url) {
        if (TextUtils.isEmpty(trySuffix) || TextUtils.isEmpty(url))
            return new String[]{url};
        else {
            String[] exts = trySuffix.split("\\|");
            String[] urls = new String[exts.length];
            for (int i=0,len=exts.length; i<len; i++) {
                urls[i] = url.replaceAll(trySuffix, exts[i]);
            }
            return urls;
        }
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

    public SdNode(){
        this.source = null;
    }

    private SdNode(SdSource source){
        this.source = source;
    }

    protected SdNode(SdSource source, Element cfg) {
        this.source = source;
        _isEmpty = (cfg == null);

        if (cfg != null) {

            this.name = cfg.getTagName();//默认为标签名

            NamedNodeMap nnMap = cfg.getAttributes();
            for(int i=0,len=nnMap.getLength(); i<len; i++) {
                Node att = nnMap.item(i);
                attrs.put(att.getNodeName(), att.getNodeValue());
            }

            showWeb = ("0".equals(cfg.getAttribute("showWeb")))==false;

            this.title   = cfg.getAttribute("title");
            this.method  = cfg.getAttribute("method");
            this.parse   = cfg.getAttribute("parse");
            this.parseUrl= cfg.getAttribute("parseUrl");
            this.url     = cfg.getAttribute("url");
            this.lib     = cfg.getAttribute("lib");
            this.expr    = cfg.getAttribute("expr");
            this._run    = cfg.getAttribute("run");
            this._encode = cfg.getAttribute("encode");
            this._ua     = cfg.getAttribute("ua");
            this.screen  = cfg.getAttribute("screen");

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
            //this.accept = cfg.getAttribute("accept");
            this.header = cfg.getAttribute("header");
            if(this.header == null)
                this.header="";

            String w = cfg.getAttribute("w");
            if(TextUtils.isEmpty(w)==false){
                String h = cfg.getAttribute("h");
                WHp = Float.parseFloat(w)/ Float.parseFloat(h);
            }

            this.buildArgs = cfg.getAttribute("buildArgs");
            this.buildRef  = cfg.getAttribute("buildRef");
            this.buildUrl  = cfg.getAttribute("buildUrl");
            this.buildWeb  = cfg.getAttribute("buildWeb");

            this.args    = cfg.getAttribute("args");

            this.trySuffix  = cfg.getAttribute("trySuffix");

            this.addCookie  = cfg.getAttribute("addCookie");
            this.addKey     = cfg.getAttribute("addKey");
            String _addPage = cfg.getAttribute("addPage");
            if(TextUtils.isEmpty(_addPage)==false){
                this.addPage = Integer.parseInt(_addPage);
            }

            String _dType = cfg.getAttribute("dtype");
            if(TextUtils.isEmpty(_dType)==false){
                this._dtype = Integer.parseInt(_dType);
            }



            if (cfg.hasChildNodes()) {
                _items = new ArrayList<SdNode>();
                _adds  = new ArrayList<SdNode>();
                _funs  = new HashMap<>();
                NodeList list = cfg.getChildNodes();
                for (int i=0,len=list.getLength(); i<len; i++){
                    Node n1 = list.item(i);
                    if(n1.getNodeType()== Node.ELEMENT_NODE) {
                        Element e1 = (Element)n1;

                        if(e1.getTagName().equals("item")) {
                            SdNode temp = new SdNode(source).buildForItem(e1, this);
                            _items.add(temp);
                        }
                        else if(e1.hasAttributes()){
                            SdNode temp = new SdNode(source).buildForAdd(e1, this);
                            _adds.add(temp);
                        }else{
                            _funs.put(e1.getTagName(),e1.getTextContent());
                        }
                    }
                }
            }
        }
    }



    //item(继承父节点)
    private SdNode buildForItem(Element cfg, SdNode p) {
        this.name    = p.name;

        this.title   = cfg.getAttribute("title");//可能为null
        this.url     = cfg.getAttribute("url");//
        this.lib     = cfg.getAttribute("lib");
        this.logo    = cfg.getAttribute("logo");
        this.group   = cfg.getAttribute("group");
        this._encode = cfg.getAttribute("encode");

        String _dType = cfg.getAttribute("dtype");
        if(TextUtils.isEmpty(_dType)==false){
            this._dtype = Integer.parseInt(_dType);
        }

        return this;
    }

    //add (不继承父节点)
    private SdNode buildForAdd(Element cfg, SdNode p) { //add不能有自己独立的url //定义为同一个page的数据获取(可能需要多个ajax)
        this.name = cfg.getTagName();//默认为标签名

        this.method  = cfg.getAttribute("method");
        //this.accept  = cfg.getAttribute("accept");
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
