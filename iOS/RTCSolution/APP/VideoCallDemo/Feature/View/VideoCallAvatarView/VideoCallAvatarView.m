// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallAvatarView.h"

@interface VideoCallAvatarView ()

@property (nonatomic, strong) UIView *avatarBackView;
@property (nonatomic, strong) UILabel *avatarLabel;
@property (nonatomic, strong) UILabel *nameLabel;

@property (nonatomic, strong) UIImageView *animationImageView;
@property (nonatomic, strong) UIView *musicView;

@end

@implementation VideoCallAvatarView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupViews];
    }
    return self;
}

- (void)setupViews {
    [self addSubview:self.animationImageView];
    [self addSubview:self.avatarBackView];
    [self addSubview:self.avatarLabel];
    [self addSubview:self.nameLabel];
    [self addSubview:self.musicView];
    
    [self.avatarBackView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self);
        make.top.equalTo(self).offset(16);
        make.width.height.mas_equalTo(88);
    }];
    [self.avatarLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.equalTo(self.avatarBackView);
    }];
    [self.nameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self);
        make.top.equalTo(self.avatarBackView.mas_bottom).offset(24);
    }];
    [self.animationImageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.avatarBackView);
    }];
    [self.musicView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self);
        make.top.equalTo(self.nameLabel.mas_bottom).offset(8);
        make.height.mas_equalTo(20);
    }];
}

- (void)setName:(NSString *)name {
    _name = name;
    self.nameLabel.text = name;
    self.avatarLabel.text = name.length > 0 ? [name substringWithRange:NSMakeRange(0, 1)] : @"";
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
    self.avatarBackView.layer.borderWidth = 4;
    self.avatarBackView.layer.borderColor = [UIColor colorFromHexString:@"#1664FF"].CGColor;
    self.musicView.hidden = NO;
    
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
    self.musicView.hidden = YES;
    [self.animationImageView.layer removeAllAnimations];
    self.avatarBackView.layer.borderWidth = 0;
    self.avatarBackView.layer.borderColor = [UIColor clearColor].CGColor;
}

#pragma mark - getter
- (UIView *)avatarBackView {
    if (!_avatarBackView) {
        _avatarBackView = [[UIView alloc] init];
        _avatarBackView.backgroundColor = [UIColor colorFromHexString:@"#3E4045"];
        _avatarBackView.layer.cornerRadius = 44;
    }
    return _avatarBackView;
}

- (UILabel *)avatarLabel {
    if (!_avatarLabel) {
        _avatarLabel = [[UILabel alloc] init];
        _avatarLabel.font = [UIFont systemFontOfSize:36];
        _avatarLabel.textColor = UIColor.whiteColor;
    }
    return _avatarLabel;
}

- (UILabel *)nameLabel {
    if (!_nameLabel) {
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.font = [UIFont systemFontOfSize:20 weight:UIFontWeightMedium];
        _nameLabel.textColor = UIColor.whiteColor;
    }
    return _nameLabel;
}

- (UIImageView *)animationImageView {
    if (!_animationImageView) {
        _animationImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"avatar_animation_icon" bundleName:HomeBundleName]];
        _animationImageView.size = CGSizeMake(88, 88);
    }
    return _animationImageView;
}

- (UIView *)musicView {
    if (!_musicView) {
        _musicView = [[UIView alloc] init];
        _musicView.backgroundColor = [UIColor.blackColor colorWithAlphaComponent:0.64];
        _musicView.layer.cornerRadius = 10;
        _musicView.hidden = YES;
        
        UIImageView *imageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"note_icon" bundleName:HomeBundleName]];
        UILabel *label = [[UILabel alloc] init];
        label.font = [UIFont systemFontOfSize:12];
        label.textColor = UIColor.whiteColor;
        label.text = @"PeterPan Was Right";
        
        [_musicView addSubview:imageView];
        [_musicView addSubview:label];
        [imageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(_musicView).offset(7);
            make.centerY.equalTo(_musicView);
        }];
        [label mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(imageView.mas_right).offset(4);
            make.centerY.equalTo(_musicView);
            make.right.equalTo(_musicView).offset(-7);
        }];
    }
    return _musicView;
}

@end
