// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "EffectResource.h"

@implementation EffectResource

+ (NSString *)licensePath {
    NSString *licenseName = CVLicenseName;
    NSString *bunldePath = [[NSBundle mainBundle] pathForResource:@"LicenseBag" ofType:@"bundle"];

    NSString *licensePath = [NSString stringWithFormat:@"%@/%@", bunldePath, licenseName];

    [self checkPathExsit:licensePath];

    return licensePath;
}

+ (NSString *)modelPath {
    NSString *modelPath = [[NSBundle mainBundle] pathForResource:@"ModelResource" ofType:@"bundle"];

    [self checkPathExsit:modelPath];

    return modelPath;
}

+ (NSString *)beautyCameraPath {
    NSString *bunldePath = [[NSBundle mainBundle] pathForResource:@"ComposeMakeup" ofType:@"bundle"];

    NSString *beautyCameraPath = [NSString stringWithFormat:@"%@/ComposeMakeup/beauty_IOS_live", bunldePath];

    [self checkPathExsit:beautyCameraPath];

    return beautyCameraPath;
}

+ (NSString *)reshapeCameraPath {
    NSString *bunldePath = [[NSBundle mainBundle] pathForResource:@"ComposeMakeup" ofType:@"bundle"];

    NSString *reshapeCameraPath = [NSString stringWithFormat:@"%@/ComposeMakeup/reshape_live", bunldePath];

    [self checkPathExsit:reshapeCameraPath];

    return reshapeCameraPath;
}

+ (NSString *)makeupMatarialPathWithName:(NSString *)name {
    NSString *bunldePath = [[NSBundle mainBundle] pathForResource:@"ComposeMakeup" ofType:@"bundle"];

    NSString *beautyCameraPath = [NSString stringWithFormat:@"%@/ComposeMakeup/%@", bunldePath, name];

    [self checkPathExsit:beautyCameraPath];

    return beautyCameraPath;
}

+ (NSString *)stickerPathWithName:(NSString *)stickerName {
    NSString *bunldePath = [[NSBundle mainBundle] pathForResource:@"StickerResource" ofType:@"bundle"];

    NSString *stickerPath = [NSString stringWithFormat:@"%@/stickers/%@", bunldePath, stickerName];

    [self checkPathExsit:stickerPath];

    return stickerPath;
}

+ (NSString *)filterPathWithName:(NSString *)filterName {
    if ([filterName isEqualToString:@"landiaojiaopian"]) {
        filterName = @"Filter_47_S5";
    }

    if ([filterName isEqualToString:@"lianaichaotian"]) {
        filterName = @"Filter_24_Po2";
    }

    if ([filterName isEqualToString:@"yese"]) {
        filterName = @"Filter_35_L3";
    }

    if ([filterName isEqualToString:@"lengyang"]) {
        filterName = @"Filter_30_Po8";
    }

    NSString *bunldePath = [[NSBundle mainBundle] pathForResource:@"FilterResource" ofType:@"bundle"];

    NSString *filterPath = [NSString stringWithFormat:@"%@/Filter/%@", bunldePath, filterName];

    [self checkPathExsit:filterPath];

    return filterPath;
}

+ (void)checkPathExsit:(NSString *)path {
    BOOL isDirectory;
    BOOL exist = [[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDirectory];
    
    NSLog(@"path = %@, path exist? %d", path, exist);

//    NSAssert(exist == YES, @"path = %@ is not exist", path);
}
@end
