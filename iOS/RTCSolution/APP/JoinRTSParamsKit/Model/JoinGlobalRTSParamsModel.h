// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface JoinGlobalRTSParamsModel : NSObject

@property (nonatomic, copy) NSString *rtsAppId;

@property (nonatomic, copy) NSString *rtsUserId;

@property (nonatomic, copy) NSString *rtsToken;

@property (nonatomic, copy) NSString *serverUrl;

@property (nonatomic, copy) NSString *serverSignature;

@property (nonatomic, copy) NSDictionary *sceneInfo;

@end

NS_ASSUME_NONNULL_END
