#pragma once

#include <QWidget>
#include <QPoint>
#include "push_button_warp.h"

namespace Ui {
    class ShareButtonBar;
}

class ShareButtonBar : public QWidget {
    Q_OBJECT

public:
    explicit ShareButtonBar(QWidget* parent = nullptr);
    ~ShareButtonBar();
    void setMicState(bool isOn);
    void setCameraState(bool isOn);
    void setEventFilter(QWidget* w);
    void unSetEventFilter(QWidget* w);

protected:
    void mousePressEvent(QMouseEvent* e) override;
    void mouseMoveEvent(QMouseEvent* e) override;
    void paintEvent(QPaintEvent*) override;
    bool eventFilter(QObject* watched, QEvent* e) override;

private:
    void initConnections();
    void initCameraOption();
    void initMicOption();
    void initShareOption();

signals:
    void sigShareStateChanged(bool is_share);
    void sigOpenSetting();

private:
    Ui::ShareButtonBar* ui;
    QWidget* listener_ = nullptr;
    bool move_enabled_ = false;
    QPoint point_;
};
