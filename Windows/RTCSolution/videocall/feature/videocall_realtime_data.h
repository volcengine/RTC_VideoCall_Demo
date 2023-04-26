#pragma once

#include <QDialog>
#include <QMap>
#include "videocall/core/videocall_model.h"

class realTimeDataUnit;

namespace Ui {
    class VideoCallData;
}
/**
 * 通话数据页面,用于统计通话中的每个用户的音频或视频数据
 */

class VideoCallData : public QDialog {
    Q_OBJECT

public:
    explicit VideoCallData(QWidget* parent = nullptr);
    ~VideoCallData();
    void initView();
    void updateData();
    void updateData(const std::string& uid);

public slots:
    void onConfirm();
    void onClose();
    void onCancel();

private:
    Ui::VideoCallData* ui;
    QMap<std::string, realTimeDataUnit* > m_infos;
    bool mIsVideoInfo{ true };
};
