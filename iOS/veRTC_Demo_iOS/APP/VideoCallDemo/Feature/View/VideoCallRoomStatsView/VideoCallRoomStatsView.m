//
//  VideoCallRoomStatsView.m
//  VoiceChatDemo
//
//  Created by on 2022/7/26.
//

#import "VideoCallRoomStatsView.h"

@interface VideoCallRoomStatsView ()

@property (nonatomic, strong) UIButton *videoStatsButton;
@property (nonatomic, strong) UIView *videoStatsLine;
@property (nonatomic, strong) UIButton *audioStatsButton;
@property (nonatomic, strong) UIView *audioStatsLine;

@property (nonatomic, strong) UITableView *statsTableView;

@property (nonatomic, assign) NSUInteger currentSelectedIndex;

@end


@implementation VideoCallRoomStatsView

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        self.currentSelectedIndex = 0;
        
        self.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];
        
        self.videoStatsButton = [UIButton new];
        [self.videoStatsButton setTitle:@"视频实时数据" forState:UIControlStateNormal];
        [self.videoStatsButton setTitleColor:[UIColor colorFromHexString:@"#E5E6EB"] forState:UIControlStateNormal];
        [self.videoStatsButton setTitleColor:[UIColor colorFromHexString:@"#4080FF"] forState:UIControlStateSelected];
        self.videoStatsButton.titleLabel.font = [UIFont systemFontOfSize:16];
        [self.videoStatsButton addTarget:self action:@selector(onClickTitle:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:self.videoStatsButton];
        self.videoStatsButton.selected = YES;
        
        CGFloat width = frame.size.width;
        [self.videoStatsButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.top.equalTo(self);
            make.width.equalTo(@(0.5 * width));
            make.height.equalTo(@(48.f));
        }];
        
        self.videoStatsLine = [UIView new];
        self.videoStatsLine.backgroundColor = [UIColor colorFromHexString:@"#4080FF"];
        [self addSubview:self.videoStatsLine];
        [self.videoStatsLine mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(self.videoStatsButton);
            make.bottom.equalTo(self.videoStatsButton);
            make.width.equalTo(@(97.f));
            make.height.equalTo(@(2.f));
        }];
        self.videoStatsLine.hidden = NO;
        
        self.audioStatsButton = [UIButton new];
        [self.audioStatsButton setTitle:@"音频实时数据" forState:UIControlStateNormal];
        [self.audioStatsButton setTitleColor:[UIColor colorFromHexString:@"#E5E6EB"] forState:UIControlStateNormal];
        [self.audioStatsButton setTitleColor:[UIColor colorFromHexString:@"#4080FF"] forState:UIControlStateSelected];
        self.audioStatsButton.titleLabel.font = [UIFont systemFontOfSize:16];
        [self.audioStatsButton addTarget:self action:@selector(onClickTitle:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:self.audioStatsButton];
        
        [self.audioStatsButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.top.equalTo(self);
            make.width.equalTo(@(0.5 * width));
            make.height.equalTo(@(48.f));
        }];

        self.audioStatsLine = [UIView new];
        self.audioStatsLine.backgroundColor = [UIColor colorFromHexString:@"#4080FF"];
        [self addSubview:self.audioStatsLine];
        [self.audioStatsLine mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(self.audioStatsButton);
            make.bottom.equalTo(self.audioStatsButton);
            make.width.equalTo(@(97.f));
            make.height.equalTo(@(2.f));
        }];
        self.audioStatsLine.hidden = YES;

        self.statsTableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 48, frame.size.width, frame.size.height) style:UITableViewStylePlain];
        self.statsTableView.backgroundColor = [UIColor clearColor];
        [self addSubview:self.statsTableView];
        [self.statsTableView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.right.bottom.equalTo(self);
            make.top.equalTo(self).offset(48.f);
        }];
    }
    return self;
}

- (void)onClickTitle:(UIButton *)sender {
    if (sender == self.videoStatsButton) {
        self.currentSelectedIndex = 0;
        self.videoStatsButton.selected = YES;
        self.videoStatsLine.hidden = NO;
        self.audioStatsButton.selected = NO;
        self.audioStatsLine.hidden = YES;
    } else if (sender == self.audioStatsButton) {
        self.currentSelectedIndex = 1;
        self.videoStatsButton.selected = NO;
        self.videoStatsLine.hidden = YES;
        self.audioStatsButton.selected = YES;
        self.audioStatsLine.hidden = NO;
    }
    [self.statsTableView reloadData];
}


@end
