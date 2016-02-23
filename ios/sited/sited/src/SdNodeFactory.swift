//
//  SdNodeFactory.swift
//  sited
//
//  Created by 谢月甲 on 16/2/4.
//  Copyright © 2016年 noear. All rights reserved.
//

import Foundation

public class SdNodeFactory {
    
    public init(){
        
    }
    
    public func createNode(source:SdSource)->SdNode!
    {
        return SdNode(source: source);
    }
    
    public func createNodeSet(source:SdSource)->SdNodeSet!
    {
        return SdNodeSet(source: source);
    }
}