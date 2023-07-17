// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "EffectBeautyEffectModel.h"

#define BEAUTY_CONFIG_PATH [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject] stringByAppendingPathComponent:@"effect_cahce.plist"]

@implementation EffectBeautyEffectModel

+ (NSArray *)localModelArray {
    NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile:BEAUTY_CONFIG_PATH];

    NSString *uid = [LocalUserComponent userModel].uid;

    NSArray *beautyItems = [dict objectForKey:uid];
    
    if (!beautyItems) {
        NSString *filePath = [[NSBundle mainBundle] pathForResource:@"effect_beauty_default_config" ofType:@"plist"];
        beautyItems = [NSArray arrayWithContentsOfFile:filePath];
    }

    NSMutableArray *modelArray = [NSMutableArray array];

    for (int i = 0; i < beautyItems.count; i++) {
        NSArray *itmes = beautyItems[i];
        NSArray *array = [NSArray yy_modelArrayWithClass:[EffectBeautyEffectModel class] json:itmes];
        [modelArray addObject:array];
    }

    return modelArray.copy;
}

+ (void)saveBeautyConfig:(NSArray *)modelArray {
    NSArray *objectArray = [modelArray yy_modelToJSONObject];

    NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile:BEAUTY_CONFIG_PATH];

    NSMutableDictionary *dictM = [NSMutableDictionary dictionary];
    [dictM addEntriesFromDictionary:dict];

    NSString *uid = [LocalUserComponent userModel].uid;
    if (!uid) {
        return;
    }
    [dictM setObject:objectArray forKey:uid];

    [dictM writeToFile:BEAUTY_CONFIG_PATH atomically:YES];
}

+ (void)reset {
    NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile:BEAUTY_CONFIG_PATH];

    NSMutableDictionary *dictM = [NSMutableDictionary dictionary];
    [dictM addEntriesFromDictionary:dict];
    NSString *uid = [LocalUserComponent userModel].uid;
    if ([dictM objectForKey:uid]) {
        [dictM removeObjectForKey:uid];
        [dictM writeToFile:BEAUTY_CONFIG_PATH atomically:YES];
    }
}

@end
