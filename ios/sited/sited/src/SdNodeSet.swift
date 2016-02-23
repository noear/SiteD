//
//  SdNodeSet.swift
//  ddcat
//
//  Created by 谢月甲 on 15/10/8.
//  Copyright © 2015年 noear. All rights reserved.
//

import Foundation
import SWXMLHash

public class SdNodeSet : ISdNode{
    var _items = [String:ISdNode]();
    var _nodes = [ISdNode]();
    public final var source:SdSource;
    
    public init(source:SdSource){
        self.source = source;
    }
    
    public func OnDidInit(){
    
    }
    
    public func nodeType()->Int{return 2;}
    
    public var attrs = SdAttributeList();
    
    func buildForNode(cfg:XMLIndexer)->SdNodeSet{
        if(cfg.element == nil){
            return self;
        }
        
        _items.removeAll();
        _nodes.removeAll();
        attrs.clear();
        
        for (k,v)in cfg.element!.attributes{
            attrs.set(k, val: v);
        }
        
        for (n1) in cfg.children
        {
            let e1 = n1.element;
            if(e1?.attributes.count>0){
                let temp = Util.createNode(source).buildForNode(n1);
                self.add(e1!.name, node:temp);
            }else{ //说明是Set类型
                let temp = Util.createNodeSet(source).buildForNode(n1);
                self.add(e1!.name, node: temp);
            }
        }
    
        return self;
    }
    
    public func nodes()->[ISdNode]{
        return _nodes;
    }
    
    public func get(name:String)->ISdNode!{
        if(_items.contains({(k,v) -> Bool in return name==k;})){
            return _items[name]!;
        }else{
            return Util.createNode(source).buildForNode(nil);
        }
    }
    
    func add(name:String, node:ISdNode){
        _items[name] = node;
        _nodes.append(node);
    }
}