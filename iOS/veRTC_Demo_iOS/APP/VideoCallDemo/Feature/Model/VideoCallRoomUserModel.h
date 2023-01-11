//
//  VideoCallRoomUserModel.h
//  quickstart
//
//  Created by on 2021/4/2.
//  
//

#import "BaseUserModel.h"

@interface VideoCallRoomUserModel : BaseUserModel

@property (nonatomic, copy) NSString *roomId;
@property (nonatomic, assign) BOOL isScreen;
@property (nonatomic, assign) BOOL isEnableVideo;
@property (nonatomic, assign) BOOL isEnableAudio;

// speaker or earpiece
@property (nonatomic, assign) BOOL isSpeakers;

@end

