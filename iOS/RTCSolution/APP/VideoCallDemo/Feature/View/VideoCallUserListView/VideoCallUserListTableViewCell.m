// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallUserListTableViewCell.h"

@interface VideoCallUserListTableViewCell ()

@property (nonatomic, strong) UIView *avatarBackView;
@property (nonatomic, strong) UILabel *avatarNameLabel;
@property (nonatomic, strong) UILabel *nameLabel;
@property (nonatomic, strong) UILabel *userIDLabel;
@property (nonatomic, strong) UIButton *callButton;
@property (nonatomic, strong) UIView *lineView;

@end

@implementation VideoCallUserListTableViewCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        self.backgroundColor = UIColor.clearColor;
        self.selectionStyle = UITableViewCellSelectionStyleNone;
        [self setupViews];
    }
    return self;
}

- (void)setupViews {
    [self.contentView addSubview:self.avatarBackView];
    [self.contentView addSubview:self.avatarNameLabel];
    [self.contentView addSubview:self.nameLabel];
    [self.contentView addSubview:self.userIDLabel];
    [self.contentView addSubview:self.callButton];
    [self.contentView addSubview:self.lineView];
    
    [self.avatarBackView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.contentView).offset(20);
        make.centerY.equalTo(self.contentView);
        make.size.mas_equalTo(CGSizeMake(40, 40));
    }];
    [self.avatarNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.equalTo(self.avatarBackView);
    }];
    [self.nameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.avatarBackView.mas_right).offset(8);
        make.bottom.equalTo(self.contentView.mas_centerY).offset(-2);
        make.right.lessThanOrEqualTo(self.callButton.mas_left).offset(-20);
    }];
    [self.userIDLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.avatarBackView.mas_right).offset(8);
        make.top.equalTo(self.contentView.mas_centerY).offset(2);
    }];
    [self.callButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(self.contentView).offset(-20);
        make.centerY.equalTo(self.contentView);
        make.size.mas_equalTo(CGSizeMake(70, 28));
    }];
    [self.lineView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.equalTo(self.contentView);
        make.left.equalTo(self.avatarBackView);
        make.right.equalTo(self.callButton);
        make.height.mas_equalTo(0.5);
    }];
}

#pragma mark - action
- (void)callButtonClick {
    if ([self.delegate respondsToSelector:@selector(videoCallUserListTableViewCell:didClickUser:)]) {
        [self.delegate videoCallUserListTableViewCell:self didClickUser:self.data];
    }
}

#pragma mark - public
- (void)setData:(VideoCallUserModel *)data {
    _data = data;
    
    NSString *name = data.name;
    NSString *userID = data.uid;
    self.avatarNameLabel.text = (name.length > 0)? [name substringWithRange:NSMakeRange(0, 1)] : @"";
    self.nameLabel.text = name;
    self.userIDLabel.text = [NSString stringWithFormat:@"ID: %@", userID];
}

#pragma mark - getter
- (UIView *)avatarBackView {
    if (!_avatarBackView) {
        _avatarBackView = [[UIView alloc] init];
        _avatarBackView.backgroundColor = [UIColor colorFromHexString:@"#4E5969"];
        _avatarBackView.layer.cornerRadius = 20;
    }
    return _avatarBackView;
}

- (UILabel *)avatarNameLabel {
    if (!_avatarNameLabel) {
        _avatarNameLabel = [[UILabel alloc] init];
        _avatarNameLabel.font = [UIFont systemFontOfSize:20];
        _avatarNameLabel.textColor = UIColor.whiteColor;
    }
    return _avatarNameLabel;
}

- (UILabel *)nameLabel {
    if (!_nameLabel) {
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.font = [UIFont systemFontOfSize:16];
        _nameLabel.textColor = [UIColor colorFromHexString:@"#E5E6EB"];
    }
    return _nameLabel;
}

- (UILabel *)userIDLabel {
    if (!_userIDLabel) {
        _userIDLabel = [[UILabel alloc] init];
        _userIDLabel.font = [UIFont systemFontOfSize:12];
        _userIDLabel.textColor = [UIColor colorFromHexString:@"#86909C"];
    }
    return _userIDLabel;
}

- (UIButton *)callButton {
    if (!_callButton) {
        _callButton = [[UIButton alloc] init];
        _callButton.titleLabel.font = [UIFont systemFontOfSize:14];
        _callButton.backgroundColor = [UIColor colorFromHexString:@"#1664FF"];
        _callButton.layer.cornerRadius = 14;
        [_callButton setTitleColor:UIColor.whiteColor forState:UIControlStateNormal];
        [_callButton setTitle:LocalizedString(@"user_list_call") forState:UIControlStateNormal];
        [_callButton addTarget:self action:@selector(callButtonClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _callButton;
}

- (UIView *)lineView {
    if (!_lineView) {
        _lineView = [[UIView alloc] init];
        _lineView.backgroundColor = [UIColor.whiteColor colorWithAlphaComponent:0.2];
    }
    return _lineView;
}

@end
