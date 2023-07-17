// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallControlButton.h"

static NSString *kNormalImageName = @"kNormalImageName";
static NSString *kNormalTitle = @"kNormalTitle";
static NSString *kSelectedImageName = @"kSelectedImageName";
static NSString *kSelectedTitle = @"kSelectedTitle";


@interface VideoCallControlButton ()

@property (nonatomic, strong) UIImageView *iconImageView;
@property (nonatomic, strong) UILabel *label;

@property (nonatomic, strong) NSMutableDictionary *dict;

@end

@implementation VideoCallControlButton

- (instancetype)initWithImage:(NSString *)imageName {
    return [self initWithNormalImage:imageName normalTitle:nil selectedImage:imageName selectedTitle:nil];
}

- (instancetype)initWithNormalImage:(NSString *)normalImage normalTitle:(NSString *)normalTitle selectedImage:(NSString *)selectedImage selectedTitle:(NSString *)selectedTitle {
    if (self = [super init]) {
        [self.dict setValue:normalImage forKey:kNormalImageName];
        [self.dict setValue:normalTitle forKey:kNormalTitle];
        [self.dict setValue:selectedImage forKey:kSelectedImageName];
        [self.dict setValue:selectedTitle forKey:kSelectedTitle];
        
        [self addSubview:self.iconImageView];
        [self addSubview:self.label];
        if (normalTitle || selectedTitle) {
            [self.iconImageView mas_makeConstraints:^(MASConstraintMaker *make) {
                make.left.right.top.equalTo(self);
                make.size.mas_equalTo(CGSizeMake(64, 64));
            }];
            [self.label mas_makeConstraints:^(MASConstraintMaker *make) {
                make.top.equalTo(self.iconImageView.mas_bottom).offset(8);
                make.centerX.equalTo(self);
                make.bottom.equalTo(self);
            }];
        } else {
            [self.iconImageView mas_makeConstraints:^(MASConstraintMaker *make) {
                make.edges.equalTo(self);
                make.size.mas_equalTo(CGSizeMake(64, 64));
            }];
        }
        
        self.type = VideoCallControlButtonTypeNormal;
    }
    return self;
}

- (void)setType:(VideoCallControlButtonType)type {
    _type = type;
    
    if (type == VideoCallControlButtonTypeNormal) {
        self.iconImageView.image = [UIImage imageNamed:self.dict[kNormalImageName] bundleName:HomeBundleName];
        self.label.text = self.dict[kNormalTitle];
    } else {
        self.iconImageView.image = [UIImage imageNamed:self.dict[kSelectedImageName] bundleName:HomeBundleName];
        self.label.text = self.dict[kSelectedTitle];
    }
}

- (void)setEnabled:(BOOL)enabled {
    [super setEnabled:enabled];
    
    self.iconImageView.alpha = enabled ? 1.0 : 0.5;
}

#pragma mark - getter
- (UIImageView *)iconImageView {
    if (!_iconImageView) {
        _iconImageView = [[UIImageView alloc] init];
    }
    return _iconImageView;
}

- (UILabel *)label {
    if (!_label) {
        _label = [[UILabel alloc] init];
        _label.font = [UIFont systemFontOfSize:13];
        _label.textColor = UIColor.whiteColor;
        _label.textAlignment = NSTextAlignmentCenter;
    }
    return _label;
}

- (NSMutableDictionary *)dict {
    if (!_dict) {
        _dict = [NSMutableDictionary dictionary];
    }
    return _dict;
}

@end
