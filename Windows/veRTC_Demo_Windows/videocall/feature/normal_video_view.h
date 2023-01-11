#pragma once

#include "videocall/core/videocall_model.h"
#include <QWidget>

namespace Ui {
class NormalVideoView;
}

class NormalVideoView : public QWidget {
    Q_OBJECT
public:
    explicit NormalVideoView(QWidget* parent = nullptr);
    ~NormalVideoView();

    void showWidget(int cnt, bool forceUpdated = false);
    void showWidgetWithIndex(int firstIndex);
    void init();
protected:
    void paintEvent(QPaintEvent* event);

private:
    Ui::NormalVideoView* ui;
    int cnt_ = 0;
    int first_video_index_ = 0;
};
