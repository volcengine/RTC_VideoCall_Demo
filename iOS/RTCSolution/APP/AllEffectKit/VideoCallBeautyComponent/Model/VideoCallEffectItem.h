// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, VideoCallEffectItemType) {
    VideoCallEffectItemTypeBeauty,
    VideoCallEffectItemTypeReshape,
    VideoCallEffectItemTypeFilter,
};

typedef NS_ENUM(NSInteger, VideoCallEffectItemSubType) {
    VideoCallEffectItemSubTypeClean = 1,
    VideoCallEffectItemSubTypeReshapeFace,
    VideoCallEffectItemSubTypeFilter,
};

NS_ASSUME_NONNULL_BEGIN

@interface VideoCallEffectItem : NSObject

@property (nonatomic, assign) VideoCallEffectItemType type;
@property (nonatomic, assign) VideoCallEffectItemSubType subType;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *imageName;
@property (nonatomic, assign) BOOL selected;
@property (nonatomic, assign) BOOL valueChanged;
@property (nonatomic, assign) CGFloat value;

@property (nonatomic, strong) VideoCallEffectItem *selectItem;

// 美颜美型
@property (nonatomic, copy) NSString *cvKey;
@property (nonatomic, copy) NSString *composeName;
// 滤镜名称
@property (nonatomic, copy) NSString *filterName;

@property (nonatomic, copy) NSArray<VideoCallEffectItem *> *childrens;

- (BOOL)subSelected;

+ (NSArray<VideoCallEffectItem *> *)localItemArray;

+ (void)saveBeautyConfig:(NSArray<VideoCallEffectItem *> *)itemArray;

+ (void)reset;


@end

NS_ASSUME_NONNULL_END
