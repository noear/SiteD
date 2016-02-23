//
//  __Regex.swift
//  sited
//
//  Created by 谢月甲 on 15/10/12.
//  Copyright © 2015年 noear. All rights reserved.
//

import Foundation

class Regex {
    let internalExpression: NSRegularExpression?
    let pattern: String
    
    init(_ pattern: String) {
        self.pattern = pattern
        do{
         try internalExpression = NSRegularExpression(pattern: pattern, options:.CaseInsensitive);
        }catch{
            internalExpression = nil;
        }
    }
    
    func test(input: String) -> Bool {
        if let matches = internalExpression?.matchesInString(input,
            options: NSMatchingOptions.Anchored,
            range: NSMakeRange(0, input.characters.count)) {
                return matches.count > 0
        } else {
            return false
        }
    }
}