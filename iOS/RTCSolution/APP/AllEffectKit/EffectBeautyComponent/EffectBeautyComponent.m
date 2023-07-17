// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "EffectBeautyComponent.h"
#import "EffectResource.h"
#import "EffectBeautyView.h"
#import "EffectBeautyEffectModel.h"

static CGFloat const kBeautyViewHeight = 233;

@interface EffectBeautyComponent ()<EffectBeautyViewDelegate>

@property (nonatomic, weak) UIView *superView;
@property (nonatomic, strong) UIButton *maskButton;
@property (nonatomic, strong) EffectBeautyView *hostBeautyView;
@property (nonatomic, copy) NSArray *makeupNodes;
@property (nonatomic, copy) void (^clickCloseBlock)(BOOL result);
@property (nonatomic, strong) ByteRTCVideoEffect *videoEffect;

@end

@implementation EffectBeautyComponent

- (instancetype)initWithRTCEngineKit:(ByteRTCVideo *)rtcEngineKit {
    if (self = [super init]) {
        self.videoEffect = [rtcEngineKit getVideoEffectInterface];
        
        NSString *licensePath = [EffectResource licensePath];
        NSString *modelPath = [EffectResource modelPath];
        NSString *beautyPath = [EffectResource beautyCameraPath];
        NSString *resharppath = [EffectResource reshapeCameraPath];
        
        int errorCode = [self.videoEffect initCVResource:licensePath withAlgoModelDir:modelPath];
        if (errorCode == 0) {
            NSLog(@"check license success");
        } else {
            NSLog(@"check license failed");
            return nil;
        }
        [self.videoEffect enableVideoEffect];
        self.makeupNodes = @[ beautyPath ?: @"", resharppath ?: @"" ];
        [self.videoEffect setEffectNodes:self.makeupNodes];
    }
    return self;
}

- (void)showWithView:(UIView *)superView
        dismissBlock:(void (^)(BOOL result))block {
    self.superView = superView;
    _clickCloseBlock = block;
    
    EffectBeautyView *beautyView = self.hostBeautyView;
    [superView addSubview:self.maskButton];
    [self.maskButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(superView);
    }];
    
    [superView addSubview:beautyView];
    [beautyView mas_makeConstraints:^(MASConstraintMaker *make) {
        CGFloat height = kBeautyViewHeight + [DeviceInforTool getVirtualHomeHeight];
        make.left.right.equalTo(superView);
        make.height.mas_equalTo(height);
        make.bottom.equalTo(superView).offset(height);
    }];
    [beautyView reload];
    [superView layoutIfNeeded];
    [superView setNeedsUpdateConstraints];

    [UIView animateWithDuration:0.25
                     animations:^{
        [beautyView mas_updateConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(superView);
        }];
        [superView layoutIfNeeded];
    }];
}

- (void)showInView:(UIView *)superView animated:(BOOL)animated dismissBlock:(void (^)(BOOL))block {
    [self showWithView:superView dismissBlock:block];
}

- (void)close {
    if (_hostBeautyView.superview) {
        [_hostBeautyView saveBeautyConfig];
        [_hostBeautyView removeFromSuperview];
    }

    if (self.maskButton.superview) {
        [self.maskButton removeFromSuperview];
        self.maskButton = nil;
    }
}

- (void)resume {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        NSArray *modelArray = [EffectBeautyEffectModel localModelArray];
        if (modelArray.count == 0) {
            return;
        }

        NSInteger count = modelArray.count;
        for (int i = 0; i < count; i++) {
            NSArray *items = modelArray[i];
            for (int j = 0; j < items.count; j++) {
                EffectBeautyEffectModel *model = items[j];

                if (i == 0 && model.value) {
                    [self changeEffectValue:model];
                }

                if (i == 1 && model.value) {
                    [self didClickedEffect:model];
                    [self changeEffectValue:model];
                }

                if (i == 2 && model.selected == YES) {
                    [self didClickedEffect:model];
                }
            }
        }
    });
}

- (void)saveBeautyConfig {
    [self.hostBeautyView saveBeautyConfig];
}

- (void)reset {
    [EffectBeautyEffectModel reset];
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

#pragma mark - Private Action

- (void)changeEffectValue:(EffectBeautyEffectModel *_Nonnull)model {
    // Modify effect strength through UISlider
    if (model.type == 0) {
        // Beauty
        switch (model.subType) {
            case 0: {
                // None
            } break;
            case 1: {
                [self.videoEffect updateEffectNode:[EffectResource beautyCameraPath] key:@"whiten" value:model.value];
            } break;
            case 2: {
                [self.videoEffect updateEffectNode:[EffectResource beautyCameraPath] key:@"smooth" value:model.value];
            } break;
            case 3: {
                [self.videoEffect updateEffectNode:[EffectResource reshapeCameraPath] key:@"Internal_Deform_Overall" value:model.value];
            } break;
            case 4: {
                [self.videoEffect updateEffectNode:[EffectResource reshapeCameraPath] key:@"Internal_Deform_Eye" value:model.value];
            } break;
        }
    }

    if (model.type == 1) {
        // Filter
        [self.videoEffect setColorFilterIntensity:model.value];
    }
}

- (void)didClickedEffect:(EffectBeautyEffectModel *_Nonnull)model {
    // By clicking effect strength
    if (model.type == 2) {
        // Sticker
        NSString *stickerName = @"";
        switch (model.subType) {
            case 0: {
                // No stiker
            } break;
            case 1: {
                stickerName = @"shenxiangaoguang";
            } break;
            case 2: {
                stickerName = @"manhuanansheng";
            } break;
            case 3: {
                stickerName = @"suixingshan";
            } break;
            case 4: {
                stickerName = @"fuguxyanjing";
            } break;
            default:
                break;
        }

        NSString *stikerPath = [EffectResource stickerPathWithName:stickerName];

        if (model.selected && model.subType != 0) {
            NSArray *allNodes = [self.makeupNodes arrayByAddingObject:stikerPath];
            [self.videoEffect setEffectNodes:allNodes];
        } else {
            [self.videoEffect setEffectNodes:self.makeupNodes];
        }
    }

    if (model.type == 1) {
        // Filter
        NSString *filterName = @"";
        switch (model.subType) {
            case 0: {
                // None
            } break;
            case 1: {
                filterName = @"landiaojiaopian";
            } break;
            case 2: {
                filterName = @"lengyang";
            } break;
            case 3: {
                filterName = @"lianaichaotian";
            } break;
            case 4: {
                filterName = @"yese";
            } break;
            default:
                break;
        }

        NSString *filterPath = [EffectResource filterPathWithName:filterName];
        [self.videoEffect setColorFilter:filterPath];
        [self.videoEffect setColorFilterIntensity:model.value];
    }

    if (model.type == 0 && model.subType == 0) {
        [self.videoEffect updateEffectNode:[EffectResource beautyCameraPath] key:@"whiten" value:0];
        [self.videoEffect updateEffectNode:[EffectResource beautyCameraPath] key:@"smooth" value:0];
        [self.videoEffect updateEffectNode:[EffectResource reshapeCameraPath] key:@"Internal_Deform_Eye" value:0];
        [self.videoEffect updateEffectNode:[EffectResource reshapeCameraPath] key:@"Internal_Deform_Overall" value:0];
    }
}

#pragma mark - Delegate

- (void)effectBeautyView:(nonnull EffectBeautyView *)beautyView didChangeEffectValue:(EffectBeautyEffectModel *_Nonnull)model {
    [self changeEffectValue:model];
    [beautyView saveBeautyConfig];
}

- (void)effectBeautyView:(nonnull EffectBeautyView *)beautyView didClickedEffect:(EffectBeautyEffectModel *_Nonnull)model {
    [self didClickedEffect:model];
    [beautyView saveBeautyConfig];
}

#pragma mark - Getter

- (EffectBeautyView *)hostBeautyView {
    if (!_hostBeautyView) {
        _hostBeautyView = [[EffectBeautyView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, kBeautyViewHeight)];
        _hostBeautyView.delegate = self;
    }
    return _hostBeautyView;
}

- (UIButton *)maskButton {
    if (!_maskButton) {
        _maskButton = [[UIButton alloc] init];
        [_maskButton addTarget:self action:@selector(maskButtonAction) forControlEvents:UIControlEventTouchUpInside];
        [_maskButton setBackgroundColor:[UIColor clearColor]];
    }
    return _maskButton;
}

- (void)dealloc {
    NSLog(@"%@,%s", [NSThread currentThread], __func__);
}

@end
