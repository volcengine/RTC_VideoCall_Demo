#pragma once
#include <QObject>
#include <QProcess>
#include "core/module_interface.h"


namespace vrd {
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
