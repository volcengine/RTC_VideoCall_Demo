#pragma once
#include "ui_videocall_login.h"

class VideoCallLoginWidget : public QWidget {
    Q_OBJECT
public:
    explicit VideoCallLoginWidget(QWidget* parent = nullptr);
    void setMicState(bool on);
    void setCameraState(bool on);

signals:
    void sigClose();

protected:
    void showEvent(QShowEvent*)override;
    void closeEvent(QCloseEvent*)override;

private:
    void initUi();
    void initConnections();
    void validateUserId(QString str);
    void validateRoomId(QString str);

private:
    bool login_ = false;
    bool user_name_error_{ false };
    bool room_id_error_{ false };
    Ui::VideoCallLoginWidget ui;
};
