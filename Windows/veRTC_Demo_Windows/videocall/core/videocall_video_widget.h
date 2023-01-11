#pragma once
#include <QLabel>
#include <QStackedWidget>
#include <QWidget>

class VideoCallVideoWidget : public QWidget {
public:
    VideoCallVideoWidget(QWidget* parent = nullptr);
    ~VideoCallVideoWidget() = default;
    void setUserName(const QString & str);
    void setShare(bool enabled);
    void setMic(bool isOn);
    void setHasVideo(bool has_video);
    void* getWinID();
    void setVideoUpdateEnabled(bool enabled);
    void hideVideo();
    void showVideo();
    void setHighLight(bool enabled);
    QPaintEngine* paintEngine() const { return nullptr; }
    void setUserLogoSize();
private:
    QStackedWidget* stacked_widget_;

class HasVideoWidget : public QWidget {
public:
    HasVideoWidget(QWidget* parent = nullptr);
    ~HasVideoWidget() = default;
    void* getVideoWinID();
    void setVideoUpdateEnabled(bool enabled);
    void setUserName(const QString& str);
    void hideVideo();
    void showVideo();
    void setHighLight(bool enabled);
    void setShare(bool enabled);
    void setMic(bool enabled);

protected:
    void resizeEvent(QResizeEvent* e) override;
    void showEvent(QShowEvent*) override;
    void moveEvent(QMoveEvent*) override;

private:
    QWidget* info_content_;
    QWidget* video_;
    QLabel* lbl_share_logo_;
    QLabel* lbl_user_name_;
    QLabel* lbl_mic_state_;
};

class NoVideoWidget : public QWidget {
public:
    NoVideoWidget(QWidget* parent = nullptr);
    ~NoVideoWidget() = default;

    void setUserLogoSize();
    void setUserName(const QString& str);
    void setShare(bool enabled);
    void setMic(bool enabled);
    void setHighLight(bool enabled);
protected:
    void resizeEvent(QResizeEvent* e) override;
    void showEvent(QShowEvent*) override;

private:
    QLabel* lbl_user_logo_;
    QLabel* lbl_share_logo_;
    QLabel* lbl_user_name_;
    QLabel* lbl_mic_state_;
    QWidget* info_content_;
};

};
