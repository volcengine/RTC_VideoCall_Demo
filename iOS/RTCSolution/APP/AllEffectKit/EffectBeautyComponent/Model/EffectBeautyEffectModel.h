// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface EffectBeautyEffectModel : NSObject

@property (nonatomic, assign) NSInteger type;
@property (nonatomic, assign) NSInteger subType;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *imageName;
@property (nonatomic, assign) BOOL selected;
@property (nonatomic, assign) BOOL valueChanged;
@property (nonatomic, assign) CGFloat value;

+ (NSArray *)localModelArray;

+ (void)saveBeautyConfig:(NSArray *)modelArray;

+ (void)reset;

@end

NS_ASSUME_NONNULL_END
