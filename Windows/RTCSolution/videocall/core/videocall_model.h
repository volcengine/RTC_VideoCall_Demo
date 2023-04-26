#pragma once
#include <unordered_map>
#include <vector>
#include <string>

namespace videocall {
    struct VideoResolution {
        int width = 640;
        int height = 360;
    };

    enum AudioQuality {
        kAudioQualityFluent = 0,
        kAudioQualityStandard = 1,
        kAudioQualityHD = 2,
    };

    struct VideoConfiger {
        VideoResolution resolution;
        int fps = 15;
        int kbps = 64;
    };

    struct VideoCallSettingModel {
        VideoConfiger camera{ {1280, 720}, 15, 500 };
        AudioQuality audio_quality{ kAudioQualityStandard };
        bool enable_camera_mirror = true;
    };

    struct User {
        std::string user_id;
        std::string user_name;
        // 加入通话的时间
        int64_t created_at{ 0 };
        bool is_sharing{ false };
        bool is_mic_on{ false };
        bool is_camera_on{ false };
        int audio_volume{ 0 };
    };

    struct VideoCallRoom {
      std::string room_id;
      std::string screen_shared_uid;
      // 通话持续时间
      int64_t duration;
    };

    struct StreamInfo {
        std::string user_id;
        std::string user_name;
        // 分辨率的宽度值
        int width;
        // 分辨率的高度值
        int height;
        // 视频帧率
        int video_fps;
        // 视频码率
        int video_kbitrate;
        // 音频码率
        int audio_kbitrate;
        // 音频时延
        int audio_delay;
        // 视频时延
        int video_delay;
        // 音频丢包率
        float audio_loss_rate;
        // 视频丢包率
        float video_loss_rate;
        // 网络质量
        int natwork_quality;
    };
}


