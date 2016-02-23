//
//  Exts.swift
//  sited
//
//  Created by 谢月甲 on 15/10/9.
//  Copyright © 2015年 noear. All rights reserved.
//

import Foundation

extension String{
    
    static func isNullOrEmpty(str:String?)->Bool{
        return (str==nil || str!.isEmpty);
    }
    
    func indexOf(searchstring:String)->Int
    {
        if let range = self.rangeOfString(searchstring) {
            return self.startIndex.distanceTo(range.startIndex);
        }else {
            return -1;
        }
    }
    
    func split(key:String)->[String]{
        return [];
    }
    
    func replace(oldVal:String, newVal:String)->String
    {
        return self.stringByReplacingOccurrencesOfString(oldVal,withString:newVal);
    }
    
    
    func length()->Int{
        return self.characters.count;
    }
    
    func contains(s: String) -> Bool {
        return (self.rangeOfString(s) != nil) ? true : false
    }
    
    func subString(start:Int)->String{
        return self.subString(start, end: self.length() - start);
    }
    
    func subString(start:Int,end:Int)->String{
        let range=NSMakeRange(start,end-start);
        
        return (self as NSString).substringWithRange(range);
    }
}

extension Dictionary{
    func containsKey(key:Key)->Bool{
        return contains({(k,v)->Bool in return k==key});
    }
}