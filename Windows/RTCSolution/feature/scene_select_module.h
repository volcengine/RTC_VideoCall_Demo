#pragma once
#include <QObject>
#include "core/module_interface.h"

namespace vrd {
/**
 * 更多场景选择模块，用于提示用户其他场景仍在开发中，等待上线
 * 该类中的接口不做实现
 */
class SceneSelectModule : public QObject, public IModule {
	Q_OBJECT

signals:
	void sigSceneSelectReturn();

public:
	static void addThis();

private:
	SceneSelectModule();

public:
	~SceneSelectModule();

public:
	void open() override;
	void close() override;

public:
	void quit(bool error = false);
};

}  // namespace vrd

