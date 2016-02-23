//
//  SdJscript.swift
//  ddcat
//
//  Created by 谢月甲 on 15/10/8.
//  Copyright © 2015年 noear. All rights reserved.
//

import Foundation
import SWXMLHash

class SdJscript: NSObject {
    var require:SdNode;
    var code:String;
    private var s:SdSource;
    
    init(source:SdSource, node:XMLIndexer) {
        self.s = source;
        self.code =  node.element!.attributes["code"]!;
        self.require = Util.createNode(source).buildForNode(node["require"]);
    }
    
    
    func loadJs(js:JsEngine){
        if (!require.isEmpty()) {
//            for(SdNode n1 in require.items){
//                Util.dohttp(<#T##source: SdSource##SdSource#>, isUpdate: <#T##Bool#>, url: <#T##String#>, params: <#T##Dictionary<String, String>#>, config: <#T##SdNode#>, callback: <#T##((code: Int, text: String) -> ())##((code: Int, text: String) -> ())##(code: Int, text: String) -> ()#>)
//            }
//            for(SdNode n1 in _require.items){
//                [Util doHttp:_s url:n1.url params:nil config:n1 callback:^(int code, NSString *text) {
//                    if(code==0){
//                    [js loadJs:text];
//                    }
//                    }];
//            }
        }
    }
}