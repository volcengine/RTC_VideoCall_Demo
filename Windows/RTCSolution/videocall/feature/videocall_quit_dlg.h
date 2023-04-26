#pragma once

#include <QDialog>

namespace Ui {
class VideoCallQuitDlg;
}
/**
 * 结束通话的再次确认页面
 */
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
