#pragma once
#include <functional>
#include <string>
#include "videocall_model.h"

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
