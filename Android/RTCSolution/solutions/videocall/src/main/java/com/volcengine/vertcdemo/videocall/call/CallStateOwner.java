package com.volcengine.vertcdemo.videocall.call;

import com.volcengine.vertcdemo.videocall.call.state.IState;
import com.volcengine.vertcdemo.videocall.call.state.VoipState;
import com.volcengine.vertcdemo.videocall.model.VoipInfo;

public interface CallStateOwner {
    /**
     * 获取当前状态
     */
    IState getIState();

    /**
     * 获取通话相关数据
     */
    VoipInfo getVoipInfo();

    /**
     * 更新状态
     */
    void updateState(IState state,VoipInfo newVoip);

    /**
     * 创建目标状态
     */
    IState createState(VoipState state);
}
