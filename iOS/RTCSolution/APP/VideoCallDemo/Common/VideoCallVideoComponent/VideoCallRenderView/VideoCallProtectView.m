// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallProtectView.h"

@interface VideoCallProtectView ()

@property (nonatomic, strong) UITextField *textField;
@property (nonatomic, strong) UIView *clearView;

@end

@implementation VideoCallProtectView


- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self setupUI];
        [[NSNotificationCenter defaultCenter] removeObserver:self];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    self.textField.frame = self.bounds;
    self.clearView.frame = self.bounds;
    
    if (self.textField.superview != self) {
        [self addSubview:self.textField];
    }
}

#pragma mark - NSNotificationCenter

- (void)keyboardWillShow:(NSNotification *)noti {
    if (self.textField.isFirstResponder) {
        [self.textField resignFirstResponder];
        self.textField.subviews.firstObject.userInteractionEnabled = YES;
    }
}

#pragma mark - private

- (void)setupUI {
    [self addSubview:self.textField];
    self.textField.subviews.firstObject.userInteractionEnabled = YES;
    [self.textField.subviews.firstObject addSubview:self.clearView];
}

- (void)addSubview:(UIView *)view {
    [super addSubview:view];
    if (self.textField != view) {
        [self.clearView addSubview:view];
    }
}

#pragma mark - getter

- (UITextField *)textField {
    if (!_textField) {
        _textField = [[UITextField alloc] init];
        _textField.secureTextEntry = YES;
    }
    
    return _textField;
}

- (UIView *)clearView {
    if (!_clearView) {
        _clearView = [[UIView alloc] init];
        _clearView.backgroundColor = [UIColor clearColor];
    }
    
    return _clearView;
}

#pragma mark - dealloc

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end
