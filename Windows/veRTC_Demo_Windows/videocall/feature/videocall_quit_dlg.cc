#include "videocall_quit_dlg.h"
#include "ui_videocall_quit_dlg.h"

#include "videocall/core/data_mgr.h"
#include "videocall/core/videocall_session.h"


VideoCallQuitDlg::VideoCallQuitDlg(QWidget *parent)
    : QDialog(parent), ui(new Ui::VideoCallQuitDlg) {
    ui->setupUi(this);
    setWindowFlags(Qt::Dialog | Qt::FramelessWindowHint);
    connect(ui->btn_cancel, &QPushButton::clicked, this, [=] { reject(); });
    connect(ui->btn_end, &QPushButton::clicked, this, [=] {
        vrd::VideoCallSession::instance().leaveCall(
            [=](int code) {
                accept(); 
            });
        });
    initView();
}

VideoCallQuitDlg::~VideoCallQuitDlg() { delete ui; }

void VideoCallQuitDlg::initView() {
    ui->lbl_info->setText("请再次确认是否要离开房间？");
}
