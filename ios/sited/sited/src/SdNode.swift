//
//  SdNode.swift
//  ddcat
//
//  Created by 谢月甲 on 15/10/8.
//  Copyright © 2015年 noear. All rights reserved.
//

import Foundation
import SWXMLHash

public class SdNode : ISdNode{
    public init(source:SdSource?){
        self.source = source;
    }
    
    public func OnDidInit(){
    
    }
    
    public func nodeType()->Int{return 1;}
    public let attrs = SdAttributeList();
    
    //info
    public var name:String?; //节点名称
    public var title:String?;//标题
    public var url:String?; //url
    public var logo:String?; //logo
    public var expr:String?; //expr
    public var group:String?; //group
    
    public var lib:String?; //lib
    
    //http
    public var header:String?;   //http header 头需求: cookies|accept
    public var method:String?;//http method
    
    private var _encode:String?;   //http 编码
    private var _ua:String?;     //http ua
    
    public var jsTag="";
    
    var cache:Int32=1;//cache//单位为秒(0不缓存；1不限时间)
    
    //parse
    var parse:String?; //解析函数
    var parseUrl:String?; //解析函数
    
    //build
    var buildArgs:String?;
    public var buildUrl:String?;
    var buildRef:String?;//

    //add prop for search or tag
    var addCookie:String?; //需要添加的Cookie
    var addKey:String?; //需要添加的关键字
    var addPage:Int32!=0;//需要添加的页数增减量
    
    //源
    public var args:String?;
    //源
    public var source:SdSource!;
    
    
    private var _isEmpty=false;
    public func isEmpty()->Bool{ return _isEmpty; }
    
    private var _items:[SdNode]?;
    public func items()->[SdNode]?{ return _items; }
    
    private var _adds:[SdNode]?;
    public func adds()->[SdNode]?{ return _adds; }
    
    
    public func hasMacro()->Bool{
        if(url == nil || url?.indexOf("@")<0){
            return false;
        }else{
            return true;
        }
    }
    
    public func isMatch(url:String)->Bool{
        if(Util.isEmpty(expr)==false){
            return Regex(expr!).test(url);
        }else{
            return false;
        }
    }
    
    public func isEquals(node:SdNode)->Bool{
        if name == nil {
            return false;
        }
        
        return name == node.name;
    }
    
    func isInCookie()->Bool{
        if(Util.isEmpty(header)){
            return false;
        }
        else{
            return header!.indexOf("cookie")>=0;
        }
    }
    
    func isInReferer()->Bool{
        if(Util.isEmpty(header)){
            return false;
        }
        else{
            return header!.indexOf("referer")>=0;
        }
    }
    
    func hasItems()->Bool{
        return _items?.count>0;
    }
    
    func hasAdds()->Bool{
        return _adds?.count>0;
    }
    
    func ua()->String?{
        if(Util.isEmpty(_ua)){
            return source.ua();
        }else{
            return _ua;
        }
    }
    
    func encode()->String?{
        if(Util.isEmpty(_encode)){
            return source.encode();
        }else{
            return _encode!;
        }
    }
    
    func referer(uri:String)->String{
        return source.buildReferer(self, url: uri)!;
    }
    
    func cookies(uri:String)->String?{
        var cookies = source.cookies();
        
        if (Util.isEmpty(addCookie) == false) {
            if (Util.isEmpty(cookies)) {
                cookies = addCookie! + "; Path=/; Domain=" + (NSURL(string: uri)?.host)!;
            } else {
                cookies = addCookie! + "; " + cookies!;
            }
        }
        
        return cookies;
    }
    
    func buildForNode(cfg:XMLIndexer?) -> SdNode{
        _isEmpty = (cfg == nil || cfg!.element == nil);
        
        if(_isEmpty == false){
            self.name = cfg!.element!.name;
            
            for (k,v)in cfg!.element!.attributes{
                attrs.set(k, val: v);
            }
            
            self.title    = attrs.getString("title");
            self.method   = attrs.getString("method",def:"get");
            self.parse    = attrs.getString("parse");
            self.parseUrl = attrs.getString("parseUrl");
            self.url      = attrs.getString("url");
            self.lib      = attrs.getString("lib");
            self.expr     = attrs.getString("expr");
            
            self._encode  = attrs.getString("encode");
            self._ua      = attrs.getString("ua");
            
            //book,section 特有
            self.header = attrs.getString("header",def:"");
            
            self.buildArgs = attrs.getString("buildArgs");
            self.buildRef  = attrs.getString("buildRef");
            self.buildUrl  = attrs.getString("buildUrl");
            
            self.args    = attrs.getString("args");
            
            self.addCookie  = attrs.getString("addCookie");
            self.addKey     = attrs.getString("addKey");
            self.addPage    = attrs.getInt("addPage");
            
            
            let temp = attrs.getString("cache");
            if(Util.isEmpty(temp)==false){
                let len = temp!.length();
                if(len==1){
                    cache = Int32(temp!)!;
                }else if(len>1){
                    cache = Int32(temp!.subString(0, end: len-1))!;
                    
                    let p = temp!.subString(len-1);
                    switch (p){
                        case "d":cache=cache*24*60*60;break;
                        case "h":cache=cache*60*60;break;
                        case "m":cache=cache*60;break;
                        default:break;
                    }
                }
            }
            
            
            let childNodes = cfg!.children;
            
            if(childNodes.count>0){
                _items = [SdNode]();
                _adds = [SdNode]();
                
                for(var i=0,len=childNodes.count; i<len; i++){
                     let n1 = childNodes[i];
                    let e1 = n1.element!;
                    
                    if(e1.name == "item"){
                        let temp = Util.createNode(source).buildForItem(n1, p: self);
                        _items?.append(temp);
                    }else if(e1.attributes.count>0){
                        let temp = Util.createNode(source).buildForAddin(n1, p:self);
                        _items?.append(temp);
                    }else{
                        attrs.set(e1.name, val: e1.text!);
                    }
                }
            }
            
        }
        
        OnDidInit();
        
        return self;
    }
    
    //item(不继承父节点)
    func buildForItem(cfg:XMLIndexer, p:SdNode)->SdNode
    {
        for (k,v)in cfg.element!.attributes{
            attrs.set(k, val: v);
        }
        
        self.name    = p.name;
        
        self.title   = attrs.getString("title");//可能为null
        self.group   = attrs.getString("group");
        self.url     = attrs.getString("url");//
        self.lib     = attrs.getString("lib");
        self.logo    = attrs.getString("logo");
        self._encode = attrs.getString("encode");
        
        return self;
    }
    
    func buildForAddin(cfg:XMLIndexer, p:SdNode)->SdNode{
        for (k,v)in cfg.element!.attributes{
            attrs.set(k, val: v);
        }
        
        self.name = cfg.element?.name;//默认为标签名
        
        self.method  = attrs.getString("method");
        self._encode = attrs.getString("encode");
        self._ua     = attrs.getString("ua");
        
        //--------
        self.title    = attrs.getString("title");//可能为null
        self.parse    = attrs.getString("parse");
        self.buildUrl = attrs.getString("buildUrl");
        self.buildRef = attrs.getString("buildRef");
        
        return self;
    }
}




