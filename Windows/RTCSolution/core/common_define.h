#pragma once

struct SnapshotAttr {
    // 缩略图类型
    enum SnapshotType
    {
    kUnkonw = 0,
    // 屏幕类型
    kScreen,
    // 窗口类型
    kWindow
    };
    // 缩略图名字
    std::string name;
    SnapshotType type = kUnkonw;
    // 共享源ID
    void* source_id;
    // 缩略图序号
    int index = 0;
};