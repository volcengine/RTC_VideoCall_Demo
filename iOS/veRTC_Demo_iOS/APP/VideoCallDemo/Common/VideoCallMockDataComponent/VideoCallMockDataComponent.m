//
//  VideoCallMockDataComponent.m
//  quickstart
//

#import "VideoCallMockDataComponent.h"

@interface VideoCallMockDataComponent ()

@end

@implementation VideoCallMockDataComponent

+ (instancetype)shared {
    static VideoCallMockDataComponent *Component = nil;
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        Component = [[VideoCallMockDataComponent alloc] init];
    });
    return Component;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        self.currentResolutionDic = self.resLists[0];
        self.currentaudioProfileDic = self.audioProfileLists[1];
        self.isOpenMirror = YES;
    }
    return self;
}

- (NSArray *)resLists {
    if (!_resLists) {
        _resLists = @[@{@"title" : @"720*1280",
                        @"value" : @(CGSizeMake(720, 1280))},
                     
                     @{@"title" : @"540*960",
                       @"value" : @(CGSizeMake(540, 960))},
                     
                     @{@"title" : @"360*640",
                       @"value" : @(CGSizeMake(360, 640))},
                     
                     @{@"title" : @"180*320",
                       @"value" : @(CGSizeMake(180, 320))}];
    }
    return _resLists;
}

- (NSArray *)audioProfileLists {
    if (!_audioProfileLists) {
        _audioProfileLists = @[
            @{@"title" : @"流畅音质",
              @"value" : @(1)},
            
            @{@"title" : @"标准音质",
              @"value" : @(2)},
            
            @{@"title" : @"高清音质",
              @"value" : @(3)}];
    }
    return _audioProfileLists;
}

@end
