//
//  SdLogListener.swift
//  sited
//
//  Created by 谢月甲 on 16/2/4.
//  Copyright © 2016年 noear. All rights reserved.
//

import Foundation

public typealias SdLogListener = (source:SdSource,tag:String,msg:String,tr:NSObject)->()
