//
//  SdApi.swift
//  sited
//
//  Created by 谢月甲 on 16/2/4.
//  Copyright © 2016年 noear. All rights reserved.
//

import Foundation

public class SdApi
{
    static var _factory : SdNodeFactory?;
    static var _listener : SdLogListener?;
    
    public static func tryInit(factory:SdNodeFactory, listener:SdLogListener){
        if(_factory == nil){
            _factory = factory;
        }
        
        if(_listener == nil){
            _listener = listener;
        }
    }
    
    public static func check()
    {
        if(_factory == nil || _listener==nil){
            //throw exception();
        }
    }
    
}