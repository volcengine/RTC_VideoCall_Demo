#pragma once
#include <windows.h>
#include <sstream>
#include "videocall_model.h"
#include <QString>
#include <QFont>

namespace videocall {

std::string getOsInfo();

QString elideText(const QFont& font, const QString& str, int width);


}  // namespace utils
