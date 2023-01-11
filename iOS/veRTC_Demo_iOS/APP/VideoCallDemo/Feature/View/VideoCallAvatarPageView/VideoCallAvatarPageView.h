//
//  VideoCallAvatarPageView.h
//  VideoCallDemo
//
//  Created by on 2022/7/20.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VideoCallAvatarPageViewDelegate <NSObject>

/*
 * 多页时翻页停止后呈现的对应view以及index
 */
- (void)onShowAvatarView:(UIView *)avatarView index:(NSUInteger)index;

/*
 * 多页时翻页停止后隐藏的对应view以及index
 */
- (void)onHideAvatarView:(UIView *)avatarView index:(NSUInteger)index;

/*
 * 点击的对应view以及index
 */
- (void)onClickAvatarView:(UIView *)avatarView index:(NSUInteger)index;


/*
 * 多页时翻页停止后对应的pageIndex
 */
- (void)onScrollToPageIndex:(NSUInteger)pageIndex;

@end


@interface VideoCallAvatarPageView : UIView

@property (nonatomic, weak) id <VideoCallAvatarPageViewDelegate> avatarPageViewDelegate;

@property (nonatomic, readonly) NSUInteger currentPageIndex;

@property (nonatomic, readonly) NSUInteger currentPageCount;

@property (nonatomic, readonly) NSUInteger avatarViewCount;

@property (nonatomic, readonly) NSUInteger mainViewIndex;

/*
 * 不要通过init初始化,用initWithFrame:
 * Don't initialize via init, use initWithFrame:
 */
- (instancetype)init NS_UNAVAILABLE;

/*
 * 不要通过initWithCoder初始化,用initWithFrame:
 * Don't initialize via initWithCoder, use initWithFrame:
 */
- (instancetype)initWithCoder:(NSCoder *)coder NS_UNAVAILABLE;

/*
 * 初始化时,指定正确的frame
 * When initializing, specify the correct frame
 */
- (instancetype)initWithFrame:(CGRect)frame NS_DESIGNATED_INITIALIZER;

/*
 * 添加视频窗口
 * Add video window
 */
- (void)addAvatarView:(UIView *)avatarView;

/*
 * 根据index添加视频窗口
 * Add video window according to index
 */
- (void)addAvatarView:(UIView *)avatarView atIndex:(NSUInteger)index;

/*
 * 删除视频窗口
 * delete video window
 */
- (void)removeAvatarView:(UIView *)avatarView;

/*
 * 根据index删除视频窗口
 * Delete video window according to index
 */
- (void)removeAvatarViewAtIndex:(NSUInteger)index;

/*
 * 获取对应index的视频窗口
 * Get the video window corresponding to the index
 */
- (nullable UIView *)avatarViewAtIndex:(NSUInteger)index;

/*
 * 将对应index的视频窗口设为主窗口（只有两个视频窗口时生效）
 * Set the video window corresponding to the index as the main window (only effective when there are only two video windows)
 */
- (void)bringViewToMainAvatarViewOfIndex:(NSUInteger)index;


@end

NS_ASSUME_NONNULL_END
