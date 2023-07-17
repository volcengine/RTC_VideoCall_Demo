// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface EffectResource : NSObject

+ (NSString *)licensePath;

+ (NSString *)modelPath;

+ (NSString *)beautyCameraPath;

+ (NSString *)reshapeCameraPath;

+ (NSString *)makeupMatarialPathWithName:(NSString *)name;

+ (NSString *)stickerPathWithName:(NSString *)stickerName;

+ (NSString *)filterPathWithName:(NSString *)filterName;
@end

NS_ASSUME_NONNULL_END
