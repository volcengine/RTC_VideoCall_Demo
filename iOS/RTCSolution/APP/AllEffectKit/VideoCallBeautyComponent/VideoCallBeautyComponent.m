// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallBeautyComponent.h"
#import "EffectResource.h"
#import "VideoCallBeautyView.h"

static CGFloat const kBeautyViewHeight = 233;

@interface VideoCallBeautyComponent ()<VideoCallBeautyViewDelegate>

@property (nonatomic, weak) UIView *superView;
@property (nonatomic, strong) UIButton *maskButton;
@property (nonatomic, copy) NSArray *makeupNodes;
@property (nonatomic, copy) void (^clickCloseBlock)(BOOL result);
@property (nonatomic, strong) ByteRTCVideoEffect *effect;
@property (nonatomic, strong) VideoCallBeautyView *videoCallBeautyView;

@property (nonatomic, strong) NSArray<VideoCallEffectItem *> *itemArray;

@end

@implementation VideoCallBeautyComponent


- (instancetype)initWithRTCEngineKit:(ByteRTCVideo *)rtcEngineKit{
    if (self = [super init]) {
        self.effect = [rtcEngineKit getVideoEffectInterface];
        
        NSString *licensePath = [EffectResource licensePath];
        NSString *modelPath = [EffectResource modelPath];
        
    
        int errorCode = [self.effect initCVResource:licensePath withAlgoModelDir:modelPath];
//        if (errorCode == 0) {
//            NSLog(@"check license success");
//        } else {
//            NSLog(@"check license failed");
//            return nil;
//        }
        
        [self.effect enableVideoEffect];
        
        NSArray *composeNameArray = @[@"beauty_IOS_lite", @"reshape_lite", @"beauty_4Items", @"reshape_boy", @"reshape_girl", @"reshape_nature"];
        NSMutableArray *pathArray = [NSMutableArray array];
        for (NSString *composeName in composeNameArray) {
            NSString *path = [EffectResource makeupMatarialPathWithName:composeName];
            if (path) {
                [pathArray addObject:path];
            }
        }
        self.makeupNodes = pathArray.copy;
        [self.effect setEffectNodes:self.makeupNodes];
    }
    return self;
}

- (void)showWithView:(UIView *)superView
        dismissBlock:(void (^)(BOOL result))block {
    [self showInView:superView animated:YES dismissBlock:block];
}

- (void)showInView:(UIView *)superView animated:(BOOL)animated dismissBlock:(void (^)(BOOL))block {
    self.superView = superView;
    _clickCloseBlock = block;
    
    self.videoCallBeautyView.itemArray = self.itemArray;
    UIView *beautyView = self.videoCallBeautyView;
    if (!animated) {
        [superView addSubview:beautyView];
        [beautyView mas_makeConstraints:^(MASConstraintMaker *make) {
            CGFloat height = kBeautyViewHeight + [DeviceInforTool getVirtualHomeHeight];
            make.left.right.equalTo(superView);
            make.height.mas_equalTo(height);
            make.bottom.equalTo(superView);
        }];
    } else {
        [self animatedShowBeautyView:beautyView];
    }
}

- (void)animatedShowBeautyView:(UIView *)view {
    [self.superView addSubview:self.maskButton];
    [self.maskButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.superView);
    }];
    
    [self.superView addSubview:view];
    [view mas_makeConstraints:^(MASConstraintMaker *make) {
        CGFloat height = kBeautyViewHeight + [DeviceInforTool getVirtualHomeHeight];
        make.left.right.equalTo(self.superView);
        make.height.mas_equalTo(height);
        make.bottom.equalTo(self.superView).offset(height);
    }];

    [self.superView layoutIfNeeded];
    [self.superView setNeedsUpdateConstraints];

    [UIView animateWithDuration:0.25
                     animations:^{
        [view mas_updateConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(self.superView);
        }];
        [self.superView layoutIfNeeded];
    }];
}

- (void)close {
    if (_videoCallBeautyView.superview) {
        [_videoCallBeautyView saveBeautyConfig];
        [_videoCallBeautyView removeFromSuperview];
    }

    if (self.maskButton.superview) {
        [self.maskButton removeFromSuperview];
        self.maskButton = nil;
    }
}

- (void)resume {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        
        VideoCallEffectItem *beautyItem = self.itemArray[VideoCallEffectItemTypeBeauty];
        VideoCallEffectItem *reshapeItem = self.itemArray[VideoCallEffectItemTypeReshape];
        VideoCallEffectItem *filterItem = self.itemArray[VideoCallEffectItemTypeFilter];
        
        // 美颜
        [self updateRTCBeautyEffect:beautyItem isClean:NO];
        // 美型
        for (VideoCallEffectItem *obj in reshapeItem.childrens) {
            if (obj.subType == VideoCallEffectItemSubTypeReshapeFace) {
                // 瘦脸
                for (VideoCallEffectItem *item in obj.childrens) {
                    if (item.selected) {
                        [self updateRTCBeautyEffect:item isClean:NO];
                    }
                }
            } else {
                [self updateRTCBeautyEffect:obj isClean:NO];
            }
        }
        // 滤镜
        VideoCallEffectItem *selectdfilter = nil;
        for (VideoCallEffectItem *obj in filterItem.childrens) {
            for (VideoCallEffectItem *item in obj.childrens) {
                if (item.selected) {
                    selectdfilter = item;
                    break;
                }
            }
            if (selectdfilter) {
                break;
            }
        }
        [self updateRTCFilterEffect:selectdfilter];
        
    });
}


- (void)saveBeautyConfig {
    [self.videoCallBeautyView saveBeautyConfig];
}

- (void)reset {
    [VideoCallEffectItem reset];
}

#pragma mark - BytedEffectProtocol

- (instancetype)protocol:(BytedEffectProtocol *)protocol
    initWithRTCEngineKit:(ByteRTCVideo *)rtcEngineKit {
    return [self initWithRTCEngineKit:rtcEngineKit];
}

- (void)protocol:(BytedEffectProtocol *)protocol
    showWithView:(UIView *)superView
    dismissBlock:(void (^)(BOOL result))block {
    [self showWithView:superView dismissBlock:block];
}

- (void)protocol:(BytedEffectProtocol *)protocol showInView:(UIView *)superView animated:(BOOL)animated dismissBlock:(void (^)(BOOL))block {
    [self showInView:superView animated:animated dismissBlock:block];
}

- (void)protocol:(BytedEffectProtocol *)protocol close:(BOOL)result {
    [self close];
}

- (void)protocol:(BytedEffectProtocol *)protocol resume:(BOOL)result {
    [self resume];
}

- (void)protocol:(BytedEffectProtocol *)protocol saveBeautyConfig:(BOOL)result {
    [self saveBeautyConfig];
}

- (void)protocol:(BytedEffectProtocol *)protocol reset:(BOOL)result {
    [self reset];
}

#pragma mark - Touch Action

- (void)maskButtonAction {
    [self close];
    if (_clickCloseBlock) {
        _clickCloseBlock(YES);
    }
}

#pragma mark - VideoCallBeautyViewDelegate
- (void)videoCallBeautyView:(VideoCallBeautyView *)beautyView didCleanEffect:(VideoCallEffectItem *)item {
    if (item.type == VideoCallEffectItemTypeFilter) {
        [self updateRTCFilterEffect:nil];
    } else {
        [self updateRTCBeautyEffect:item isClean:YES];
    }
}

- (void)videoCallBeautyView:(VideoCallBeautyView *)beautyView didReloadEffectItem:(VideoCallEffectItem *)item {
    if (item.type == VideoCallEffectItemTypeFilter) {
        [self updateRTCFilterEffect:item];
    } else {
        [self updateRTCBeautyEffect:item isClean:NO];
    }
}

- (void)videoCallBeautyView:(VideoCallBeautyView *)beautyView didChangeEffectItemValue:(VideoCallEffectItem *)item {
    if (!item) {
        return;
    }
    if (item.type == VideoCallEffectItemTypeFilter) {
        [self.effect setColorFilterIntensity:item.value];
    } else {
        [self.effect updateEffectNode:[EffectResource makeupMatarialPathWithName:item.composeName] key:item.cvKey value:item.value];
    }
}

- (void)videoCallBeautyViewDidReset:(VideoCallBeautyView *)beautyView {
    [self reset];
    _itemArray = nil;
    self.videoCallBeautyView.itemArray = self.itemArray;
    [self resume];
}

#pragma mark - private
- (void)updateRTCBeautyEffect:(VideoCallEffectItem *)item isClean:(BOOL)isClean {
    if (item.childrens.count > 0) {
        for (VideoCallEffectItem *obj in item.childrens) {
            [self updateRTCBeautyEffect:obj isClean:isClean];
        }
    } else {
        if (item.cvKey && item.composeName) {
            CGFloat value = isClean ? 0 : item.value;
            [self.effect updateEffectNode:[EffectResource makeupMatarialPathWithName:item.composeName] key:item.cvKey value:value];
        }
    }
}

- (void)updateRTCFilterEffect:(VideoCallEffectItem *)item {
    NSString *filterName = item.filterName ?: @"";
    NSString *filterPath = [EffectResource filterPathWithName:filterName];
    [self.effect setColorFilter:filterPath];
    [self.effect setColorFilterIntensity:item.value];
}

#pragma mark - Getter

- (VideoCallBeautyView *)videoCallBeautyView {
    if (!_videoCallBeautyView) {
        _videoCallBeautyView = [[VideoCallBeautyView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, kBeautyViewHeight)];
        _videoCallBeautyView.delegate = self;
    }
    return _videoCallBeautyView;
}

- (UIButton *)maskButton {
    if (!_maskButton) {
        _maskButton = [[UIButton alloc] init];
        [_maskButton addTarget:self action:@selector(maskButtonAction) forControlEvents:UIControlEventTouchUpInside];
        [_maskButton setBackgroundColor:[UIColor clearColor]];
    }
    return _maskButton;
}

- (NSArray<VideoCallEffectItem *> *)itemArray {
    if (!_itemArray) {
        _itemArray = [VideoCallEffectItem localItemArray];
    }
    return _itemArray;
}

- (void)dealloc {
    NSLog(@"%@,%s", [NSThread currentThread], __func__);
}


@end
