//
//  SdSource.swift
//  ddcat
//
//  Created by 谢月甲 on 15/10/8.
//  Copyright © 2015年 noear. All rights reserved.
//

import Foundation
import SWXMLHash

public class SdSource {
    public var attrs = SdAttributeList();
    
    public var isDebug:Bool=false;//是否为调试模式
    
    public final var url_md5:String?; //更新接口
    public final var url:String?; //更新接口
    public final var title:String?; //源标题
    public final var expr:String?;//匹配源的表达式

       

    private var _encode:String?;
    public func encode()->String?{return _encode;}
    
    private var _ua:String?;
    public func ua()->String?{ if(Util.isEmpty(_ua)){ return Util.defUA; }else{ return _ua; } }
    
    private var _cookies:String?;
    public func cookies()->String?{return _cookies;}
    public func setCookies(cookies:String){_cookies = cookies;}
    
    public func delCache(key:String){}
    //-------------------------------
    public final var body:SdNodeSet!;
    private final var js:JsEngine!;
    var jscript:SdJscript?;
    
    public init(xml:String,_ xmlBodyNodeName:String)
    {

        Util.tryInitCache();
        
        
        let root = SWXMLHash.parse(xml);
        
        
        for (k,v)in root.element!.attributes{
            attrs.set(k, val: v);
        }
        
        let childNodes = root.children;
        for(var i=0,len=childNodes.count; i<len; i++){
            let n1 = childNodes[i];
            let e1 = n1.element!;
            
            if(e1.attributes.count==0 && n1.children.count==0){
                attrs.set(e1.name, val: e1.text!);
            }
        }

        
        isDebug = attrs.getInt("debug") > 0;
        
        title   = attrs.getString("title");
        expr    = attrs.getString("expr");
        url     = attrs.getString("url");
        url_md5 = Util.md5(url!);
        
        _encode = attrs.getString("encode");
        _ua     = attrs.getString("ua");
        
        body = Util.createNodeSet(self);
        body.buildForNode(root[xmlBodyNodeName]);
     
        js = JsEngine(source: self);
        jscript =  SdJscript(source: self, node: root["jscript"]);
        jscript!.loadJs(js!);
        
        OnDidInit();
    }
    
    public func OnDidInit(){
        
    }
    
    public func isMatch(url:String)->Bool{
        return Regex(expr!).test(url);
    }
    
    public func callJs(config:SdNode, funAttr:String, args:[AnyObject]!)->String{
        return js!.callJs(config.attrs.getString(funAttr)!, args: args);
    }
    
    //-------------
    
    public func buildArgs(config:SdNode, url:String, key:String, page:Int32)->String? {
        if (Util.isEmpty(config.buildArgs)){
            return config.args;
        }
        else{
            return js!.callJs(config.buildArgs!, args: [url, key, NSNumber(int: page), config.jsTag]);
        }
    }
    
    public func buildArgs(config:SdNode, url:String, page:Int32)->String? {
        if (Util.isEmpty(config.buildArgs)){
            return config.args;
        }
        else{
            return js!.callJs(config.buildArgs!, args: [url, NSNumber(int: page), config.jsTag]);
        }
    }
    
    public func buildUrl(config:SdNode, url:String)->String? {
        if (Util.isEmpty(config.buildUrl)){
            return url;
        }
        else{
            return js!.callJs(config.buildUrl!, args: [url, config.jsTag]);
        }
    }
    
    public func buildUrl(config:SdNode, url:String, page:Int32)->String? {
        if (Util.isEmpty(config.buildUrl)){
            return url;
        }
        else{
            return js!.callJs(config.buildUrl!, args: [url, NSNumber(int: page), config.jsTag]);
        }
    }
    
    public func buildUrl(config:SdNode, url:String,key:String, page:Int32)->String? {
        if (Util.isEmpty(config.buildUrl)){
            return url;
        }
        else{
            return js!.callJs(config.buildUrl!, args: [url, key, NSNumber(int: page), config.jsTag]);
        }
    }
    
    public func buildReferer(config:SdNode, url:String)->String? {
        if (Util.isEmpty(config.buildRef)){
            return url;
        }
        else{
            return js!.callJs(config.buildRef!, args: [url, config.jsTag]);
        }
    }
    
    public func parse(config:SdNode, url:String, html:String)->String? {
        if ("@null" == config.parse){
            return html;
        }
        else{
            return js!.callJs(config.parse!, args: [url, html, config.jsTag]);
        }
    }
    
    public func parseUrl(config:SdNode, url:String, html:String)->String {
        if (Util.isEmpty(config.parseUrl)){
            return url;
        }
        else{
            return js!.callJs(config.parseUrl!, args: [url, html, config.jsTag]);
        }
    }
    
    //
    //---------------------------------------
    //
    //
    //---------------------------------------
    //
    public func getNodeViewModel(viewModel:ISdViewModel, nodeSet:SdNodeSet, isUpdate:Bool, callback:SdSourceCallback)
    {
        let tag = AsyncTag();
        
        for node in nodeSet.nodes()
        {
            let n = node as! SdNode;
            doGetNodeViewModel(viewModel, isUpdate:isUpdate, tag:tag, url:n.url!, args: nil, config:n, callback:callback);
        }
        
        if (tag.total == 0) {
            callback(code: 1);
        }
    }
    
    public func getNodeViewModel(viewModel:ISdViewModel , isUpdate:Bool, var key:String? , var page:Int32,  config:SdNode,  callback:SdSourceCallback)
    {
        
        page += config.addPage; //加上增减量
        
        if (key != nil && Util.isEmpty(config.addKey) == false) {//如果有补充关键字
            key = key! + " " + config.addKey!;
        }
        
        var newUrl:String?;
        if(key == nil){
            newUrl = buildUrl(config, url: config.url!, page: page);
        }
        else{
            newUrl = buildUrl(config, url: config.url!, key: key!, page: page);
        }
    
    
    
        var args:[String:String]?;
        
        if ("post"==(config.method)) {
            args = [String:String]();
            
            var _strArgs:String?;
            if (key == nil){
                _strArgs = buildArgs(config, url:url!, page:page);
            }
            else{
                _strArgs = buildArgs(config, url:url!, key:key!, page:page);
            }
    
            if (Util.isEmpty(_strArgs) == false) {
                for kv in _strArgs!.split(";") {
                    if (kv.length() > 3) {
                        let name = kv.split("=")[0];
                        let value = kv.split("=")[1];
                        
                        if (value == ("@key")){
                            args![name] =  key;
                        }
                        else if (value == ("@page")){
                            args![name] = String(page);
                        }
                        else{
                            args![name] = value;
                        }
                    }
                }
            }
            
        } else {
            newUrl = newUrl!.replace("@page", newVal: String(page));
            if (key != nil){ newUrl = newUrl!.replace("@key", newVal: Util.urlEncode(key!, config:config));}
        }
    
        let  newUrl0 = newUrl;
        let  args0 = args;
    
        Util.http(self, isUpdate:isUpdate, url:newUrl!, args:args0, tag:0, config:config, callback:{ (code, t, text)->() in
            if (code == 1)
            {
                
                if (Util.isEmpty(config.parseUrl) == false)
                {
                    //url需要解析出来(多个用;隔开)
                    let newUrls = self.parseUrl(config, url:newUrl0!, html:text!).split(";");
                    let dataList = [Int32:String]();//如果有多个地址，需要排序
                    let tag = AsyncTag();
                    
                    for newUrl2 in newUrls
                    {
                        tag.total++;
                        Util.http(self, isUpdate:isUpdate, url:newUrl2, args:args0, tag:tag.total, config:config, callback: {(code2, t2, text2)->() in
                            if (code2 == 1) {
                                self.doParse_noAddinForTmp(dataList, config:config, url:newUrl2, text:text2!, tag:t2);
                            }
                            
                            tag.value++;
                            if (tag.total == tag.value)
                            {
                                var jsonList = [String]();
                                for (var i:Int32 = 1; i <= tag.total; i++) { //安排序加载内容
                                    if (dataList.containsKey(i)) {
                                        jsonList.append(dataList[i]!);
                                    }
                                }
                                
                                viewModel.loadByJson(config, jsons:jsonList);
                                
                                callback(code: code);
                            }
                        });
                    }
                    return;//下面的代码被停掉
                }
            
                self.doParse_noAddin(viewModel, config:config, url:newUrl0!, text:text!);
                
            }
            
            callback(code:code);
        });
    }
    
    
    public func getNodeViewModel(viewModel:ISdViewModel, isUpdate:Bool, page:Int32, config:SdNode, callback:SdSourceCallback) {
        getNodeViewModel(viewModel, isUpdate:isUpdate, key:nil, page:page, config:config, callback:callback);
    }
    
    public func getNodeViewModel(viewModel:ISdViewModel ,  isUpdate:Bool,  url:String , config:SdNode, callback:SdSourceCallback) {
        //需要对url进行转换成最新的格式（可能之前的旧的格式缓存）
        
        let tag = AsyncTag();
        
        doGetNodeViewModel(viewModel, isUpdate:isUpdate, tag:tag, url:url, args:nil, config:config, callback:callback);
        
        if (tag.total == 0) {
            callback(code: 1);
        }
    }
    
    private func doGetNodeViewModel(viewModel:ISdViewModel,  isUpdate:Bool, tag: AsyncTag,  url:String , args:[String:String]?,  config:SdNode,  callback:SdSourceCallback) {
        //需要对url进行转换成最新的格式（可能之前的旧的格式缓存）
        
        if (config.isEmpty()){
            return;
        }
        
        if (config.hasItems()) {
            viewModel.loadByConfig(config);
            return;
        }
        
        if (Util.isEmpty(config.parse)){ //没有解析的不处理
            return;
        }
        
        //------------
        if (Util.isEmpty(url)){ //url为空的不处理
            return;
        }
        
        if(1==1){
            //2.获取主内容
            tag.total++;
            let newUrl = self.buildUrl(config, url:url);
            
            //有缓存的话，可能会变成同步了
            Util.http(self, isUpdate:isUpdate, url:newUrl!, args:args, tag:0, config:config, callback: {(code, t, text)->() in
                if (code == 1) {
                    
                    if (Util.isEmpty(config.parseUrl) == false) { //url需要解析出来(多个用;隔开)
                        let newUrls = self.parseUrl(config, url:newUrl!, html:text!).split(";");
                        var dataList = [Int32:String]();//如果有多个地址，需要排序
                        
                        tag.total--;//抵消之前的++
                        for  newUrl2 in newUrls {
                            tag.total++;
                            Util.http(self, isUpdate:isUpdate, url:newUrl2, args:args, tag:tag.total, config:config, callback:{(code2, t2, text2)->() in
                                if (code2 == 1) {
                                    self.doParse_noAddinForTmp(dataList, config:config, url:newUrl2, text:text2!,tag:t2);
                                }
                                
                                tag.value++;
                                if (tag.total == tag.value) {
                                    var jsonList = [String]();
                                    for (var i:Int32 = 1; i <= tag.total; i++) { //安排序加载内容
                                        if (dataList.containsKey(i)) {
                                            jsonList.append(dataList[i]!);
                                        }
                                    }
                                    
                                    viewModel.loadByJson(config, jsons: jsonList);
                                    callback(code:code);
                                }
                            });
                        }
                        return;//下面的代码被停掉
                    }
                    
                    self.doParse_hasAddin(viewModel, config:config, url:newUrl!, text:text!);
                }
                
                tag.value++;
                if (tag.total == tag.value) {
                    callback(code:code);
                }
            });
        }
        
        if (config.hasAdds()) {
            //2.2 获取副内容（可能有多个）
            for  n1 in config.adds()! {
                if (Util.isEmpty(n1.buildUrl)){
                    continue;
                }
                
                tag.total++;
                let newUrl = buildUrl(n1, url:url); //有url的add //add 不能有自己的独立url
                
                Util.http(self, isUpdate:isUpdate, url:newUrl!, args:args, tag:0, config:n1, callback: {(code, t, text) ->() in
                    if (code == 1) {
                        let json = self.parse(n1, url:newUrl!, html:text!);
                        if (self.isDebug) {
                            Util.log(self, node:n1, url:newUrl, json:json);
                        }
                        
                        if (json != nil) {
                            viewModel.loadByJson(n1, jsons: [json!]);
                        }
                    }
                    
                    tag.value++;
                    if (tag.total == tag.value) {
                        callback(code:code);
                    }
                });
            }
        }
    }
    
    private func doParse_noAddin(viewModel:ISdViewModel ,config:SdNode ,url:String ,text:String ) {
        let json = self.parse(config, url:url, html:text);
        
        if (self.isDebug) {
            Util.log(self, node:config, url:url, json:json);
        }
        
        if (json != nil) {
            viewModel.loadByJson(config, jsons:[json!]);
        }
    }
    
    private func doParse_hasAddin(viewModel:ISdViewModel , config:SdNode , url:String , text:String ) {
        let json = self.parse(config, url:url, html:text);
        
        if (self.isDebug) {
            Util.log(self, node:config, url:url, json:json);
        }
        
        if (json != nil) {
            viewModel.loadByJson(config, jsons:[json!]);
            
            if (config.hasAdds()) { //没有url的add
                for  n2 in config.adds()! {
                    if (Util.isEmpty(n2.buildUrl) == false){
                        continue;
                    }
                    
                    let json2 = self.parse(n2, url:url, html:text);
                    if (self.isDebug) {
                        Util.log(self, node:n2, url:url, json:json2);
                    }
                    
                    if (json2 != nil) {
                        viewModel.loadByJson(n2, jsons: [json2!]);
                    }
                }
            }
        }
    }
    
    private func doParse_noAddinForTmp(var dataList:[Int32:String], config:SdNode, url:String , text:String , tag:Int32){
        let json = self.parse(config, url:url, html:text);
        
        if(isDebug) {
            Util.log(self,node:config, url:url, json:json);
        }
        
        if (json != nil) {
            dataList[tag] = json!;
        }
    }
}