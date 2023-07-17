// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallUserListView.h"
#import "VideoCallUserListTableViewCell.h"

@interface VideoCallUserListView ()<UITableViewDelegate, UITableViewDataSource, VideoCallUserListTableViewCellDelegate>

@property (nonatomic, strong) UIView *emptyView;
@property (nonatomic, strong) UITableView *tableView;

@end

@implementation VideoCallUserListView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self addSubview:self.emptyView];
        [self addSubview:self.tableView];
        [self.emptyView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self);
        }];
        [self.tableView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self);
        }];
    }
    return self;
}

#pragma mark - UITableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    VideoCallUserListTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([VideoCallUserListTableViewCell class]) forIndexPath:indexPath];
    cell.delegate = self;
    cell.data = self.dataArray[indexPath.row];
    return cell;
}

#pragma mark - VideoCallUserListTableViewCellDelegate
- (void)videoCallUserListTableViewCell:(VideoCallUserListTableViewCell *)cell didClickUser:(VideoCallUserModel *)userData {
    if ([self.delegate respondsToSelector:@selector(videoCallUserListView:didClickUser:)]) {
        [self.delegate videoCallUserListView:self didClickUser:userData];
    }
}

#pragma mark - public

- (void)setDataArray:(NSArray *)dataArray {
    _dataArray = dataArray;
    
    if (dataArray.count > 0) {
        self.emptyView.hidden = YES;
        self.tableView.hidden = NO;
        [self.tableView reloadData];
    } else {
        self.emptyView.hidden = NO;
        self.tableView.hidden = YES;
    }
}

#pragma mark - getter
- (UIView *)emptyView {
    if (!_emptyView) {
        _emptyView = [[UIView alloc] init];
        UIImageView *imageView = [[UIImageView alloc] initWithImage: [UIImage imageNamed:@"user_list_empty" bundleName:HomeBundleName]];
        UILabel *label = [[UILabel alloc] init];
        label.font = [UIFont systemFontOfSize:14];
        label.textColor = [UIColor colorFromHexString:@"#D3C6C6"];
        label.text = LocalizedString(@"user_list_empty_tip");
        [_emptyView addSubview:imageView];
        [_emptyView addSubview:label];
        [imageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(_emptyView).offset(120);
            make.centerX.equalTo(_emptyView);
        }];
        [label mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(imageView);
            make.top.equalTo(imageView.mas_bottom).offset(12);
        }];
    }
    return _emptyView;
}

- (UITableView *)tableView {
    if (!_tableView) {
        _tableView = [[UITableView alloc] init];
        _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        _tableView.backgroundColor = UIColor.clearColor;
        _tableView.rowHeight = 88;
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.keyboardDismissMode = UIScrollViewKeyboardDismissModeOnDrag;
        [_tableView registerClass:[VideoCallUserListTableViewCell class] forCellReuseIdentifier:NSStringFromClass([VideoCallUserListTableViewCell class])];
        
        _tableView.hidden = YES;
    }
    return _tableView;
}

@end
