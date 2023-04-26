#pragma once
#include <functional>
#include <string>
#include "videocall_model.h"

/**
 * 通知类，用来定义从服务端回调来的通知事件的处理
 */
class VideoCallNotify {
public:
    static VideoCallNotify& instance();
    static void init();
    void onCallEnd(std::function<void(int)>&& callback);
    void offAll();

private:
    VideoCallNotify();
    ~VideoCallNotify() = default;

    void _init();

    std::function<void(int)> onCallEnd_;
};
