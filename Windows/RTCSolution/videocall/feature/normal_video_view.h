#pragma once

#include "videocall/core/videocall_model.h"
#include <QWidget>

namespace Ui {
class NormalVideoView;
}

/**
* 视频渲染区域类，以网格布局排布的用户视频，一屏最多4个视频，多余4个可翻页
*/

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
