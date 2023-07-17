// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallSearchView.h"

// uid为10位纯数字
static NSInteger kUserIdLength = 10;

@interface VideoCallSearchView ()<UITextFieldDelegate>

@property (nonatomic, strong) UIView *contentView;
@property (nonatomic, strong) UIImageView *searchImageView;
@property (nonatomic, strong) UITextField *textField;
@property (nonatomic, strong) UIButton *searchButton;

@end

@implementation VideoCallSearchView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupViews];
    }
    return self;
}

- (void)setupViews {
    [self addSubview:self.contentView];
    [self addSubview:self.searchImageView];
    [self addSubview:self.textField];
    [self addSubview:self.searchButton];
    [self.contentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self).offset(20);
        make.right.equalTo(self).offset(-20);
        make.top.bottom.equalTo(self);
        make.height.mas_equalTo(36);
    }];
    [self.searchImageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.contentView).offset(12);
        make.centerY.equalTo(self.contentView);
        make.size.mas_equalTo(CGSizeMake(16, 16));
    }];
    [self.textField mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.bottom.equalTo(self.contentView);
        make.left.equalTo(self.contentView).offset(40);
        make.right.equalTo(self.searchButton.mas_left).offset(-5);
    }];
    [self.searchButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(self.contentView).offset(-16);
        make.centerY.equalTo(self);
        make.height.equalTo(self.contentView);
    }];
}

#pragma mark - UITextFieldDelegate

- (void)textFieldEditingChanged:(UITextField *)textField {
    
    UITextRange *selectedRange = [textField markedTextRange];
    UITextPosition *position = [textField positionFromPosition:selectedRange.start offset:0];
    if (position) {
        return;
    }
    // 没有高亮选择的字，则对已输入的文字进行字数统计和限制
    if (textField.text.length > kUserIdLength) {
        textField.text = [textField.text substringToIndex:kUserIdLength];
    }
    
    if (textField.text.length == 0) {
        if ([self.delegate respondsToSelector:@selector(videoCallSearchViewDidClearSearch:)]) {
            [self.delegate videoCallSearchViewDidClearSearch:self];
        }
    }
}

#pragma mark - action
- (void)searchButtonClick:(UIButton *)button {
    if (self.textField.text.length == 0) {
        return;
    }
    if ([self isPureInt:self.textField.text] && self.textField.text.length == kUserIdLength) {
        if ([self.delegate respondsToSelector:@selector(videoCallSearchView:didSearchUser:)]) {
            [self.delegate videoCallSearchView:self didSearchUser:self.textField.text];
        }
        [self.textField resignFirstResponder];
    } else {
        [[ToastComponent shareToastComponent] showWithMessage:LocalizedString(@"search_uid_check_error")];
    }
}

#pragma mark - private
- (BOOL)isPureInt:(NSString *)string {
    NSScanner *scanner = [[NSScanner alloc] initWithString:string];
    int val;
    return [scanner scanInt:&val] && [scanner isAtEnd];
}

#pragma mark - getter
- (UIView *)contentView {
    if (!_contentView) {
        _contentView = [[UIView alloc] init];
        _contentView.backgroundColor = [UIColor.whiteColor colorWithAlphaComponent:0.2];
        _contentView.layer.cornerRadius = 18;
    }
    return _contentView;
}

- (UIImageView *)searchImageView {
    if (!_searchImageView) {
        _searchImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"search_icon" bundleName:HomeBundleName]];
    }
    return _searchImageView;
}

- (UITextField *)textField {
    if (!_textField) {
        _textField = [[UITextField alloc] init];
        _textField.font = [UIFont systemFontOfSize:14];
        _textField.textColor = UIColor.whiteColor;
        _textField.attributedPlaceholder = [[NSAttributedString alloc] initWithString:LocalizedString(@"search_bar_placehold") attributes:@{NSForegroundColorAttributeName : [UIColor.whiteColor colorWithAlphaComponent:0.4]}];
        _textField.returnKeyType = UIReturnKeySearch;
        _textField.enablesReturnKeyAutomatically = YES;
        _textField.clearButtonMode = UITextFieldViewModeAlways;
        _textField.keyboardType = UIKeyboardTypeNumberPad;
        _textField.delegate = self;
        
        [_textField addTarget:self action:@selector(textFieldEditingChanged:) forControlEvents:UIControlEventEditingChanged];
    }
    return _textField;
}

- (UIButton *)searchButton {
    if (!_searchButton) {
        _searchButton = [[UIButton alloc] init];
        _searchButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_searchButton setTitleColor:UIColor.whiteColor forState:UIControlStateNormal];
        [_searchButton setTitle:LocalizedString(@"search_button_title") forState:UIControlStateNormal];
        [_searchButton addTarget:self action:@selector(searchButtonClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _searchButton;
}

@end
