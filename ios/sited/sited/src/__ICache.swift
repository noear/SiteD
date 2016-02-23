//
//  __ICache.swift
//  sited
//
//  Created by 谢月甲 on 15/10/12.
//  Copyright © 2015年 noear. All rights reserved.
//

import Foundation

internal protocol ICache{
    
    func save(key:String, data:String);
    func get(key:String)->CacheBlock;
    func delete(key:String);
    func isCached(key:String)->Bool;
}

