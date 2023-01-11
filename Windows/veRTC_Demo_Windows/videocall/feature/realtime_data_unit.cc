#include "realtime_data_unit.h"
#include "ui_realtime_data_unit.h"

static std::vector<QString> networkQualityStr{
    "未知", "优秀", "良好", "较差", "极差", "卡顿"};

realTimeDataUnit::realTimeDataUnit(QWidget* parent)
    : QWidget(parent), ui(new Ui::realTimeDataUnit) {
    ui->setupUi(this);
}

void realTimeDataUnit::updateInfo(const videocall::StreamInfo& info, bool isVideoInfo) {
    ui->userName->setText(QString::fromStdString(info.user_name));
    if (isVideoInfo) {
        ui->labelText_1->setText("分辨率");
        ui->labelValue_1->setText(QString("%1*%2").arg(info.width).arg(info.height));

        ui->labelText_2->setText("码率");
        ui->labelValue_2->setText(QString("%1").arg(info.video_kbitrate));

        ui->labelText_3->setText("帧率");
        ui->labelValue_3->setText(QString("%1").arg(info.video_fps));
        
        ui->labelText_4->setText("延时");
        ui->labelValue_4->setText(QString("%1").arg(info.video_delay));

        ui->labelText_5->setText("丢包率");
        ui->labelValue_5->setText(QString("%1").arg(info.video_loss_rate));

        ui->labelText_6->setText("网络状态");
        ui->labelValue_6->setText(networkQualityStr[info.natwork_quality]);
    }
    else {
        ui->labelText_1->setText("码率");
        ui->labelValue_1->setText(QString("%1").arg(info.audio_kbitrate));

        ui->labelText_2->setText("延时");
        ui->labelValue_2->setText(QString("%1").arg(info.audio_delay));

        ui->labelText_3->setText("丢包率");
        ui->labelValue_3->setText(QString("%1").arg(info.audio_loss_rate));

        ui->labelText_4->setText("网络状态");
        ui->labelValue_4->setText(networkQualityStr[info.natwork_quality]);

        ui->labelText_5->setText("");
        ui->labelValue_5->setText("");

        ui->labelText_6->setText("");
        ui->labelValue_6->setText("");
    }
}

realTimeDataUnit::~realTimeDataUnit() { 
    delete ui; 
}

