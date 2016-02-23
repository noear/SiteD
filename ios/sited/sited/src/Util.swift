//
//  Util.swift
//  ddcat
//
//  Created by 谢月甲 on 15/10/8.
//  Copyright © 2015年 noear. All rights reserved.
//

import Foundation
import STHTTPRequest


class Util {
    static let defUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240";
    
    static var cache:ICache?;
    
    static func tryInitCache(){
        if(cache == nil){
            cache = FileCache();
        }
    }
    
    static func isEmpty(str:String?)->Bool{
        return str == nil || str!.isEmpty;
    }
    
//    static func isNullOrEmpty(str:String?)->Bool{
//        return str == nil || str!.isEmpty;
//    }
    
    //------------
    
    static func urlEncode(str:String, config:SdNode)->String{
        return "";
    }
    
    static func md5(str:String)->String{
        return "";
    }
    
    static func http(source:SdSource, isUpdate:Bool, url:String, args:[String:String]?, tag:Int32, config:SdNode, callback:((code:Int32, tag:Int32,text:String?)->())){
        
        var block:CacheBlock? = nil;
        
        var cacheKey2:String?;
        if (args == nil){
            cacheKey2 = url;
        }
        else {
            var sb = String();
            sb.appendContentsOf(url);
            for key in args!.keys {
                sb.appendContentsOf(key);
                sb.appendContentsOf("=");
                sb.appendContentsOf(args![key]!);
                sb.appendContentsOf(";");
            }
            cacheKey2 = sb;
        }
        let cacheKey = cacheKey2;
        
        if(isUpdate==false && config.cache>0){
            block = cache!.get(cacheKey!);
        }
        
        if(block != nil){
            if(config.cache==1 || block?.seconds()<=config.cache){
                callback(code: 1, tag: tag, text: block!.value);
            }
            return;
        }
        
        dohttp(source, url: url, args: args, tag:tag, config: config, callback: { (code, tag2, data) -> () in
            if(code==1 && config.cache>0){
                cache!.save(url,data: data!);
            }
            
            callback(code: code, tag:tag, text: data);
        });
        
    }
    
    static func dohttp(source:SdSource, url:String, args:[String:String]?, tag:Int32,config:SdNode, callback:((code:Int32, tag:Int32, data:String?)->()))
    {
        let client:STHTTPRequest = STHTTPRequest(URLString: url);
        //rst.timeoutSeconds         = 60;
        client.forcedResponseEncoding = NSUTF8StringEncoding;
        client.setHeaderWithName("User-Agent", value: source.ua());
        
        if(config.isInCookie()){
            let cookies = config.cookies(url);
            if(cookies != nil){
                client.setHeaderWithName("Cookie", value: cookies);
            }
        }
        
        if(config.isInReferer()){
             client.setHeaderWithName("Referer", value: source.buildReferer(config, url: url));
        }
        
        if(Util.isEmpty(config.header)==false){
            for kv in config.header!.split(";") {
                let kv2 = kv.split("=");
                if(kv2.count==2){
                    client.setHeaderWithName(kv2[0], value: kv2[1]);
                }
            }
        }
        
        if("post" == config.method){
            client.POSTDataEncoding       = NSUTF8StringEncoding;
            client.encodePOSTDictionary   = false;
            if(args != nil){
                client.POSTDictionary     = args!;
            }
        }
    
        client.completionBlock = {headers,body in
            let cookie = headers["Set-Cookie"];
            if(cookie != nil){
                source.setCookies(cookie! as! String);
            }
            
            return callback(code: 1, tag:tag, data: body);
        };
        
        client.errorBlock = {error in
            return callback(code: -2, tag:tag, data: nil);
        };
        
        client.startAsynchronous();
    }
    
    //
    //--------------------------------
    //
    
    static func log(source:SdSource, node:SdNode, url:String?, json:String?){
        
    }
    
    static func log(source:SdSource, tag:String, msg:String){
        
    }
    
    static func log(source:SdSource, tag:String, msg:String, tr:AnyObject){
        
    }
    
    static func createNode(source:SdSource)->SdNode!
    {
        return (SdApi._factory!.createNode(source));
    }
    
    static func createNodeSet(source:SdSource)->SdNodeSet!
    {
        return SdApi._factory!.createNodeSet(source);
    }
    
    
}