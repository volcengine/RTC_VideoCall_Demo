// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "EffectBeautyView.h"
#import "EffectBeautyItemButton.h"
#import "EffectBeautyTitleButton.h"

@interface EffectBeautyView ()

@property (nonatomic, strong) UISlider *slider;
@property (nonatomic, strong) UIView *contentView;
@property (nonatomic, strong) UILabel *sliderTipLabel;
@property (nonatomic, strong) UILabel *sliderValueLabel;

@property (nonatomic, strong) UIScrollView *scrollView;

@property (nonatomic, strong) UIButton *selectedTitleButton;
@property (nonatomic, strong) EffectBeautyItemButton *selectedBeautyButton;
@property (nonatomic, strong) EffectBeautyItemButton *selectedFilterButton;
@property (nonatomic, strong) EffectBeautyItemButton *selectedStikerButton;

@property (nonatomic, copy) NSArray *modelArray;

@property (nonatomic, assign) CGFloat lastValue;
@property (nonatomic, strong) NSMutableArray<NSArray *> *itemList;

@end

@implementation EffectBeautyView

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

        NSArray *titleArray = @[ LocalizedString(@"beauty"),
                                 LocalizedString(@"filter"),
                                 LocalizedString(@"sticker") ];
        for (int i = 0; i < titleArray.count; i++) {
            EffectBeautyTitleButton *button = [[EffectBeautyTitleButton alloc] init];
            button.title = titleArray[i];
            button.tag = i + 3000;
            [button addTarget:self action:@selector(titleButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
            [self.contentView addSubview:button];
            [button mas_makeConstraints:^(MASConstraintMaker *make) {
              make.width.mas_equalTo(64);
              make.height.mas_equalTo(24);
              make.left.mas_equalTo(20 + i * 64);
              make.top.mas_equalTo(16);
            }];
            if (i == 0) {
                button.selected = YES;
                self.selectedTitleButton = button;
            }
        }

        [self.contentView addSubview:self.scrollView];
        self.scrollView.contentSize = CGSizeMake(self.width * titleArray.count, 0);
        [self.scrollView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.left.bottom.right.equalTo(self.contentView);
          make.top.mas_equalTo(54);
        }];

        NSArray *modelArray = [EffectBeautyEffectModel localModelArray];
        for (NSInteger i = modelArray.count - 1; i >= 0; i--) {
            NSArray *items = modelArray[i];
            CGFloat left = i * self.width;
            NSMutableArray *list = [[NSMutableArray alloc] init];
            
            for (int j = 0; j < items.count; j++) {
                EffectBeautyItemButton *button = [[EffectBeautyItemButton alloc] init];
                button.model = items[j];
                [button addTarget:self action:@selector(itemButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
                [self.scrollView addSubview:button];

                CGFloat margin = (self.width - 50 * 5) / 6;
                CGFloat buttonX = left + margin + (margin + 50) * j;
                [button mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.left.mas_equalTo(buttonX);
                    make.top.mas_equalTo(0);
                    make.width.mas_equalTo(50);
                    make.height.mas_equalTo(74);
                }];
                [list addObject:button];
            }
            [self.itemList insertObject:[list copy] atIndex:0];
        }
        
        [self reload];
    }
    return self;
}

#pragma mark - Publish Action

- (void)reload {
    NSArray *modelArray = [EffectBeautyEffectModel localModelArray];
    self.modelArray = modelArray;
    
    for (NSInteger i = modelArray.count - 1; i >= 0; i--) {
        NSArray *items = modelArray[i];
        NSArray *list = self.itemList[i];
        for (int j = 0; j < items.count; j++) {
            EffectBeautyEffectModel *model = items[j];
            EffectBeautyItemButton *button = list[j];
            button.model = model;

            if (i == 2 && model.selected == YES) {
                self.selectedStikerButton = button;
            }

            if (i == 1 && model.selected == YES) {
                self.selectedFilterButton = button;
            }

            if (i == 0 && model.selected == YES) {
                self.selectedBeautyButton = button;
                self.slider.value = model.value;
                [self updateSlideValueLabel];
                if (model.subType == 0) {
                    [self setSliderHidden:YES];
                } else {
                    [self setSliderHidden:NO];
                }
            }
        }
    }
    
    // 每次reload，都默认选中美颜
    EffectBeautyTitleButton *titleButton = [self.contentView viewWithTag:3000];
    [self titleButtonClicked:titleButton];
}

#pragma mark - Private Action
- (void)saveBeautyConfig {
    [EffectBeautyEffectModel saveBeautyConfig:self.modelArray];
}

- (void)titleButtonClicked:(UIButton *)sender {
    self.selectedTitleButton.selected = NO;
    sender.selected = YES;
    self.selectedTitleButton = sender;

    self.scrollView.contentOffset = CGPointMake((sender.tag - 3000) * self.scrollView.width, 0);

    switch (sender.tag) {
        case 3000: // beauty
        {
            if (self.selectedBeautyButton.model.subType != 0) {
                [self setSliderHidden:NO];
                self.slider.value = self.selectedBeautyButton.model.value;
            } else {
                [self setSliderHidden:YES];
                self.slider.value = 0;
            }
            [self updateSlideValueLabel];
        } break;
        case 3001: // filter
        {
            if (self.selectedFilterButton.model.subType != 0) {
                [self setSliderHidden:NO];
                self.slider.value = self.selectedFilterButton.model.value;
            } else {
                [self setSliderHidden:YES];
                self.slider.value = 0;
            }
            [self updateSlideValueLabel];
        } break;
        case 3002: // stiker
        {
            [self setSliderHidden:YES];
        } break;

        default:
            break;
    }
}

- (void)itemButtonClicked:(EffectBeautyItemButton *)button {
    EffectBeautyEffectModel *selectModel = button.model;

    switch (selectModel.type) {
        case 0: // beauty
        {
            if (selectModel.subType == 0) {
                [self setSliderHidden:YES];

                for (UIView *subview in self.scrollView.subviews) {
                    if (![subview isKindOfClass:[EffectBeautyItemButton class]]) {
                        continue;
                    }

                    EffectBeautyItemButton *buttonSubview = (EffectBeautyItemButton *)subview;

                    EffectBeautyEffectModel *model = buttonSubview.model;
                    if (model.valueChanged) {
                        model.value = 0;
                        model.valueChanged = NO;
                        buttonSubview.model = model;
                    }
                }
            } else {
                [self setSliderHidden:NO];
            }

            if (self.selectedBeautyButton != button) {
                EffectBeautyEffectModel *model = self.selectedBeautyButton.model;
                model.selected = NO;
                self.selectedBeautyButton.model = model;

                EffectBeautyEffectModel *selectModel = button.model;
                selectModel.selected = YES;
                button.model = selectModel;
                self.slider.value = selectModel.value;
                self.selectedBeautyButton = button;
                [self updateSlideValueLabel];
            }
        } break;
        case 1: // filter
        {
            if (selectModel.subType == 0) {
                [self setSliderHidden:YES];
            } else {
                [self setSliderHidden:NO];
            }

            if (self.selectedFilterButton != button) {
                EffectBeautyEffectModel *model = self.selectedFilterButton.model;
                model.selected = NO;
                model.value = 0;
                self.selectedFilterButton.model = model;

                EffectBeautyEffectModel *selectModel = button.model;
                selectModel.selected = YES;
                button.model = selectModel;

                self.selectedFilterButton = button;
                self.slider.value = selectModel.value;
                [self updateSlideValueLabel];
            }
        } break;
        case 2: // stiker
        {
            if (self.selectedStikerButton != button) {
                EffectBeautyEffectModel *model = self.selectedStikerButton.model;
                model.selected = NO;
                self.selectedStikerButton.model = model;

                selectModel.selected = YES;
                button.model = selectModel;
                self.selectedStikerButton = button;
            }
        } break;

        default:
            break;
    }

    if ([self.delegate respondsToSelector:@selector(effectBeautyView:didClickedEffect:)]) {
        [self.delegate effectBeautyView:self didClickedEffect:button.model];
    }
}

- (void)sliderValueChanged:(UISlider *)slider {
    NSString *currentStr = [NSString stringWithFormat:@"%.2f", slider.value];
    CGFloat currentValue = [currentStr floatValue];
    if (currentValue == self.lastValue) {
        return;
    }
    self.lastValue = currentValue;
    self.sliderValueLabel.text = [NSString stringWithFormat:@"%.0f", currentValue * 100];
    EffectBeautyEffectModel *model;
    if (self.selectedTitleButton.tag == 3000) { // beauty
        model = self.selectedBeautyButton.model;
        model.value = currentValue;
        self.selectedBeautyButton.model = model;
    } else if (self.selectedTitleButton.tag == 3001) { // filter
        model = self.selectedFilterButton.model;
        model.value = currentValue;
    }
    if ([self.delegate respondsToSelector:@selector(effectBeautyView:didChangeEffectValue:)]) {
        [self.delegate effectBeautyView:self didChangeEffectValue:model];
    }
}

- (void)setSliderHidden:(BOOL)isHidden {
    self.slider.hidden = isHidden;
    self.sliderTipLabel.hidden = isHidden;
    self.sliderValueLabel.hidden = isHidden;
}

- (void)updateSlideValueLabel {
    self.sliderValueLabel.text = [NSString stringWithFormat:@"%.0f", self.slider.value * 100];
}

#pragma mark - Getter

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

- (UIScrollView *)scrollView {
    if (!_scrollView) {
        _scrollView = [[UIScrollView alloc] init];
        _scrollView.scrollEnabled = NO;
        _scrollView.canCancelContentTouches = NO;
    }
    return _scrollView;
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

- (NSMutableArray<NSArray *> *)itemList {
    if (!_itemList) {
        _itemList = [[NSMutableArray alloc] init];
    }
    return _itemList;
}

@end
