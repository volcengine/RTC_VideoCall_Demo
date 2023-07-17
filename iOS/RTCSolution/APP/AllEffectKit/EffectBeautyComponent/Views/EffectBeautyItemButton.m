// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "EffectBeautyItemButton.h"

@interface EffectBeautyItemButton ()

@property (nonatomic, strong) UIView *bgView;

@property (nonatomic, strong) UIImageView *iconView;

@property (nonatomic, strong) UIImageView *borderView;

@property (nonatomic, strong) UILabel *label;

@end

@implementation EffectBeautyItemButton


- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self addSubview:self.bgView];
        [self.bgView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.top.mas_equalTo(0);
          make.centerX.equalTo(self);
          make.width.height.mas_equalTo(50);
        }];

        [self.bgView addSubview:self.iconView];
        [self.iconView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.center.equalTo(self.bgView);
        }];

        [self.bgView addSubview:self.borderView];
        [self.borderView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.center.equalTo(self.bgView);
        }];

        [self addSubview:self.label];
        [self.label mas_makeConstraints:^(MASConstraintMaker *make) {
          make.centerX.equalTo(self);
          make.left.greaterThanOrEqualTo(self).offset(-8);
          make.top.equalTo(self.bgView.mas_bottom).offset(8);
        }];
    }
    return self;
}

- (void)setModel:(EffectBeautyEffectModel *)model {
    _model = model;
    self.iconView.image = [UIImage imageNamed:model.imageName bundleName:HomeBundleName];
    self.label.text = LocalizedString(model.title);
    self.selected = model.selected;

    if (model.type == 0 || model.subType == 0) {
        self.bgView.layer.cornerRadius = 50 * 0.5;
        self.bgView.layer.masksToBounds = YES;
    } else {
        [self.iconView mas_updateConstraints:^(MASConstraintMaker *make) {
          make.width.height.mas_equalTo(48);
        }];
    }

    if (model.type == 0 && model.subType != 0) {
        if (model.value > 0) {
            model.valueChanged = YES;
        } else {
            model.valueChanged = NO;
        }

        if (model.valueChanged && model.selected == NO) {
            self.iconView.image = [UIImage imageNamed:[NSString stringWithFormat:@"%@_valueChanged", model.imageName] bundleName:HomeBundleName];
        } else {
            self.iconView.image = [UIImage imageNamed:model.imageName bundleName:HomeBundleName];
        }
    }
}

- (void)setSelected:(BOOL)selected {
    [super setSelected:selected];

    if (self.model.type == 0 || self.model.subType == 0) {
        if (selected) {
            self.bgView.backgroundColor = [UIColor colorFromHexString:@"#1664FF"];
            self.label.textColor = [UIColor whiteColor];
        } else {
            self.bgView.backgroundColor = [UIColor colorFromRGBHexString:@"##FFFFFF" andAlpha:0.1 * 255];
            self.label.textColor = [UIColor colorFromHexString:@"#86909C"];
        }
    } else {
        if (selected) {
            self.borderView.hidden = NO;
            self.label.textColor = [UIColor whiteColor];
        } else {
            self.borderView.hidden = YES;
            self.label.textColor = [UIColor colorFromHexString:@"#86909C"];
        }
    }
}

#pragma mark - Getter

- (UILabel *)label {
    if (!_label) {
        _label = [[UILabel alloc] init];
        _label.font = [UIFont systemFontOfSize:12];
        _label.userInteractionEnabled = NO;
        _label.textAlignment = NSTextAlignmentCenter;
        _label.numberOfLines = 0;
    }
    return _label;
}

- (UIImageView *)iconView {
    if (!_iconView) {
        _iconView = [[UIImageView alloc] init];
        _iconView.userInteractionEnabled = NO;
    }
    return _iconView;
}

- (UIView *)bgView {
    if (!_bgView) {
        _bgView = [[UIView alloc] init];
        _bgView.userInteractionEnabled = NO;
    }
    return _bgView;
}

- (UIImageView *)borderView {
    if (!_borderView) {
        _borderView = [[UIImageView alloc] init];
        [_borderView setImage:[UIImage imageNamed:@"InteractiveLive_beauty_border" bundleName:HomeBundleName]];
        _borderView.hidden = YES;
        _borderView.userInteractionEnabled = NO;
    }
    return _borderView;
}

@end
