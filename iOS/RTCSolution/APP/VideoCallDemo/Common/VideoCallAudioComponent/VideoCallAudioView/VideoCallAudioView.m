// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallAudioView.h"

@interface VideoCallAudioView ()

@property (nonatomic, strong) UIView *contentView;
@property (nonatomic, strong) UILabel *avatarLabel;
@property (nonatomic, strong) UILabel *timeLabel;
@property (nonatomic, strong) UIImageView *animationImageView;

@end

@implementation VideoCallAudioView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = [UIColor colorFromHexString:@"#1E1E1E"];
        [self setupViews];
    }
    return self;
}

- (void)setupViews {
    [self addSubview:self.contentView];
    [self.contentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.equalTo(self);
        make.centerY.equalTo(self);
    }];
    [self.contentView addSubview:self.animationImageView];
    [self.contentView addSubview:self.avatarLabel];
    [self.contentView addSubview:self.timeLabel];
    [self.animationImageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.avatarLabel);
    }];
    [self.avatarLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.contentView);
        make.centerX.equalTo(self);
        make.size.mas_equalTo(CGSizeMake(40, 40));
    }];
    [self.timeLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.avatarLabel.mas_bottom);
        make.centerX.equalTo(self);
        make.height.mas_equalTo(21);
        make.left.right.bottom.equalTo(self.contentView);
    }];
}

- (void)updateUserName:(NSString *)userName {
    self.avatarLabel.text = (userName.length > 0) ? [userName substringToIndex:1] : @"";
}

- (void)updateText:(NSString *)text {
    self.timeLabel.text = text;
}

- (void)setAnimation:(BOOL)animation {
    if (animation) {
        [self startAnimation];
    } else {
        [self stopAnimation];
    }
}

- (void)startAnimation {
    [self layoutIfNeeded];

    CAKeyframeAnimation *animation = [CAKeyframeAnimation animationWithKeyPath:@"transform.scale"];
    animation.values = @[@1, @1.36];
    animation.duration = 1;
    animation.repeatCount = HUGE_VAL;
    animation.beginTime = 0;
    animation.removedOnCompletion = NO;
    animation.fillMode = kCAFillModeForwards;
    [self.animationImageView.layer addAnimation:animation forKey:@"animation.transform.scale"];
}

- (void)stopAnimation {
    [self.animationImageView.layer removeAllAnimations];
}

#pragma mark - getter
- (UIView *)contentView {
    if (!_contentView) {
        _contentView = [[UIView alloc] init];
        _contentView.hidden = YES;
    }
    return _contentView;
}

- (UILabel *)avatarLabel {
    if (!_avatarLabel) {
        _avatarLabel = [[UILabel alloc] init];
        _avatarLabel.backgroundColor = [UIColor colorFromHexString:@"#3E4045"];
        _avatarLabel.textColor = UIColor.whiteColor;
        _avatarLabel.font = [UIFont systemFontOfSize:16];
        _avatarLabel.layer.cornerRadius = 20;
        _avatarLabel.layer.masksToBounds = YES;
        _avatarLabel.textAlignment = NSTextAlignmentCenter;
    }
    return _avatarLabel;
}

- (UILabel *)timeLabel {
    if (!_timeLabel) {
        _timeLabel = [[UILabel alloc] init];
        _timeLabel.font = [UIFont systemFontOfSize:14];
        _timeLabel.textColor = UIColor.whiteColor;
        _timeLabel.textAlignment = NSTextAlignmentCenter;
        _timeLabel.text = LocalizedString(@"narrow_waiting_accept");
    }
    return _timeLabel;
}

- (UIImageView *)animationImageView {
    if (!_animationImageView) {
        _animationImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"avatar_animation_icon" bundleName:HomeBundleName]];
        _animationImageView.size = CGSizeMake(88, 88);
    }
    return _animationImageView;
}

@end
