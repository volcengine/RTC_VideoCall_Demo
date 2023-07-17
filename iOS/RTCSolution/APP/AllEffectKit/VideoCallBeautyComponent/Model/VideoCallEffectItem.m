// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallEffectItem.h"

#define VIDEO_CALL_BEAUTY_CONFIG_PATH [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject] stringByAppendingPathComponent:@"video_calleffect_cahce.plist"]

@implementation VideoCallEffectItem

+ (NSDictionary *)modelContainerPropertyGenericClass {
    return @{@"childrens" : [VideoCallEffectItem class]};
}

- (instancetype)initWithType:(VideoCallEffectItemType)type title:(NSString *)title imageName:(NSString *)imageName value:(CGFloat)value cvKey:(NSString *)cvKey composeName:(NSString *)composeName {
    if (self = [super init]) {
        self.type = type;
        self.title = title;
        self.imageName = imageName;
        self.value = value;
        self.cvKey = cvKey;
        self.composeName = composeName;
    }
    return self;
}

- (instancetype)initWithType:(VideoCallEffectItemType)type title:(NSString *)title imageName:(NSString *)imageName value:(CGFloat)value filterName:(NSString *)filterName {
    if (self = [super init]) {
        self.type = type;
        self.title = title;
        self.imageName = imageName;
        self.value = value;
        self.filterName = filterName;
    }
    return self;
}

+ (NSArray<VideoCallEffectItem *> *)beautyEffects {
    NSString *composeName = @"beauty_IOS_lite";
    VideoCallEffectItemType type = VideoCallEffectItemTypeBeauty;
     NSArray<VideoCallEffectItem *> *items = @[
        [[VideoCallEffectItem alloc] initWithType:type title:@"none" imageName:@"InteractiveLive_no_beauty" value:0 cvKey:nil composeName:nil],
        [[VideoCallEffectItem alloc] initWithType:type title:@"whitening" imageName:@"beauty_white" value:0.7 cvKey:@"whiten" composeName:composeName],
        [[VideoCallEffectItem alloc] initWithType:type title:@"microdermabrasion" imageName:@"beauty_smooth" value:0.8 cvKey:@"smooth" composeName:composeName],
        [[VideoCallEffectItem alloc] initWithType:type title:@"beauty_sharpen" imageName:@"beauty_sharp" value:0.5 cvKey:@"sharp" composeName:composeName],
        [[VideoCallEffectItem alloc] initWithType:type title:@"beauty_clear" imageName:@"beauty_clear" value:0.7 cvKey:@"clear" composeName:composeName],
    ];
    items.firstObject.subType = VideoCallEffectItemSubTypeClean;
    
    return items;
}

+ (NSArray<VideoCallEffectItem *> *)reshapeFaceEffects {
    NSString *cvKey = @"Internal_Deform_Overall";
    VideoCallEffectItemType type = VideoCallEffectItemTypeReshape;
    NSArray<VideoCallEffectItem *> *items = @[
        [[VideoCallEffectItem alloc] initWithType:type title:@"thin_face" imageName:@"reshape_face_normal" value:0.8 cvKey:cvKey composeName:@"reshape_lite"],
        [[VideoCallEffectItem alloc] initWithType:type title:@"reshape_face_boy" imageName:@"reshape_face_boy" value:0.8 cvKey:cvKey composeName:@"reshape_boy"],
        [[VideoCallEffectItem alloc] initWithType:type title:@"reshape_face_girl" imageName:@"reshape_face_girl" value:0.8 cvKey:cvKey composeName:@"reshape_girl"],
        [[VideoCallEffectItem alloc] initWithType:type title:@"reshape_face_nature" imageName:@"reshape_face_nature" value:0.8 cvKey:cvKey composeName:@"reshape_nature"],
    ];
    for (VideoCallEffectItem *item in items) {
        item.subType = VideoCallEffectItemSubTypeReshapeFace;
    }
    
    return items;
}

+ (NSArray<VideoCallEffectItem *> *)reshapeEffects {
    NSString *composeName = @"reshape_lite";
    VideoCallEffectItemType type = VideoCallEffectItemTypeReshape;
    
    VideoCallEffectItem *faceItems = [[VideoCallEffectItem alloc] initWithType:type title:@"thin_face" imageName:@"reshape_face" value:0 cvKey:nil composeName:nil];
    faceItems.subType = VideoCallEffectItemSubTypeReshapeFace;
    faceItems.childrens = [self reshapeFaceEffects];
    
    NSArray<VideoCallEffectItem *> *items =  @[
        [[VideoCallEffectItem alloc] initWithType:type title:@"none" imageName:@"InteractiveLive_no_beauty" value:0 cvKey:nil composeName:nil],
        [[VideoCallEffectItem alloc] initWithType:type title:@"big_eyes" imageName:@"reshape_eye" value:0.3 cvKey:@"Internal_Deform_Eye" composeName:composeName],
        faceItems,
        [[VideoCallEffectItem alloc] initWithType:type title:@"beauty_narrow_face" imageName:@"reshape_cut_face" value:0 cvKey:@"Internal_Deform_CutFace" composeName:composeName],
        [[VideoCallEffectItem alloc] initWithType:type title:@"beauty_cheekbones" imageName:@"reshape_zoom_cheekbone" value:0 cvKey:@"Internal_Deform_Zoom_Cheekbone" composeName:composeName],
        [[VideoCallEffectItem alloc] initWithType:type title:@"beauty_jaw" imageName:@"reshape_zoom_jawbone" value:0 cvKey:@"Internal_Deform_Zoom_Jawbone" composeName:composeName],
        [[VideoCallEffectItem alloc] initWithType:type title:@"beauty_nose" imageName:@"reshape_nose" value:0.5 cvKey:@"Internal_Deform_Nose" composeName:composeName],
        [[VideoCallEffectItem alloc] initWithType:type title:@"beauty_law_pattern" imageName:@"reshape_smiles_folds" value:0.7 cvKey:@"BEF_BEAUTY_SMILES_FOLDS" composeName:@"beauty_4Items"],
        [[VideoCallEffectItem alloc] initWithType:type title:@"beauty_dark_circles" imageName:@"reshape_remove_pouch" value:0.6 cvKey:@"BEF_BEAUTY_REMOVE_POUCH" composeName:@"beauty_4Items"],
    ];
    
    items.firstObject.subType = VideoCallEffectItemSubTypeClean;
    return items;
}

+ (NSArray<VideoCallEffectItem *> *)filterEffects {
    NSString *path = [[NSBundle mainBundle] pathForResource:@"video_call_beauty_default" ofType:@"json"];
    NSData *data = [[NSData alloc] initWithContentsOfFile:path];
    
    if(!data) {
        return nil;
    }
    NSError* error = nil;
    NSArray * jsonarray = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:&error];
    
    if (error != nil) {
        NSLog(@"Error occur when parse custom.json, error is %@", error);
        return nil;
    }
    NSArray<VideoCallEffectItem *> *items = [NSArray yy_modelArrayWithClass:[VideoCallEffectItem class] json:jsonarray];
    
    for (VideoCallEffectItem *obj in items) {
        [self setItemFilterType:obj];
    }
    items.firstObject.subType = VideoCallEffectItemSubTypeClean;
    return items;
}

+ (void)setItemFilterType:(VideoCallEffectItem *)item {
    item.type = VideoCallEffectItemTypeFilter;
    if (item.childrens.count > 0) {
        for (VideoCallEffectItem *obj in item.childrens) {
            obj.type = VideoCallEffectItemTypeFilter;
            obj.subType = VideoCallEffectItemSubTypeFilter;
        }
    }
}

#pragma mark - public class methods
+ (NSArray<VideoCallEffectItem *> *)defaultEffects {
    
    VideoCallEffectItem *beauty = [[VideoCallEffectItem alloc] initWithType:VideoCallEffectItemTypeBeauty title:@"beauty" imageName:nil value:0 cvKey:nil composeName:nil];
    beauty.childrens = [self beautyEffects];
    beauty.selectItem = beauty.childrens[1];
    beauty.childrens[1].selected = YES;
    
    VideoCallEffectItem *reshape = [[VideoCallEffectItem alloc] initWithType:VideoCallEffectItemTypeReshape title:@"beauty_shap" imageName:nil value:0 cvKey:nil composeName:nil];
    reshape.childrens = [self reshapeEffects];
    reshape.selectItem = reshape.childrens[1];
    reshape.childrens[1].selected = YES;
    
    VideoCallEffectItem *filter = [[VideoCallEffectItem alloc] initWithType:VideoCallEffectItemTypeFilter title:@"filter" imageName:nil value:0 cvKey:nil composeName:nil];
    filter.childrens = [self filterEffects];
    
    NSArray<VideoCallEffectItem *> *array = @[beauty, reshape, filter];
    return array;
}

+ (NSArray<VideoCallEffectItem *> *)localItemArray {
    NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile:VIDEO_CALL_BEAUTY_CONFIG_PATH];
    NSString *uid = [LocalUserComponent userModel].uid;
    NSArray *beautyItems = [dict objectForKey:uid];
    
    if (!beautyItems) {
        return [self defaultEffects];
    }

    NSArray *itemArray = [NSArray yy_modelArrayWithClass:[VideoCallEffectItem class] json:beautyItems];

    return itemArray;
}

+ (void)saveBeautyConfig:(NSArray<VideoCallEffectItem *> *)itemArray {
    NSArray *objectArray = [itemArray yy_modelToJSONObject];

    NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile:VIDEO_CALL_BEAUTY_CONFIG_PATH];

    NSMutableDictionary *dictM = [NSMutableDictionary dictionary];
    [dictM addEntriesFromDictionary:dict];

    NSString *uid = [LocalUserComponent userModel].uid;
    if (!uid) {
        return;
    }
    [dictM setObject:objectArray forKey:uid];

    [dictM writeToFile:VIDEO_CALL_BEAUTY_CONFIG_PATH atomically:YES];
}

+ (void)reset {
    NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile:VIDEO_CALL_BEAUTY_CONFIG_PATH];

    NSMutableDictionary *dictM = [NSMutableDictionary dictionary];
    [dictM addEntriesFromDictionary:dict];
    NSString *uid = [LocalUserComponent userModel].uid;
    if ([dictM objectForKey:uid]) {
        [dictM removeObjectForKey:uid];
        [dictM writeToFile:VIDEO_CALL_BEAUTY_CONFIG_PATH atomically:YES];
    }
}

#pragma mark - public
- (BOOL)subSelected {
    if (self.type == VideoCallEffectItemTypeFilter) {
        if (self.subType != VideoCallEffectItemSubTypeClean && self.childrens.count > 0) {
            for (VideoCallEffectItem *obj in self.childrens) {
                if (obj.selected) {
                    return YES;
                }
            }
        }
    } else if (self.type == VideoCallEffectItemTypeReshape) {
        if (self.subType == VideoCallEffectItemSubTypeReshapeFace && self.childrens.count > 0) {
            for (VideoCallEffectItem *obj in self.childrens) {
                if (obj.selected) {
                    return YES;
                }
            }
        }
    }
    return NO;
}


@end
