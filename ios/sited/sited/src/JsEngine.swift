//
//  JsEngine.swift
//  ddcat
//
//  Created by 谢月甲 on 15/10/8.
//  Copyright © 2015年 noear. All rights reserved.
//

import Foundation
import JavaScriptCore

class JsEngine {
    private var engine:JSContext;
    
    init(source:SdSource){
        engine = JSContext();
    }
    
    func loadJs(funs:String)->JsEngine
    {
        engine.evaluateScript(funs);
        return self;
    }
    
    func callJs(fun:String,args:[AnyObject]!)->String
    {
        let jsFun = engine.objectForKeyedSubscript(fun);
        let rst = jsFun.callWithArguments(args);
        
        return rst.toString();
    }
}