#include "scene_select_widget.h"
#include "core/configer.h"
#include "feature/data_mgr.h"
#include "core/application.h"
#include "core/navigator_interface.h"
#include "core/session_base.h"
#include "input_dlg.h"
#include "core/util_tip.h"
#include "core/component/image_button.h"
#include <QDebug>

#ifdef VIDEOCALL_SCENE
#include "videocall/videocall_module.h"
#endif // VIDEOCALL_SCENE

#ifdef MORE_SCENE
#include "feature/scene_select_module.h"
#endif // MORE_SCENE

static constexpr char* kMainQss =
    "#stackedWidget {"
    "    background : #1D2129;"
    "}"

    "#txt_sdk_ver {"
    "font-family : 'Microsoft YaHei';"
    "font-size : 12px;"
    "color : #86909C "
    "}";

static constexpr char* kGrayFontMQss =
    "font-family : 'Microsoft YaHei';"
    "background : transparent;"
    "font-size : 20px;"
    "color : #86909C; ";

static constexpr char* kLightFontQss =
    "font-family : 'Microsoft YaHei';"
    "background:transparent;"
    "font-size : 20px;"
    "color : #ffffff; ";

SceneSelectWidget& SceneSelectWidget::instance() {
    static SceneSelectWidget sceneSelect;
    return sceneSelect;
}

SceneSelectWidget::SceneSelectWidget(QWidget* parent) : QWidget(parent) {
	ui.setupUi(this);
	initControls();
	initConnects();
}

SceneSelectWidget::~SceneSelectWidget() {
}

void SceneSelectWidget::initControls() {
    this->resize(QSize(1180, 700));
	this->setStyleSheet(kMainQss);
	this->setContentsMargins(0, 0, 0, 0);

     ui.txt_sdk_ver->setText(QObject::tr("app_version_vxxx").arg("13.0.0") + " / " +QObject::tr("sdk_version_vxxx").arg(RtcEngineWrap::getSDKVersion().c_str()));


	QApplication::setEffectEnabled(Qt::UI_AnimateCombo, false);
    
#ifdef VIDEOCALL_SCENE
    vrd::VideoCallModule::addThis();
    setupVideoCallSceneButton();
#endif // VIDEOCALL_SCENE

#ifdef MORE_SCENE
	vrd::SceneSelectModule::addThis();
	setupMoreSceneButton();
#endif // MORE_SCENE

	mask_widget_ = new QWidget(this);
	mask_widget_->setStyleSheet("background:rgba(0,0,0,0.5);");
	mask_widget_->hide();
}

void SceneSelectWidget::initConnects() {
    connect(ui.top_bar, &TopBarWidget::sigUserTriggered, this, [=] {
        auto dlg = new InputDlg(this);
        dlg->setName(ui.top_bar->userName());
        enableMask(true);
        if (dlg->exec() == QDialog::Accepted) {
            auto session = vrd::Application::getSingleton().getComponent(
                VRD_UTIL_GET_COMPONENT_PARAM(vrd::SessionBase));
            session->changeUserName(dlg->name().toUtf8().constData(), [=](int code) {
                if (code == 200) {
                    Configer::instance().saveData("info/user_name",
                        dlg->name().toUtf8().constData());
                    ui.top_bar->setUserName(dlg->name());
                }
                else if (code == 430) {
                    vrd::util::showToastInfo(QObject::tr("network_messsage_430").toStdString());
                }
                else if (code == 500) {
                    vrd::util::showToastInfo(QObject::tr("network_messsage_500").toStdString());
                }
                else {
                    vrd::util::showToastInfo(QObject::tr("operation_failed_message").toStdString());
                }
                dlg->deleteLater();
            });
        }
        else {
            dlg->deleteLater();
        }
        enableMask(false);
        });
    connect(ui.top_bar, &TopBarWidget::sigQuitTriggered, this,
        [=] { emit sigLogOut();
        });
}

void SceneSelectWidget::setupMoreSceneButton() {
	auto moreBtn = new ImageButton(this);
    ui.sceneSelectLayout->addWidget(moreBtn, scene_count_ / 4,
        scene_count_ < 4 ? scene_count_ : scene_count_ % 4);
    scene_count_++;

    moreBtn->layout()->removeItem(moreBtn->layout()->itemAt(1));
    moreBtn->setStyleSheet("border-radius:14px;"
        "background:#272E3B;");

	moreBtn->setText(QObject::tr("more_scenarios"));
    moreBtn->text()->setWordWrap(true);
	moreBtn->setTextQss(kGrayFontMQss);
	moreBtn->text()->setAlignment(Qt::AlignHCenter | Qt::AlignVCenter);
    moreBtn->setFixedSize(QSize(220,220));
}

void SceneSelectWidget::setupVideoCallSceneButton() {
    auto videoCallBtn = new ImageButton(this);
    ui.sceneSelectLayout->addWidget(videoCallBtn, scene_count_ / 4,
        scene_count_ < 4 ? scene_count_ : scene_count_ % 4);
    scene_count_++;
    videoCallBtn->img()->setFixedHeight(128);
    videoCallBtn->setImgQss(
        "background-image:url(:img/"
        "video_call);margin-top:10px;background-position:center;background-"
        "repeat:"
        "no-repeat;");
    videoCallBtn->setStyleSheet("border-radius:14px;"
        "background:#272E3B;");
    videoCallBtn->setTextQss(kLightFontQss);
    videoCallBtn->text()->setAlignment(Qt::AlignHCenter |
        Qt::AlignVCenter);
    videoCallBtn->setText(QObject::tr("audio_video_calls"));
    videoCallBtn->setFixedSize(QSize(220, 220));
    connect(videoCallBtn, &ImageButton::sigPressed, this,
        [=] {
            hide();
            VRD_FUNC_GET_COMPONET(vrd::INavigator)->go("videocall");
            emit enterScene("videocall");
        });
}

void SceneSelectWidget::updateUserName() {
	ui.top_bar->setUserName(
		QString::fromStdString(vrd::DataMgr::instance().user_name()));
}

void SceneSelectWidget::resizeEvent(QResizeEvent* e) {
  mask_widget_->setGeometry(ui.stackedWidget->geometry());
}

void SceneSelectWidget::closeEvent(QCloseEvent* e) { 
    VRD_FUNC_GET_COMPONET(vrd::INavigator)->quit();
    QApplication::quit(); 
    QWidget::closeEvent(e);
}

void SceneSelectWidget::enableMask(bool enabled) {
    mask_widget_->setVisible(enabled);
}

QStackedWidget* SceneSelectWidget::getMainStackWidget() {
    return ui.stackedWidget;
}
