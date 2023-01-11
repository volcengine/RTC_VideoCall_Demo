#pragma once

#include <QDialog>

namespace Ui {
class VideoCallQuitDlg;
}

class VideoCallQuitDlg : public QDialog
{
    Q_OBJECT
public:
    explicit VideoCallQuitDlg(QWidget *parent = nullptr);
    ~VideoCallQuitDlg();
    void initView();
private:
    Ui::VideoCallQuitDlg*ui;
};
