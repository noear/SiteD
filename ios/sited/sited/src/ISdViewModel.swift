//
//  ISdViewModel.swift
//  sited
//
//  Created by 谢月甲 on 15/10/8.
//  Copyright © 2015年 noear. All rights reserved.
//

import Foundation

public protocol ISdViewModel{
    func loadByConfig(config:SdNode);
    func loadByJson(config:SdNode, jsons:[String]!);
}