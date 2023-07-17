// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallBeautyView.h"
#import "EffectBeautyItemButton.h"
#import "EffectBeautyTitleButton.h"
#import "VideoCallCollectionViewCell.h"
#import "VideoCallEffectSubView.h"

@interface VideoCallBeautyView ()<VideoCallEffectSubViewDelegate>

@property (nonatomic, strong) UISlider *slider;
@property (nonatomic, strong) UIView *contentView;
@property (nonatomic, strong) UILabel *sliderTipLabel;
@property (nonatomic, strong) UILabel *sliderValueLabel;
@property (nonatomic, assign) CGFloat lastValue;
@property (nonatomic, strong) VideoCallEffectSubView *rootEffectView;
@property (nonatomic, strong) VideoCallEffectItem *selectItem;

@end

@implementation VideoCallBeautyView

- (void)dealloc {
    [self saveBeautyConfig];
}

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        
        [self addSubview:self.sliderTipLabel];
        [self addSubview:self.sliderValueLabel];
        [self.sliderTipLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(self).offset(20);
            make.top.equalTo(self).offset(16);
        }];
        [self.sliderValueLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(self).offset(-20);
            make.centerY.equalTo(self.sliderTipLabel);
        }];
        
        [self addSubview:self.slider];
        [self.slider mas_makeConstraints:^(MASConstraintMaker *make) {
          make.top.mas_equalTo(30);
          make.height.mas_equalTo(44);
          make.left.mas_equalTo(30);
          make.right.mas_equalTo(-30);
        }];

        [self addSubview:self.contentView];
        [self.contentView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.left.right.bottom.equalTo(self);
          make.top.equalTo(self).offset(84);
        }];

        
        [self.contentView addSubview:self.rootEffectView];
        [self.rootEffectView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self.contentView);
        }];
    }
    return self;
}

#pragma mark - VideoCallEffectSubViewDelegate
- (void)videoCallEffectSubView:(VideoCallEffectSubView *)view onSelectedEffectType:(VideoCallEffectItemType)type {
    self.selectItem = self.itemArray[type].selectItem;
    [self updateSlideUI];
}

- (void)videoCallEffectSubView:(VideoCallEffectSubView *)view didClickEffectItem:(VideoCallEffectItem *)item {
    if (item.childrens.count > 0) {
        VideoCallEffectSubView *effectSubView = [[VideoCallEffectSubView alloc] initWithFrame:self.contentView.bounds rootView:NO];
        effectSubView.delegate = self;
        [self.contentView addSubview:effectSubView];
        effectSubView.item = item;
        
        if (item.subType == VideoCallEffectItemSubTypeReshapeFace) {
            for (VideoCallEffectItem *obj in item.childrens) {
                if (obj.selected) {
                    self.selectItem = obj;
                    [self updateSlideUI];
                }
            }
        }
    } else {
        if (item.subType == VideoCallEffectItemSubTypeClean) {
            
            [self clearItemValue:self.itemArray[item.type]];
            
            if ([self.delegate respondsToSelector:@selector(videoCallBeautyView:didCleanEffect:)]) {
                [self.delegate videoCallBeautyView:self didCleanEffect:self.itemArray[item.type]];
            }
            
        } else {
            
            if (item.subType == VideoCallEffectItemSubTypeReshapeFace) {
                if ([self.delegate respondsToSelector:@selector(videoCallBeautyView:didCleanEffect:)]) {
                    [self.delegate videoCallBeautyView:self didCleanEffect:view.item];
                }
            }
            
            if ([self.delegate respondsToSelector:@selector(videoCallBeautyView:didReloadEffectItem:)]) {
                [self.delegate videoCallBeautyView:self didReloadEffectItem:item];
            }
        }
        if (item.type == VideoCallEffectItemTypeFilter) {
            [self clearFilterSelectedState:item];
        } else {
            [self clearItemSelectedState:self.itemArray[item.type]];
            if (item.subType == VideoCallEffectItemSubTypeReshapeFace) {
                for (VideoCallEffectItem *obj in view.item.childrens) {
                    obj.selected = NO;
                }
            }
        }
        
        item.selected = YES;
        
        self.selectItem = item;
        [self updateSlideUI];
        
        self.itemArray[item.type].selectItem = item;
        [self.rootEffectView reloadData];
        
    }
}

- (void)videoCallEffectSubViewDidClickReset:(VideoCallEffectSubView *)view {
    if ([self.delegate respondsToSelector:@selector(videoCallBeautyViewDidReset:)]) {
        [self.delegate videoCallBeautyViewDidReset:self];
    }
}

#pragma mark - Private Action
- (void)saveBeautyConfig {
    [VideoCallEffectItem saveBeautyConfig:self.itemArray];
}

- (void)sliderValueChanged:(UISlider *)slider {
    NSString *currentStr = [NSString stringWithFormat:@"%.2f", slider.value];
    CGFloat currentValue = [currentStr floatValue];
    if (currentValue == self.lastValue) {
        return;
    }
    self.lastValue = currentValue;
    self.sliderValueLabel.text = [NSString stringWithFormat:@"%.0f", currentValue * 100];
    
    self.selectItem.value = currentValue;
    if ([self.delegate respondsToSelector:@selector(videoCallBeautyView:didChangeEffectItemValue:)]) {
        [self.delegate videoCallBeautyView:self didChangeEffectItemValue:self.selectItem];
    }
}

- (void)updateSlideUI {
    if (!self.selectItem || self.selectItem.subType == VideoCallEffectItemSubTypeClean) {
        [self setSliderHidden:YES];
        self.slider.value = 0;
    } else {
        [self setSliderHidden:NO];
        self.slider.value = self.selectItem.value;
    }
    [self updateSlideValueLabel];
}

- (void)setSliderHidden:(BOOL)isHidden {
    self.slider.hidden = isHidden;
    self.sliderTipLabel.hidden = isHidden;
    self.sliderValueLabel.hidden = isHidden;
}

- (void)updateSlideValueLabel {
    self.sliderValueLabel.text = [NSString stringWithFormat:@"%.0f", self.slider.value * 100];
}

#pragma mark - private
- (void)clearItemSelectedState:(VideoCallEffectItem *)item {
    for (VideoCallEffectItem *obj in item.childrens) {
        obj.selected = NO;
    }
}

- (void)clearFilterSelectedState:(VideoCallEffectItem *)currentItem {
    for (VideoCallEffectItem *item in self.itemArray[currentItem.type].childrens) {
        item.selected = NO;
        for (VideoCallEffectItem *obj in item.childrens) {
            if (obj == currentItem) {
                continue;
            } else {
                obj.selected = NO;
                obj.value = 0;
            }
        }
    }
}

- (void)clearItemValue:(VideoCallEffectItem *)item {
    if (item.childrens.count > 0) {
        for (VideoCallEffectItem *obj in item.childrens) {
            [self clearItemValue:obj];
        }
    } else {
        item.value = 0;
    }
}

#pragma mark - public
- (void)setItemArray:(NSArray<VideoCallEffectItem *> *)itemArray {
    _itemArray = itemArray;
    self.rootEffectView.itemArray = itemArray;
}

#pragma mark - getter

- (UISlider *)slider {
    if (!_slider) {
        _slider = [[UISlider alloc] init];
        _slider.minimumTrackTintColor = [UIColor colorFromHexString:@"#4080FF"];
        [_slider setThumbImage:[UIImage imageNamed:@"Effect_thumbImag" bundleName:HomeBundleName] forState:UIControlStateNormal];
        [_slider addTarget:self action:@selector(sliderValueChanged:) forControlEvents:UIControlEventValueChanged];
        _slider.hidden = YES;
    }
    return _slider;
}

- (UIView *)contentView {
    if (!_contentView) {
        _contentView = [[UIView alloc] init];
        _contentView.backgroundColor = [UIColor colorFromRGBHexString:@"#0E0825" andAlpha:0.95 * 255];
    }
    return _contentView;
}

- (UILabel *)sliderTipLabel {
    if (!_sliderTipLabel) {
        _sliderTipLabel = [[UILabel alloc] init];
        _sliderTipLabel.textColor = [UIColor colorFromHexString:@"#E5E6EB"];
        _sliderTipLabel.font = [UIFont systemFontOfSize:14];
        _sliderTipLabel.text = LocalizedString(@"strength");
        _sliderTipLabel.hidden = YES;
    }
    return _sliderTipLabel;
}

- (UILabel *)sliderValueLabel {
    if (!_sliderValueLabel) {
        _sliderValueLabel = [[UILabel alloc] init];
        _sliderValueLabel.textColor = [UIColor whiteColor];
        _sliderValueLabel.font = [UIFont systemFontOfSize:14];
        _sliderValueLabel.hidden = YES;
    }
    return _sliderValueLabel;
}

- (VideoCallEffectSubView *)rootEffectView {
    if (!_rootEffectView) {
        _rootEffectView = [[VideoCallEffectSubView alloc] initWithFrame:CGRectZero rootView:YES];
        _rootEffectView.delegate = self;
    }
    return _rootEffectView;
}


@end
