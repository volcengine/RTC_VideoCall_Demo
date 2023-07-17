// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallPIPView.h"
#import <AVKit/AVKit.h>
#import "VideoCallRTCManager.h"

@interface VideoCallPIPView ()<ByteRTCVideoSinkDelegate>

@property (nonatomic, strong) UIView *contentView;
@property (nonatomic, strong) UILabel *avatarLabel;
@property (nonatomic, strong) UILabel *timeLabel;
@property (nonatomic, strong) AVSampleBufferDisplayLayer *displayLayer;
@property (nonatomic, strong) ByteRTCRemoteStreamKey *streamKey;

@property (nonatomic, strong) UIView *renderView;


@end

@implementation VideoCallPIPView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupViews];
    }
    return self;
}

- (void)setupViews {
    
    [self addSubview:self.renderView];
    [self addSubview:self.contentView];
    [self addSubview:self.timeLabel];
    [self.contentView addSubview:self.avatarLabel];
    [self.renderView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self);
    }];
    [self.contentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self);
    }];
    [self.avatarLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.equalTo(self);
        make.size.mas_equalTo(CGSizeMake(40, 40));
    }];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    self.displayLayer.frame = self.bounds;
}

#pragma mark - ByteRTCVideoSinkDelegate
- (void)renderPixelBuffer:(CVPixelBufferRef _Nonnull)pixelBuffer
                 rotation:(ByteRTCVideoRotation)rotation
                 contentType:(ByteRTCVideoContentType)contentType
             extendedData:(NSData * _Nullable)extendedData {
    
    [self dispatchPixelBuffer:pixelBuffer];
}

- (int)getRenderElapse {
    return 0;
}


//把pixelBuffer包装成samplebuffer送给displayLayer
- (void)dispatchPixelBuffer:(CVPixelBufferRef)pixelBuffer {
    if (!pixelBuffer) {
        return;
    }
    //不设置具体时间信息
    CMSampleTimingInfo timing = {kCMTimeInvalid, kCMTimeInvalid, kCMTimeInvalid};
    //获取视频信息
    CMVideoFormatDescriptionRef videoInfo = NULL;
    OSStatus result = CMVideoFormatDescriptionCreateForImageBuffer(NULL, pixelBuffer, &videoInfo);
    NSParameterAssert(result == 0 && videoInfo != NULL);
    
    CMSampleBufferRef sampleBuffer = NULL;
    result = CMSampleBufferCreateForImageBuffer(kCFAllocatorDefault,pixelBuffer, true, NULL, NULL, videoInfo, &timing, &sampleBuffer);
    NSParameterAssert(result == 0 && sampleBuffer != NULL);
    CFRelease(videoInfo);
    CFArrayRef attachments = CMSampleBufferGetSampleAttachmentsArray(sampleBuffer, YES);
    CFMutableDictionaryRef dict = (CFMutableDictionaryRef)CFArrayGetValueAtIndex(attachments, 0);
    CFDictionarySetValue(dict, kCMSampleAttachmentKey_DisplayImmediately, kCFBooleanTrue);
    [self enqueueSampleBuffer:sampleBuffer toLayer:self.displayLayer];
    CFRelease(sampleBuffer);
}

- (void)enqueueSampleBuffer:(CMSampleBufferRef)sampleBuffer toLayer:(AVSampleBufferDisplayLayer*)layer {
    if (sampleBuffer) {
        CFRetain(sampleBuffer);
        [layer enqueueSampleBuffer:sampleBuffer];
        CFRelease(sampleBuffer);
    }
}

#pragma mark - public
- (void)setIsEnableVideo:(BOOL)isEnableVideo {
    _isEnableVideo = isEnableVideo;
    self.contentView.hidden = isEnableVideo;
    if (isEnableVideo) {
        [self.timeLabel mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(self);
            make.height.mas_equalTo(21);
            make.bottom.equalTo(self).offset(-5);
        }];
    } else {
        [self.timeLabel mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(self);
            make.height.mas_equalTo(21);
            make.top.equalTo(self.avatarLabel.mas_bottom);
        }];
    }
}

- (void)updateTimeString:(NSString *)timeStr {
    self.timeLabel.text = timeStr;
}

- (void)setName:(NSString *)name {
    _name = name;
    self.avatarLabel.text = name.length > 0 ? [name substringToIndex:1] : @"";
}

- (void)startPIPWithInfoModel:(VideoCallVoipInfo *)infoModel {
    
    ByteRTCRemoteStreamKey *streamKey = [[ByteRTCRemoteStreamKey alloc] init];
    streamKey.userId = [infoModel.userId isEqualToString:infoModel.fromUserId] ? infoModel.toUserId : infoModel.fromUserId;
    streamKey.roomId = infoModel.roomId;
    streamKey.streamIndex = ByteRTCStreamIndexMain;
    self.streamKey = streamKey;
    [[VideoCallRTCManager shareRtc] setRemoteVideoSink:self.streamKey delegate:self];
}

- (void)stopPIP {
    [[VideoCallRTCManager shareRtc] setRemoteVideoSink:self.streamKey delegate:nil];
}


#pragma mark - getter
- (UIView *)contentView {
    if (!_contentView) {
        _contentView = [[UIView alloc] init];
        _contentView.backgroundColor = [UIColor colorFromHexString:@"#1E1E1E"];
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
    }
    return _timeLabel;
}

- (UIView *)renderView {
    if (!_renderView) {
        _renderView = [[UIView alloc] init];
        _displayLayer = [[AVSampleBufferDisplayLayer alloc] init];
        _displayLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
        [_renderView.layer addSublayer:self.displayLayer];
    }
    return _renderView;
}


@end
