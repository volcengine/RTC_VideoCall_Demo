// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VideoCallEffectSubView.h"
#import "EffectBeautyTitleButton.h"
#import "VideoCallCollectionViewCell.h"
#import "VideoCallFilterCell.h"

@interface VideoCallEffectSubView ()<UICollectionViewDelegate, UICollectionViewDataSource>

@property (nonatomic, assign) BOOL isRootView;

@property (nonatomic, strong) UIView *rootTopView;
@property (nonatomic, strong) UIButton *selectedTitleButton;
@property (nonatomic, strong) UIButton *resetButton;

@property (nonatomic, strong) UIView *nodeTopView;
@property (nonatomic, strong) UILabel *nodeTitleLable;
@property (nonatomic, strong) UIButton *nodeBackButton;

@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, strong) VideoCallEffectItem *showItem;

@end

@implementation VideoCallEffectSubView

- (instancetype)initWithFrame:(CGRect)frame rootView:(BOOL)rootView {
    if (self = [super initWithFrame:frame]) {
        self.isRootView = rootView;
        
        [self setupViews];
    }
    return self;
}

- (void)setupViews {
    self.backgroundColor = [[UIColor colorFromHexString:@"#0E0825F2"] colorWithAlphaComponent:0.95];
    
    UIView *topView = self.isRootView? self.rootTopView : self.nodeTopView;
    [self addSubview:topView];
    [topView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.left.right.equalTo(self);
        make.height.mas_equalTo(56);
    }];
    [self addSubview:self.collectionView];
    [self.collectionView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.equalTo(self.collectionView);
        make.top.equalTo(topView.mas_bottom);
        make.height.mas_equalTo(74);
        make.width.mas_equalTo(SCREEN_WIDTH);
    }];
}

#pragma mark - UICollectionViewDataSource
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.showItem.childrens.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    if (self.showItem.childrens.firstObject.subType == VideoCallEffectItemSubTypeFilter) {
        VideoCallFilterCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:NSStringFromClass([VideoCallFilterCell class]) forIndexPath:indexPath];
        cell.model = self.showItem.childrens[indexPath.item];
        return cell;
    }
    VideoCallCollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:NSStringFromClass([VideoCallCollectionViewCell class]) forIndexPath:indexPath];
    cell.model = self.showItem.childrens[indexPath.item];
    return cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    VideoCallEffectItem *item = self.showItem.childrens[indexPath.item];
    if ([self.delegate respondsToSelector:@selector(videoCallEffectSubView:didClickEffectItem:)]) {
        [self.delegate videoCallEffectSubView:self didClickEffectItem:item];
    }
    [collectionView reloadData];
}

#pragma mark - actions
- (void)titleButtonClicked:(UIButton *)sender {
    self.selectedTitleButton.selected = NO;
    sender.selected = YES;
    self.selectedTitleButton = sender;
    
    self.showItem = self.itemArray[sender.tag - 3000];
    [self.collectionView reloadData];
    
    if ([self.delegate respondsToSelector:@selector(videoCallEffectSubView:onSelectedEffectType:)]) {
        [self.delegate videoCallEffectSubView:self onSelectedEffectType:sender.tag - 3000];
    }
}

- (void)nodeBackButtonClick {
    [self removeFromSuperview];
}

- (void)resetButtonClick {
    if ([self.delegate respondsToSelector:@selector(videoCallEffectSubViewDidClickReset:)]) {
        [self.delegate videoCallEffectSubViewDidClickReset:self];
    }
}

#pragma mark - public
- (void)setItemArray:(NSArray<VideoCallEffectItem *> *)itemArray {
    _itemArray = itemArray;
    
    [self titleButtonClicked:self.selectedTitleButton];
}

- (void)setItem:(VideoCallEffectItem *)item {
    _item = item;
    self.nodeTitleLable.text = LocalizedString(item.title);
    self.showItem = item;
    
    [self.collectionView reloadData];
}

- (void)reloadData {
    [self.collectionView reloadData];
}

#pragma mark - getter
- (UIView *)rootTopView {
    if (!_rootTopView) {
        _rootTopView = [[UIView alloc] init];
        
        NSArray *titleArray = @[ LocalizedString(@"beauty"),
                                 LocalizedString(@"beauty_shap"),
                                 LocalizedString(@"filter") ];
        for (int i = 0; i < titleArray.count; i++) {
            EffectBeautyTitleButton *button = [[EffectBeautyTitleButton alloc] init];
            button.title = titleArray[i];
            button.tag = i + 3000;
            [button addTarget:self action:@selector(titleButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
            [_rootTopView addSubview:button];
            [button mas_makeConstraints:^(MASConstraintMaker *make) {
              make.width.mas_equalTo(64);
              make.height.mas_equalTo(24);
              make.left.mas_equalTo(20 + i * 64);
              make.top.mas_equalTo(16);
            }];
            
            if (i == 0) {
                button.selected = YES;
                self.selectedTitleButton = button;
            }
        }
        [_rootTopView addSubview:self.resetButton];
        [self.resetButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.equalTo(self.selectedTitleButton);
            make.right.equalTo(_rootTopView).offset(-15);
        }];
    }
    return _rootTopView;
}

- (UIButton *)resetButton {
    if (!_resetButton) {
        _resetButton = [[UIButton alloc] init];
        _resetButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_resetButton setTitle:LocalizedString(@"beauty_reset") forState:UIControlStateNormal];
        [_resetButton setTitleColor:[UIColor colorFromHexString:@"#CCCED0"] forState:UIControlStateNormal];
        [_resetButton addTarget:self action:@selector(resetButtonClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _resetButton;
}

- (UIView *)nodeTopView {
    if (!_nodeTopView) {
        _nodeTopView = [[UIView alloc] init];
        _nodeBackButton = [[UIButton alloc] init];
        [_nodeBackButton setImage:[UIImage imageNamed:@"beauty_back" bundleName:HomeBundleName] forState:UIControlStateNormal];
        [_nodeBackButton addTarget:self action:@selector(nodeBackButtonClick) forControlEvents:UIControlEventTouchUpInside];
        _nodeTitleLable = [[UILabel alloc] init];
        _nodeTitleLable.font = [UIFont systemFontOfSize:16];
        _nodeTitleLable.textColor = [UIColor colorFromHexString:@"#E5E6EB"];
        
        [_nodeTopView addSubview:_nodeBackButton];
        [_nodeTopView addSubview:_nodeTitleLable];
        [_nodeBackButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(_nodeTopView).offset(16);
            make.centerY.equalTo(_nodeTopView);
        }];
        [_nodeTitleLable mas_makeConstraints:^(MASConstraintMaker *make) {
            make.center.equalTo(_nodeTopView);
        }];
    }
    return _nodeTopView;
}

- (UICollectionView *)collectionView {
    if (!_collectionView) {
        UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
        layout.itemSize = CGSizeMake(50, 74);
        CGFloat margin = (SCREEN_WIDTH - 50 * 5) / 6;
        layout.sectionInset = UIEdgeInsetsMake(0, margin, 0, margin);
        layout.minimumLineSpacing = margin;
        layout.minimumInteritemSpacing = margin;
        layout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
        UICollectionView *collectionView = [[UICollectionView alloc] initWithFrame:CGRectZero collectionViewLayout:layout];
        collectionView.delegate = self;
        collectionView.dataSource = self;
        collectionView.backgroundColor = UIColor.clearColor;
        collectionView.showsHorizontalScrollIndicator = NO;
        [collectionView registerClass:[VideoCallCollectionViewCell class] forCellWithReuseIdentifier:NSStringFromClass([VideoCallCollectionViewCell class])];
        [collectionView registerClass:[VideoCallFilterCell class] forCellWithReuseIdentifier:NSStringFromClass([VideoCallFilterCell class])];
        _collectionView = collectionView;
    }
    return _collectionView;
}


@end
