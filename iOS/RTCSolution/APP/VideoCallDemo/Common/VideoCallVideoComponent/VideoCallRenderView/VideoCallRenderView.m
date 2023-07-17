// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallRenderView.h"

@interface VideoCallRenderView ()

@property (nonatomic, strong) UIView *renderView;
@property (nonatomic, strong) UILabel *avatarLabel;
@property (nonatomic, strong) UILabel *timeLabel;
@property (nonatomic, strong) UITapGestureRecognizer *tapGesture;

@end

@implementation VideoCallRenderView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        
        [self setupViews];
        
        self.isEnableVideo = YES;
    }
    return self;
}

- (void)setupViews {
    self.backgroundColor = [UIColor colorFromHexString:@"#1E1E1E"];
    [self addGestureRecognizer:self.tapGesture];
    [self addSubview:self.avatarLabel];
    [self addSubview:self.renderView];
    [self addSubview:self.timeLabel];
    
    [self.renderView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self);
    }];
    [self.avatarLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.equalTo(self);
        make.size.mas_equalTo(CGSizeMake(40, 40));
    }];
    [self.timeLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self);
        make.height.mas_equalTo(21);
        make.width.equalTo(self.mas_width);
        make.bottom.equalTo(self).offset(-5);
    }];
}

- (void)layoutSubviews {
    [super layoutSubviews];

    BOOL isFullScreen = (self.width == SCREEN_WIDTH);
    if (!isFullScreen) {
        self.avatarLabel.hidden = NO;
        self.tapGesture.enabled = YES;
        self.layer.cornerRadius = 4;
        self.layer.masksToBounds = YES;
    } else {
        self.avatarLabel.hidden = YES;
        self.tapGesture.enabled = NO;
        self.layer.cornerRadius = 0;
    }
    [self updateContentView];
}

- (void)updateContentView {
    
    // 设置view边框
    BOOL isFullScreen = (self.width == SCREEN_WIDTH);
    if (!isFullScreen && !self.isEnableVideo) {
        self.layer.borderWidth = 1;
        self.layer.borderColor = [UIColor colorFromHexString:@"#565A60"].CGColor;
    } else {
        self.layer.borderWidth = 0;
        self.layer.borderColor = [UIColor clearColor].CGColor;
    }
}

#pragma mark - action
- (void)tapGestureTouch:(UITapGestureRecognizer *)ges {
    if ([self.delegate respondsToSelector:@selector(videoCallRenderViewOnTouched:)]) {
        [self.delegate videoCallRenderViewOnTouched:self];
    }
}

#pragma mark - public

- (void)setIsEnableVideo:(BOOL)isEnableVideo {
    _isEnableVideo = isEnableVideo;
    self.renderView.hidden = !isEnableVideo;
    if (isEnableVideo) {
        [self.timeLabel mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(self);
            make.height.mas_equalTo(21);
            make.bottom.equalTo(self).offset(-5);
            make.width.equalTo(self.mas_width);
        }];
    } else {
        [self.timeLabel mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(self);
            make.height.mas_equalTo(21);
            make.top.equalTo(self.avatarLabel.mas_bottom);
            make.width.equalTo(self.mas_width);
        }];
    }
    [self updateContentView];
}

- (void)updateTimeLablehidden:(BOOL)isHidden {
    self.timeLabel.hidden = isHidden;
}

- (void)updateTimeString:(NSString *)timeStr {
    self.timeLabel.text = timeStr;
}

- (void)setName:(NSString *)name {
    _name = name;
    self.avatarLabel.text = name.length > 0 ? [name substringToIndex:1] : @"";
}

#pragma mark - getter
- (UIView *)renderView {
    if (!_renderView) {
        _renderView = [[UIView alloc] init];
    }
    return _renderView;
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
        _timeLabel.hidden = YES;
    }
    return _timeLabel;
}

- (UITapGestureRecognizer *)tapGesture {
    if (!_tapGesture) {
        _tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapGestureTouch:)];
    }
    return _tapGesture;
}

@end
