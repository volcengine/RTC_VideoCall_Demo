
#import "BaseRTCManager.h"
#import <VolcEngineRTC/objc/rtc/ByteRTCDefines.h>
#import "VideoCallRTCManager.h"
#import "VideoCallRoomUserModel.h"
#import "VideoCallRoomViewController.h"
#import "VideoCallRoomParamInfoModel.h"

NS_ASSUME_NONNULL_BEGIN

@class VideoCallRTCManager;
@protocol VideoCallRTCManagerDelegate <NSObject>

- (void)rtcManager:(VideoCallRTCManager *_Nullable)rtcManager didUpdateVideoStatsInfo:(NSDictionary <NSString *, VideoCallRoomParamInfoModel *>*_Nullable)statsInfo;

- (void)rtcManager:(VideoCallRTCManager *_Nullable)rtcManager didUpdateAudioStatsInfo:(NSDictionary <NSString *, VideoCallRoomParamInfoModel *>*_Nullable)statsInfo;

- (void)rtcManager:(VideoCallRTCManager * _Nonnull)rtcManager onUserJoined:(NSString *_Nullable)uid userName:(NSString *_Nullable)name;

- (void)rtcManager:(VideoCallRTCManager * _Nonnull)rtcManager onUserLeaved:(NSString *_Nullable)uid;

- (void)rtcManager:(VideoCallRTCManager * _Nonnull)rtcManager didScreenStreamAdded:(NSString *_Nullable)screenStreamsUid;

- (void)rtcManager:(VideoCallRTCManager *_Nonnull)rtcManager didScreenStreamRemoved:(NSString *_Nullable)screenStreamsUid;

- (void)rtcManager:(VideoCallRTCManager *_Nonnull)rtcManager reportAllAudioVolume:(NSDictionary<NSString *, NSNumber *> *_Nonnull)volumeInfo;

- (void)rtcManager:(VideoCallRTCManager *_Nonnull)rtcManager onUserMuteAudio:(NSString * _Nonnull)uid isMute:(BOOL)isMute;

- (void)rtcManager:(VideoCallRTCManager *_Nonnull)rtcManager onUserMuteVideo:(NSString * _Nonnull)uid isMute:(BOOL)isMute;

- (void)rtcManager:(VideoCallRTCManager *_Nonnull)rtcManager onAudioRouteChanged:(BOOL)isHeadset;


@end

@interface VideoCallRTCManager : BaseRTCManager

@property (nonatomic, weak) id<VideoCallRTCManagerDelegate> _Nullable delegate;

/*
 * RTC Manager Singletons
 */
+ (VideoCallRTCManager *_Nullable)shareRtc;

#pragma mark - Base Method

/*
 * Join RTC room
 * @param localUserModel Current login user model
 * @param rtcToken RTC token
 */
- (void)joinChannelWithModel:(VideoCallRoomUserModel *)localUserModel
                    rtcToken:(NSString *)rtcToken;

/*
 * Real-time update of video parameters
 */
- (void)updateRtcVideoParams;

/*
 * Switch camera
 */
- (void)switchCamera;

/*
 * Switch audio routing (handset/speaker)
 * @param enableSpeaker ture:Use speakers false：Use the handset
 */
- (int)setEnableSpeakerphone:(ByteRTCAudioRoute)route;

/*
 * Control the sending status of the local audio stream: send/not send
 * @param mute ture:Not send false：Send
 */
- (void)publishAudioStream:(BOOL)mute;

/*
 * Switch local video capture
 * @param mute ture:Open video capture false：Turn off video capture
 */
- (void)enableLocalVideo:(BOOL)enable;

/*
 * Leave RTC room
 */
- (void)leaveChannel;

/*
 * Open preview
 * @param view Render view
 */
- (void)startPreview:(UIView *_Nullable)view;

/*
 * Remote screen render view and uid binding
 * @param canvas Screen canvas Model
 */
- (void)setupRemoteScreenVideo:(ByteRTCVideoCanvas *)canvas;

/*
 * Remote render view and uid binding
 * @param canvas Canvas Model
 */
- (void)setupRemoteVideo:(ByteRTCVideoCanvas *_Nullable)canvas;

/*
 * Local render view and uid binding
 * @param canvas Canvas Model
 */
- (void)setupLocalVideo:(ByteRTCVideoCanvas *_Nullable)canvas;

/*
 * Get screen rendering view based on uid
 * @param uid RTC room user id
 */
- (UIView *)getScreenStreamViewWithUid:(NSString *)uid;

@end

NS_ASSUME_NONNULL_END
