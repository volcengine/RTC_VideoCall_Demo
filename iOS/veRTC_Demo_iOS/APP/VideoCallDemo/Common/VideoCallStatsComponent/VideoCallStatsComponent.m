//
//  VideoCallStatsComponent.m
//  VideoCallDemo
//
//  Created by on 2022/8/3.
//

#import "VideoCallStatsComponent.h"
#import "VideoCallRoomStatsView.h"
#import "VideoCallRoomVideoStatsTableViewCell.h"
#import "VideoCallRoomAudioStatsTableViewCell.h"

@interface VideoCallStatsComponent () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, weak) UIView *superView;

@property (nonatomic, copy) NSArray <VideoCallRoomParamInfoModel *>*videoStatsInfoArray;
@property (nonatomic, copy) NSArray <VideoCallRoomParamInfoModel *>*audioStatsInfoArray;

@property (nonatomic, strong) VideoCallRoomStatsView *statsView;

@property (nonatomic, strong) UIButton *statsCloseButton;

@end

@implementation VideoCallStatsComponent

- (instancetype)initWithSuperView:(UIView *)superView {
    self = [super init];
    if (self) {
        _superView = superView;
    }
    return self;
}

- (void)setVideoStats:(NSArray <VideoCallRoomParamInfoModel *>*)videoStats {
    self.videoStatsInfoArray = videoStats;
    
    [self.statsView.statsTableView reloadData];
}

- (void)setAudioStats:(NSArray <VideoCallRoomParamInfoModel *>*)audioStats {
    self.audioStatsInfoArray = audioStats;
    
    [self.statsView.statsTableView reloadData];
}

- (void)showStatsView {
    if (!self.statsView) {
        self.statsCloseButton = [UIButton new];
        [self.statsCloseButton addTarget:self action:@selector(hideStatsView:) forControlEvents:UIControlEventTouchUpInside];
        [self.superView addSubview:self.statsCloseButton];
        [self.statsCloseButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self.superView);
        }];
        
        self.statsView = [[VideoCallRoomStatsView alloc] initWithFrame:CGRectMake(0, 200, self.superView.frame.size.width, 414.f)];
        [self.superView addSubview:self.statsView];
        
        CGFloat navHeight = 44 + [DeviceInforTool getStatusBarHight];
        [self.statsView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(self.superView).offset(navHeight + 190.f);
            make.left.bottom.right.equalTo(self.superView);
        }];
        
        self.statsView.statsTableView.delegate = self;
        self.statsView.statsTableView.dataSource = self;

    }
    self.statsCloseButton.hidden = NO;
    self.statsView.hidden = NO;
    [self.superView bringSubviewToFront:self.statsView];

}

- (void)hideStatsView:(id)sender {
    self.statsCloseButton.hidden = YES;
    self.statsView.hidden = YES;
}
#pragma mark - UITableViewDelegate

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 172.f;
}

#pragma mark - UITableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSUInteger index = self.statsView.currentSelectedIndex;
    if (index == 0) {
        return self.videoStatsInfoArray.count;
    }else {
        return self.audioStatsInfoArray.count;
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSUInteger index = self.statsView.currentSelectedIndex;
    if (index == 0) {
        VideoCallRoomVideoStatsTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"VideoStatsCell"];
        if (!cell) {
            cell = [[VideoCallRoomVideoStatsTableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"VideoStatsCell"];
        }
        [cell updateUIWithModel:[self.videoStatsInfoArray objectAtIndex:indexPath.row]];
        return cell;
    }else {
        VideoCallRoomAudioStatsTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"AudioStatsCell"];
        if (!cell) {
            cell = [[VideoCallRoomAudioStatsTableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"AudioStatsCell"];
        }
        [cell updateUIWithModel:[self.audioStatsInfoArray objectAtIndex:indexPath.row]];
        return cell;
    }
}

@end
