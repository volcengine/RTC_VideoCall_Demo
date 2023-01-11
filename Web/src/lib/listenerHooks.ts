import VERTC, {
  LocalAudioPropertiesInfo,
  RemoteAudioPropertiesInfo,
  LocalStreamStats,
  MediaType,
  onUserJoinedEvent,
  onUserLeaveEvent,
  RemoteStreamStats,
  StreamRemoveReason,
  StreamIndex,
  DeviceInfo,
} from '@volcengine/rtc';
import { useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { message as Message } from 'antd';
import { useTranslation } from 'react-i18next';

import {
  IUser,
  localLeaveRoom,
  remoteUserJoin,
  remoteUserLeave,
  startShare,
  stopShare,
  updateLocalUser,
  updateRemoteUser,
} from '@/store/slices/room';
import RtcClient, { IEventListener } from './RtcClient';

import { setMicrophoneList, setCameraList, updateSelectedDevice } from '@/store/slices/device';
import { resetConfig } from '@/store/slices/stream';

const useRtcListeners = (isDev?: boolean): IEventListener => {
  const dispatch = useDispatch();
  const { t } = useTranslation();
  const navigate = useNavigate();

  const handleUserJoin = (e: onUserJoinedEvent) => {
    const extraInfo = JSON.parse(e.userInfo.extraInfo || '{}');

    const userId = extraInfo.user_id || e.userInfo.userId;
    const username = extraInfo.user_name || e.userInfo.userId;

    dispatch(
      remoteUserJoin({
        userId,
        username,
      })
    );
  };

  const handleError = (e: { errorCode: typeof VERTC.ErrorCode.DUPLICATE_LOGIN }) => {
    const { errorCode } = e;
    if (errorCode === VERTC.ErrorCode.DUPLICATE_LOGIN) {
      console.log('踢人');
    }
  };

  const handleUserLeave = (e: onUserLeaveEvent) => {
    dispatch(remoteUserLeave(e.userInfo));
  };

  const handleUserStartVideoCapture = (e: { userId: string }) => {
    const { userId } = e;
    const payload: IUser = { userId };
    payload.publishVideo = true;
    console.log('handleUserStartVideoCapture', payload);
    dispatch(updateRemoteUser(payload));
  };
  const handleUserStopVideoCapture = (e: { userId: string }) => {
    const { userId } = e;
    const payload: IUser = { userId };
    payload.publishVideo = false;
    console.log('handleUserStopVideoCapture', payload);

    dispatch(updateRemoteUser(payload));
  };

  const handleUserPublishStream = (e: { userId: string; mediaType: MediaType }) => {
    const { userId, mediaType } = e;

    console.log('handleUserPublishStream', e);

    const payload: IUser = { userId };
    if (mediaType === MediaType.AUDIO) {
      payload.publishAudio = true;
    }

    if (mediaType === MediaType.AUDIO_AND_VIDEO) {
      payload.publishAudio = true;
    }

    dispatch(updateRemoteUser(payload));
  };

  const handleUserUnpublishStream = (e: {
    userId: string;
    mediaType: MediaType;
    reason: StreamRemoveReason;
  }) => {
    const { userId, mediaType } = e;

    const payload: IUser = { userId };
    if (mediaType === MediaType.AUDIO) {
      payload.publishAudio = false;
    }

    if (mediaType === MediaType.AUDIO_AND_VIDEO) {
      payload.publishAudio = false;
    }

    dispatch(updateRemoteUser(payload));
  };

  const handleUserPublishScreen = async (e: { userId: string; mediaType: MediaType }) => {
    const { userId } = e;
    await RtcClient.subscribeScreen(userId);
    dispatch(
      startShare({
        shareUser: userId,
      })
    );
  };

  const handleUserUnpublishScreen = async (e: {
    userId: string;
    mediaType: MediaType;
    reason: StreamRemoveReason;
  }) => {
    dispatch(stopShare());
  };

  const handleTrackEnded = async (event: { kind: string; isScreen: boolean }) => {
    const { kind, isScreen } = event;
    if (isScreen && kind === 'video') {
      RtcClient.sendServerMessage('videocallEndShareScreen');
      await RtcClient.stopScreenCapture();
      dispatch(stopShare());
    }
  };

  const handleRemoteStreamStats = (e: RemoteStreamStats) => {
    if (isDev) {
      return;
    }
    dispatch(
      updateRemoteUser({
        userId: e.userId,
        audioStats: e.audioStats,
        videoStats: e.videoStats,
      })
    );
  };

  const handleLocalStreamStats = (e: LocalStreamStats) => {
    if (isDev) {
      return;
    }
    dispatch(
      updateLocalUser({
        audioStats: e.audioStats,
        videoStats: e.videoStats,
      })
    );
  };

  const handleLocalAudioPropertiesReport = (e: LocalAudioPropertiesInfo[]) => {
    if (isDev) {
      return;
    }
    const localAudioInfo = e.find(
      (audioInfo) => audioInfo.streamIndex === StreamIndex.STREAM_INDEX_MAIN
    );
    if (localAudioInfo) {
      dispatch(
        updateLocalUser({
          audioPropertiesInfo: localAudioInfo.audioPropertiesInfo,
        })
      );
    }
  };

  const handleRemoteAudioPropertiesReport = (e: RemoteAudioPropertiesInfo[]) => {
    if (isDev) {
      return;
    }
    const remoteAudioInfo = e
      .filter((audioInfo) => audioInfo.streamKey.streamIndex === StreamIndex.STREAM_INDEX_MAIN)
      .map((audioInfo) => ({
        userId: audioInfo.streamKey.userId,
        audioPropertiesInfo: audioInfo.audioPropertiesInfo,
      }));

    if (remoteAudioInfo.length) {
      dispatch(updateRemoteUser(remoteAudioInfo));
    }
  };

  const handleVideoDeviceStateChanged = async (device: DeviceInfo) => {
    console.log('device hook VideoDeviceStateChanged', device);
    const devices = await RtcClient.getDevices();
    console.log('new devices', devices);

    let deviceId = device.mediaDeviceInfo?.deviceId;
    if (device.deviceState === 'inactive') {
      deviceId = devices.videoInputs?.[0].deviceId || '';
    }

    RtcClient.switchDevice('camera', deviceId);
    dispatch(setCameraList(devices.videoInputs));
    dispatch(
      updateSelectedDevice({
        selectedCamera: deviceId,
      })
    );
  };
  const handleAudioDeviceStateChanged = async (device: DeviceInfo) => {
    console.log('device hook AudioDeviceStateChanged', device);
    const devices = await RtcClient.getDevices();
    console.log('new devices', devices);

    if (device.mediaDeviceInfo.kind === 'audioinput') {
      let deviceId = device.mediaDeviceInfo.deviceId;
      if (device.deviceState === 'inactive') {
        deviceId = devices.audioInputs?.[0].deviceId || '';
      }
      RtcClient.switchDevice('microphone', deviceId);
      dispatch(setMicrophoneList(devices.audioInputs));

      dispatch(
        updateSelectedDevice({
          selectedMicrophone: deviceId,
        })
      );
    }

    // if (device.mediaDeviceInfo.kind === 'audiooutput') {
    //   let deviceId = device.mediaDeviceInfo.deviceId;
    //   if (device.deviceState === 'inactive') {
    //     deviceId = devices.audioOutputs?.[0].deviceId || '';
    //   }
    //   RtcClient.switchDevice('playback', deviceId);
    //   dispatch(setAudioPlayBackList(devices.audioOutputs));
    //   dispatch(setAudioPlayBack(deviceId));
    // }
  };

  const handleOnCloseRoom = (e: { userId: string; message: any }) => {
    const { userId, message } = e;
    if (userId !== 'server') {
      return;
    }

    const msgObj = JSON.parse(message || '{}');

    if (msgObj.message_type === 'inform' && msgObj.event === 'videocallOnCloseRoom') {
      if (msgObj.data.room_id) {
        Message.error(t('timeout'));

        RtcClient.leaveRoom();
        dispatch(localLeaveRoom());
        dispatch(resetConfig());
        navigate('/login');
      }
    }
  };

  const handleUserMessageReceivedOutsideRoom = (e: { userId: string; message: any }) => {
    // console.log('handleUserMessageReceivedOutsideRoom', e);
    handleOnCloseRoom(e);
  };

  const handleUserMessageReceived = (e: { userId: string; message: any }) => {
    handleOnCloseRoom(e);
  };
  const handleRoomMessageReceived = (e: { userId: string; message: any }) => {
    handleOnCloseRoom(e);
  };

  return {
    handleError,
    handleTrackEnded,
    handleUserJoin,
    handleUserLeave,
    handleUserPublishStream,
    handleUserUnpublishStream,
    handleUserStartVideoCapture,
    handleUserStopVideoCapture,
    handleUserPublishScreen,
    handleUserUnpublishScreen,
    handleRemoteStreamStats,
    handleLocalStreamStats,
    handleLocalAudioPropertiesReport,
    handleRemoteAudioPropertiesReport,
    handleVideoDeviceStateChanged,
    handleAudioDeviceStateChanged,
    handleUserMessageReceivedOutsideRoom,
    handleUserMessageReceived,
    handleRoomMessageReceived,
  };
};

export default useRtcListeners;
