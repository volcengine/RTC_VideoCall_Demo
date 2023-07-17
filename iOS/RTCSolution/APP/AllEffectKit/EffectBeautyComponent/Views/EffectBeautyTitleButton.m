// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "EffectBeautyTitleButton.h"

@interface EffectBeautyTitleButton ()

@property (nonatomic, strong) UIView *lineView;

@end

@implementation EffectBeautyTitleButton

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self addSubview:self.lineView];
        [self.lineView mas_makeConstraints:^(MASConstraintMaker *make) {
          make.left.right.equalTo(self.titleLabel);
          make.top.equalTo(self.titleLabel.mas_bottom);
          make.height.mas_equalTo(2);
        }];

        self.titleLabel.font = [UIFont systemFontOfSize:16];

        [self setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [self setTitleColor:[UIColor colorFromHexString:@"#4080FF"] forState:UIControlStateSelected];
    }
    return self;
}
- (void)setTitle:(NSString *)title {
    _title = title;

    [self setTitle:title forState:UIControlStateNormal];
}

- (void)setSelected:(BOOL)selected {
    [super setSelected:selected];

    if (selected) {
        self.lineView.backgroundColor = [UIColor colorFromHexString:@"#4080FF"];
    } else {
        self.lineView.backgroundColor = [UIColor clearColor];
    }
}

- (UIView *)lineView {
    if (!_lineView) {
        _lineView = [[UIView alloc] init];
    }
    return _lineView;
}

@end
