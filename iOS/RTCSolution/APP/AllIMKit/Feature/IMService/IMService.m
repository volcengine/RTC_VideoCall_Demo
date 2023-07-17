// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "IMService.h"
#import <VolcEngineRTS/VolcEngineRTS.h>
#import "NetworkingManager.h"
#import <AFNetworking/AFNetworkReachabilityManager.h>
#import "JoinRTSParams.h"


@interface IMService ()<RTSDelegate>

@property (nonatomic, strong) NSMutableDictionary<NSString *, BaseRTSManager *> *receivers;
@property (nonatomic, strong) RTS *rts;

@property (nonatomic, assign) BOOL loginSuccessed;
@property (nonatomic, copy) void (^rtcLoginBlock)(BOOL result);
@property (nonatomic, copy) void (^rtcSetParamsBlock)(BOOL result);
@property (nonatomic, strong) JoinGlobalRTSParamsModel *rtsParamsModel;

@property (nonatomic, strong) NSMutableDictionary<NSString *, RTCInfoBlock> *blockDict;

@property (nonatomic, strong) AFNetworkReachabilityManager *reachabilityManager;
@property (nonatomic, assign) BOOL networkReachableRelogin;

@end

@implementation IMService

+ (void)initialize {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        [IMService shared];
    });
}

+ (instancetype)shared {
    static IMService *imService = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        imService = [[IMService alloc] init];
    });
    return imService;
}

- (instancetype)init {
    if (self = [super init]) {
        // 注册全局信令接收器
        [self registerGlobalReceiver];
        
        // 登录RTS服务
        [self loginRTS];
        
        // 添加网络监听
        [self addNetworkListener];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(loginSuccessNotification:) name:NotificationLoginSuccess object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(loginExpiredNotification:) name:NotificationLoginExpired object:nil];
    }
    return self;
}

#pragma mark - RTSDelegate
// 收到 RTS 登录结果
- (void)rtsEngine:(RTS *)engine onLoginResult:(NSString *)uid errorCode:(ByteRTCLoginErrorCode)errorCode elapsed:(NSInteger)elapsed {
    if (self.rtcLoginBlock) {
        self.rtcLoginBlock((errorCode == ByteRTCLoginErrorCodeSuccess) ? YES : NO);
    }
}

- (void)rtsEngineOnLogout:(RTS *)engine {
    NSLog(@"rtsEngineOnLogout");
}

// 收到业务服务器参数设置结果
- (void)rtsEngine:(RTS *)engine onServerParamsSetResult:(NSInteger)errorCode {
    if (self.rtcSetParamsBlock) {
        self.rtcSetParamsBlock((errorCode == RTSStatusCodeSuccess) ? YES : NO);
    }
}

// 发送 p2server 消息的结果回调
- (void)rtsEngine:(RTS *)engine onServerMessageSendResult:(int64_t)msgid error:(ByteRTCUserMessageSendResult)error message:(NSData *)message {
    if (error == ByteRTCUserMessageSendResultSuccess) {
        // 发送成功，等待业务回调信息
        // Successfully sent, waiting for business callback information
    } else {
        // 发送失败
        // Failed to send
        for (BaseRTSManager *rtsManager in self.receivers.allValues) {
            [rtsManager onServerMessageSendResult:msgid error:error message:message];
        }
        
        if (error == ByteRTCUserMessageSendResultNotLogin) {
            dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
                [[NSNotificationCenter defaultCenter] postNotificationName:NotificationLoginExpired object:@"logout"];
            });
        }
    }
}

// 收到远端用户发来的文本消息
- (void)rtsEngine:(RTS *)engine onMessageReceived:(NSString *)uid message:(NSString *)message {
    NSLog(@"onMessageReceived-%@", message);
    for (BaseRTSManager *rtsManager in self.receivers.allValues) {
        [rtsManager onMessageReceived:uid message:message];
    }
}

- (void)rtsEngine:(RTS *)engine onConnectionStateChanged:(ByteRTCConnectionState)state {
    NSLog(@"onConnectionStateChanged:%ld", state);
}

#pragma mark - notice
- (void)loginSuccessNotification:(NSNotification *)notification {
    [self loginRTS];
}

- (void)loginExpiredNotification:(NSNotification *)notification {
    self.loginSuccessed = NO;
    self.rtsParamsModel = nil;
    [self.rts logout];
    [RTS destroyRTS];
    self.rts = nil;
}

- (void)onReachabilityStatusChanged:(AFNetworkReachabilityStatus)status {
    if (self.networkReachableRelogin && status != AFNetworkReachabilityStatusNotReachable) {
        self.networkReachableRelogin = NO;
        [self loginRTS];
    }
}

#pragma mark - private
- (void)registerGlobalReceiver {
    // 音视频通话
    BaseRTSManager *manager = [[NSClassFromString(@"VideoCallRTSManager") alloc] init];
    [self.receivers setValue:manager forKey:IMSceneNameVideoCall];
}

- (void)loginRTS {
    __weak typeof(self) weakSelf = self;
    [self loginRTSBlock:^(BOOL result) {
        weakSelf.loginSuccessed = result;
        if (result) {
            [weakSelf connectRTSSuccessful];
        } else {
            [weakSelf reLoginRTSIfNeed];
        }
    }];
}

- (void)reLoginRTSIfNeed {
    if (IsEmptyStr([LocalUserComponent userModel].loginToken)) {
        return;
    }
    
    if (self.reachabilityManager.networkReachabilityStatus == AFNetworkReachabilityStatusNotReachable) {
        self.networkReachableRelogin = YES;
        return;
    }
    __weak typeof(self) weakSelf = self;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if (!weakSelf.loginSuccessed) {
            [weakSelf loginRTS];
        }
    });
}

- (void)loginRTSBlock:(void(^)(BOOL result))block {
    if (IsEmptyStr([LocalUserComponent userModel].loginToken)) {
        return;
    }
    
    __weak typeof(self) weakSelf = self;
    [JoinRTSParams getJoinGlobalRTSParams:[LocalUserComponent userModel].loginToken block:^(JoinGlobalRTSParamsModel * _Nullable model) {
        if (model) {
            weakSelf.rtsParamsModel = model;
            [weakSelf connectRTS:model block:block];
        } else {
            if (block) {
                block(NO);
            }
        }
    }];
}

- (void)connectRTS:(JoinGlobalRTSParamsModel *)model block:(void(^)(BOOL result))block {
    
    if (self.rts) {
        [RTS destroyRTS];
        self.rts = nil;
    }
    // 创建 RTS 引擎
    self.rts = [RTS createRTS:model.rtsAppId delegate:self parameters:nil];
    // 登录 RTS
    [self.rts login:model.rtsToken uid:model.rtsUserId];
    // 登录 RTS 结果回调
    __weak typeof(self) weakSelf = self;
    self.rtcLoginBlock = ^(BOOL result) {
        weakSelf.rtcLoginBlock = nil;
        if (result) {
            // 设置应用服务器参数
            [weakSelf.rts setServerParams:model.serverSignature url:model.serverUrl];
        } else {
            if (block) {
                block(NO);
            }
        }
    };
    // 设置应用服务器参数结果回调
    self.rtcSetParamsBlock = ^(BOOL result) {
        weakSelf.rtcSetParamsBlock = nil;
        if (block) {
            block(result);
        }
    };
}

- (void)connectRTSSuccessful {
    for (NSString *sceneName in self.blockDict.allKeys) {
        RTCInfoBlock block = self.blockDict[sceneName];
        NSDictionary *info = self.rtsParamsModel.sceneInfo[sceneName];
        NSString *appId = info[@"rtc_app_id"];
        NSString *bid = info[@"bid"];
        dispatch_async(dispatch_get_main_queue(), ^{
            if (block) {
                block(appId, bid);
            }
        });
    }
    [self.blockDict removeAllObjects];
    
    for (BaseRTSManager *manager in self.receivers.allValues) {
        [manager connectRTSSuccessful];
    }
}

- (void)addNetworkListener {
    self.reachabilityManager = [AFNetworkReachabilityManager manager];
    __weak typeof(self) weakSelf = self;
    [self.reachabilityManager setReachabilityStatusChangeBlock:^(AFNetworkReachabilityStatus status) {
        [weakSelf onReachabilityStatusChanged:status];
    }];
    [self.reachabilityManager startMonitoring];
}

#pragma mark - public
- (int64_t)sendServerMessage:(NSString *)message {
    // 向服务器发消息
    return [self.rts sendServerMessage:message];
}

- (void)getRTCAppInfo:(NSString *)sceneName block:(nonnull RTCInfoBlock)block {
    if (self.loginSuccessed) {
        NSDictionary *info = self.rtsParamsModel.sceneInfo[sceneName];
        NSString *appId = info[@"rtc_app_id"];
        NSString *bid = info[@"bid"];
        if (block) {
            block(appId, bid);
        }
    } else {
        [self.blockDict setValue:block forKey:sceneName];
    }
}

- (NSString *)getRTSAppId; {
    return self.rtsParamsModel.rtsAppId;
}

- (void)registerRTSManager:(BaseRTSManager *)manager name:(IMSceneName)name {
    [self.receivers setValue:manager forKey:name];
}

- (BaseRTSManager *)getRTSManager:(IMSceneName)name {
    return self.receivers[name];
}

- (void)unregisterRTSManager:(IMSceneName)name {
    [self.receivers removeObjectForKey:name];
}

#pragma mark - getter
- (NSMutableDictionary<NSString *, BaseRTSManager *> *)receivers {
    if (!_receivers) {
        _receivers = [NSMutableDictionary dictionary];
    }
    return _receivers;
}

- (NSMutableDictionary<NSString *,RTCInfoBlock> *)blockDict {
    if (!_blockDict) {
        _blockDict = [NSMutableDictionary dictionary];
    }
    return _blockDict;
}

@end
