//
//  SdAttributeList.swift
//  sited
//
//  Created by 谢月甲 on 16/2/4.
//  Copyright © 2016年 noear. All rights reserved.
//

import Foundation

public class SdAttributeList
{
    var _items = [String:String]();
    
    init(){
        
    }
    
    public func clear()
    {
        _items.removeAll();
    }
    
    public func contains(key:String)->Bool
    {
        return _items.contains({ (k,v) -> Bool in
            return key==k;
        });
    }
    
    public func set(key:String,val:String){
        _items[key] = val;
    }
    
    public func getString(key:String)->String?{
        return getString(key,def:nil);
    }
    
    public func getString(key:String!,def:String?)->String?{
        if(contains(key)){
            return _items[key];
        }else{
            return def;
        }
    }
    
    public func getInt(key:String)->Int32!{
        return getInt(key,def:0);
    }
    
    public func getInt(key:String, def:Int32)->Int32!{
        if(contains(key)){
            return Int32(_items[key]!);
        }else{
            return def;
        }
    }
    
    public func getLong(key:String)->Int64!{
        return getLong(key,def:0);
    }
    
    public func getLong(key:String, def:Int64)->Int64!{
        if(contains(key)){
            return Int64(_items[key]!);
        }else{
            return def;
        }
    }
}