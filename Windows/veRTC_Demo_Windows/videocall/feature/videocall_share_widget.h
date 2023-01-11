#pragma once

#include <QDialog>

namespace Ui {
    class VideoCallShareWidget;
}

class VideoCallShareWidget : public QDialog
{
    Q_OBJECT
public:
    explicit VideoCallShareWidget(QWidget *parent = nullptr);
    ~VideoCallShareWidget();

    void updateData();
    bool canStartSharing();

private:
    Ui::VideoCallShareWidget *ui;
};

