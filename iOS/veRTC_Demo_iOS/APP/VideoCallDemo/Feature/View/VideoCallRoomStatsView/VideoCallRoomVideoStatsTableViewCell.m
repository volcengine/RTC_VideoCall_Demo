//
//  VideoCallRoomStatsTableViewCell.m
//  VoiceChatDemo
//
//  Created by on 2022/7/26.
//

#import "VideoCallRoomVideoStatsTableViewCell.h"

@interface VideoCallRoomVideoStatsTableViewCell ()

@property (nonatomic, strong) UILabel *nameLabel;
@property (nonatomic, strong) UILabel *resolutionLabel;
@property (nonatomic, strong) UILabel *bitLabel;
@property (nonatomic, strong) UILabel *frameRateLabel;
@property (nonatomic, strong) UILabel *delayLabel;
@property (nonatomic, strong) UILabel *lostLabel;
@property (nonatomic, strong) UILabel *netQualityLabel;

@end

@implementation VideoCallRoomVideoStatsTableViewCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        self.selectionStyle = UITableViewCellSelectionStyleNone;
        self.backgroundColor = [UIColor clearColor];
        self.contentView.backgroundColor = [UIColor clearColor];
        
        UIView *backView = [UIView new];
        backView.backgroundColor = [UIColor colorWithWhite:0 alpha:0.15];
        [self.contentView addSubview:backView];
        
        [backView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.top.equalTo(self.contentView).offset(16.f);
            make.right.equalTo(self.contentView).offset(-16.f);
            make.bottom.equalTo(self.contentView);
        }];
        
        self.nameLabel = [UILabel new];
        self.nameLabel.textColor = [UIColor colorFromHexString:@"#86909C"];
        self.nameLabel.font = [UIFont boldSystemFontOfSize:12.f];
        [backView addSubview:self.nameLabel];
        
        [self.nameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(backView).offset(16.f);
            make.top.equalTo(backView).offset(8.f);
            make.height.equalTo(@(22.f));
        }];

        UILabel *resolutionTitle = [UILabel new];
        resolutionTitle.textColor = [UIColor colorFromHexString:@"#86909C"];
        resolutionTitle.font = [UIFont systemFontOfSize:12.f];
        resolutionTitle.text = @"分辨率";
        [backView addSubview:resolutionTitle];
        [resolutionTitle mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(backView).offset(28.f);
            make.top.equalTo(self.nameLabel.mas_bottom).offset(15.f);
            make.height.equalTo(@(22.f));
        }];

        self.resolutionLabel = [UILabel new];
        self.resolutionLabel.textColor = [UIColor colorFromHexString:@"#E5E6EB"];
        self.resolutionLabel.font = [UIFont boldSystemFontOfSize:12.f];
        [backView addSubview:self.resolutionLabel];
        [self.resolutionLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(resolutionTitle);
            make.top.equalTo(resolutionTitle.mas_bottom);
            make.height.equalTo(@(22.f));
        }];

        UILabel *bitTitle = [UILabel new];
        bitTitle.textColor = [UIColor colorFromHexString:@"#86909C"];
        bitTitle.font = [UIFont systemFontOfSize:12.f];
        bitTitle.text = @"码率 (kb/s)";
        [backView addSubview:bitTitle];
        [bitTitle mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(backView);
            make.top.equalTo(resolutionTitle);
            make.height.equalTo(@(22.f));
        }];
        
        self.bitLabel = [UILabel new];
        self.bitLabel.textColor = [UIColor colorFromHexString:@"#E5E6EB"];
        self.bitLabel.font = [UIFont boldSystemFontOfSize:12.f];
        [backView addSubview:self.bitLabel];
        [self.bitLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(bitTitle);
            make.top.equalTo(bitTitle.mas_bottom);
            make.height.equalTo(@(22.f));
        }];

        UILabel *fpsTitle = [UILabel new];
        fpsTitle.textColor = [UIColor colorFromHexString:@"#86909C"];
        fpsTitle.font = [UIFont systemFontOfSize:12.f];
        fpsTitle.text = @"帧率 (fps)";
        [backView addSubview:fpsTitle];
        [fpsTitle mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(backView).offset(-28.f);
            make.top.equalTo(resolutionTitle);
            make.height.equalTo(@(22.f));
        }];

        self.frameRateLabel = [UILabel new];
        self.frameRateLabel.textColor = [UIColor colorFromHexString:@"#E5E6EB"];
        self.frameRateLabel.font = [UIFont boldSystemFontOfSize:12.f];
        [backView addSubview:self.frameRateLabel];
        [self.frameRateLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(fpsTitle);
            make.top.equalTo(fpsTitle.mas_bottom);
            make.height.equalTo(@(22.f));
        }];

        UILabel *delayTitle = [UILabel new];
        delayTitle.textColor = [UIColor colorFromHexString:@"#86909C"];
        delayTitle.font = [UIFont systemFontOfSize:12.f];
        delayTitle.text = @"延迟 (ms)";
        [backView addSubview:delayTitle];
        [delayTitle mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(backView).offset(28.f);
            make.bottom.equalTo(backView).offset(-30.f);
            make.height.equalTo(@(22.f));
        }];

        self.delayLabel = [UILabel new];
        self.delayLabel.textColor = [UIColor colorFromHexString:@"#E5E6EB"];
        self.delayLabel.font = [UIFont boldSystemFontOfSize:12.f];
        [backView addSubview:self.delayLabel];
        [self.delayLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(delayTitle);
            make.top.equalTo(delayTitle.mas_bottom);
            make.height.equalTo(@(22.f));
        }];

        UILabel *lostTitle = [UILabel new];
        lostTitle.textColor = [UIColor colorFromHexString:@"#86909C"];
        lostTitle.font = [UIFont systemFontOfSize:12.f];
        lostTitle.text = @"丢包率 (%)";
        [backView addSubview:lostTitle];
        [lostTitle mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(backView);
            make.bottom.equalTo(backView).offset(-30.f);
            make.height.equalTo(@(22.f));
        }];

        self.lostLabel = [UILabel new];
        self.lostLabel.textColor = [UIColor colorFromHexString:@"#E5E6EB"];
        self.lostLabel.font = [UIFont boldSystemFontOfSize:12.f];
        [backView addSubview:self.lostLabel];
        [self.lostLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(lostTitle);
            make.top.equalTo(lostTitle.mas_bottom);
            make.height.equalTo(@(22.f));
        }];
        
        UILabel *netTitle = [UILabel new];
        netTitle.textColor = [UIColor colorFromHexString:@"#86909C"];
        netTitle.font = [UIFont systemFontOfSize:12.f];
        netTitle.text = @"网络状态";
        [backView addSubview:netTitle];
        [netTitle mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(backView).offset(-28.f);
            make.bottom.equalTo(backView).offset(-30.f);
            make.height.equalTo(@(22.f));
        }];

        self.netQualityLabel = [UILabel new];
        self.netQualityLabel.textColor = [UIColor colorFromHexString:@"#E5E6EB"];
        self.netQualityLabel.font = [UIFont boldSystemFontOfSize:12.f];
        [backView addSubview:self.netQualityLabel];
        [self.netQualityLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(netTitle);
            make.top.equalTo(netTitle.mas_bottom);
            make.height.equalTo(@(22.f));
        }];

    }
    return self;
}

- (void)updateUIWithModel:(VideoCallRoomParamInfoModel *)model {
    self.nameLabel.text = model.name;
    self.resolutionLabel.text = [NSString stringWithFormat:@"%ld*%ld", (long)model.width, (long)model.height];
    self.bitLabel.text = @(model.bitRate).stringValue;
    self.frameRateLabel.text = @(model.fps).stringValue;
    self.delayLabel.text = @(model.delay).stringValue;
    self.lostLabel.text = @(model.lost).stringValue;

    switch (model.netQuality) {
        case VideoCallRoomParamNetQualityGood:
            self.netQualityLabel.text = @"优秀";
            break;
        case VideoCallRoomParamNetQualityNormal:
            self.netQualityLabel.text = @"良好";
            break;
        case VideoCallRoomParamNetQualityBad:
            self.netQualityLabel.text = @"卡顿";
            break;
        default:
            break;
    }
}

@end
