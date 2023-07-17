// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "JoinRTSParamsModel.h"
#import "JoinRTSInputModel.h"
#import "JoinGlobalRTSParamsModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface JoinRTSParams : NSObject

/**
 * @brief 获取全局 RTS login 接口参数
 * @param loginToken 登录用户Token
 * @param block callback
 */
+ (void)getJoinGlobalRTSParams:(NSString *)loginToken
                         block:(void(^)(JoinGlobalRTSParamsModel *_Nullable model))block;


/*
 * Get RTS login information
 * @param Input data model
 * @param block callback
 */
+ (void)getJoinRTSParams:(JoinRTSInputModel *)inputModel
                   block:(void (^)(JoinRTSParamsModel *model))block;
                          
/*
* Network request public parameter usage
* @param dic Dic parameter, can be nil
*/
+ (NSDictionary *)addTokenToParams:(NSDictionary * _Nullable)dic;

@end

NS_ASSUME_NONNULL_END
