//
//  __FileCache.swift
//  sited
//
//  Created by 谢月甲 on 15/10/12.
//  Copyright © 2015年 noear. All rights reserved.
//

import Foundation

internal class FileCache : ICache{
    func save(key:String, data:String){
    
    }
    
    func get(key:String)->CacheBlock{
        return CacheBlock();
    }
    
    func delete(key:String){
    }
    
    func isCached(key:String)->Bool{
        return false;
    }
}