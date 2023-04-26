#pragma once
#include <QObject>
#include <QProcess>
#include "core/module_interface.h"


namespace vrd {

/**
* 音视频通话场景模块，用于该场景的实例化，进入和退出
*/
class VideoCallModule : public QObject, public IModule {
	Q_OBJECT
public:
	static void addThis();

private:
	VideoCallModule();
	void turnToSceneSelectWidget();

public:
	~VideoCallModule();

public:
	void open() override;
	void close() override;

public:
	void quit(bool error = false);
};

}  // namespace vrd
