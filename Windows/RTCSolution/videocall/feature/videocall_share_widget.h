#pragma once

#include <QDialog>

namespace Ui {
    class VideoCallShareWidget;
}

/**
 * 选择共享内容窗口，可选择屏幕桌面或者窗口
 */
class VideoCallShareWidget : public QDialog
{
    Q_OBJECT
public:
    explicit VideoCallShareWidget(QWidget *parent = nullptr);
    ~VideoCallShareWidget();

    void updateData();
    bool canStartSharing();

private:
    void initTranslations();

    Ui::VideoCallShareWidget *ui;
};

