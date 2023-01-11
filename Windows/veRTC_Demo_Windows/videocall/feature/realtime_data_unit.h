#pragma once

#include <QWidget>
#include "videocall/core/data_mgr.h"

namespace Ui {
    class realTimeDataUnit;
}

class realTimeDataUnit : public QWidget {
    Q_OBJECT

public:
    explicit realTimeDataUnit(QWidget* parent = nullptr);
    ~realTimeDataUnit();
    void updateInfo(const videocall::StreamInfo& info, bool isVideoInfo);

private:
    Ui::realTimeDataUnit* ui;
};
