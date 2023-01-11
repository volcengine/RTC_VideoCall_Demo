#pragma once
#include <unordered_map>
#include <vector>

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
        std::string user_id;    // user ID
        std::string user_name;  // user name
        int64_t created_at{ 0 };       // UTC/GMT join call time
        bool is_sharing{ false };          // shared screen or not
        bool is_mic_on{ false };           // microphone on or not
        bool is_camera_on{ false };        // camera on or not
        int audio_volume{ 0 };
    };

    struct VideoCallRoom {
      std::string room_id;            // room ID
      std::string screen_shared_uid;  // screen sharer ID
      int64_t duration;        // UTC/GMT, call duration time
    };

    struct StreamInfo {
        std::string user_id;
        std::string user_name;
        int width;
        int height;
        int video_fps;
        int video_kbitrate;
        int audio_kbitrate;
        int audio_delay;
        int video_delay;
        float audio_loss_rate;
        float video_loss_rate;
        int natwork_quality;
    };
}


