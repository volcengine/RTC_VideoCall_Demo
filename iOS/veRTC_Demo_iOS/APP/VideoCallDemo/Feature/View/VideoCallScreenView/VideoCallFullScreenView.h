//
//  VideoCallFullScreenView.h
//
//  Created by on 2022/7/21.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface VideoCallFullScreenView : UIView

@property (nonatomic, copy) void (^clickOrientationBlock)(BOOL isLandscape);

- (void)show:(NSString *)uid
    userName:(NSString *)userName
      roomId:(NSString *)roomId
       block:(void (^)(BOOL isRemove))block;

- (void)dismiss:(BOOL)isRemove;

@end

NS_ASSUME_NONNULL_END
