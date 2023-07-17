// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallFilterCell.h"

@interface VideoCallFilterCell ()

@property (nonatomic, strong) UIView *bgView;

@property (nonatomic, strong) UIImageView *iconView;

@property (nonatomic, strong) UIImageView *borderView;

@property (nonatomic, strong) UILabel *label;

@end

@implementation VideoCallFilterCell


- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self.contentView addSubview:self.bgView];
        [self.bgView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.top.mas_equalTo(0);
          make.centerX.equalTo(self);
          make.width.height.mas_equalTo(50);
        }];

        [self.bgView addSubview:self.iconView];
        [self.iconView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.center.equalTo(self.bgView);
            make.width.height.mas_equalTo(50);
        }];

        [self.bgView addSubview:self.borderView];
        [self.borderView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.center.equalTo(self.bgView);
        }];

        [self.contentView addSubview:self.label];
        [self.label mas_makeConstraints:^(MASConstraintMaker *make) {
          make.centerX.equalTo(self);
          make.left.greaterThanOrEqualTo(self).offset(-8);
          make.top.equalTo(self.bgView.mas_bottom).offset(8);
        }];
    }
    return self;
}

- (void)setModel:(VideoCallEffectItem *)model {
    _model = model;
    
    self.iconView.image = [UIImage imageNamed:model.imageName bundleName:HomeBundleName];
    self.label.text = LocalizedString(model.title);

    if (model.selected) {
        self.iconView.layer.borderWidth = 1;
        self.iconView.layer.borderColor = [UIColor colorFromHexString:@"#1664FF"].CGColor;
        self.label.textColor = [UIColor whiteColor];
    } else {
        self.iconView.layer.borderWidth = 0;
        self.iconView.layer.borderColor = [UIColor clearColor].CGColor;
        self.label.textColor = [UIColor colorFromHexString:@"#86909C"];
    }
}


#pragma mark - getter
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
        _iconView.layer.cornerRadius = 25;
        _iconView.layer.masksToBounds = YES;
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
