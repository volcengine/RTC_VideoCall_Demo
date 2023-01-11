import { Button, Form, Input, message as Message } from 'antd';
import { useSelector, useDispatch } from 'react-redux';
import { MediaType, MirrorType } from '@volcengine/rtc';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Header from '@/components/Header';
import styles from './index.module.less';
import MediaButton from '@/components/MediaButton';
import { DeviceType } from '@/interface';
import { localJoinRoom, updateRoomTime } from '@/store/slices/room';
import {
  MediaName,
  medias,
  setDevicePermissions,
  updateMediaInputs,
  updateSelectedDevice,
} from '@/store/slices/device';
import { RootState } from '@/store';
import { getIcon } from '@/components/MediaButton/utils';
import RtcClient from '@/lib/RtcClient';
import { useJoinRTMMutation } from '@/app/roomQuery';
import { useFreeLogin } from './loginHook';
import useRtcListeners from '@/lib/listenerHooks';
import Utils from '@/utils/utils';
import { BusinessId } from '@/config';

export interface FormProps {
  username: string;
  roomId: string;
  userId: string;
  publishAudio: boolean;
  publishVideo: boolean;
}

export default function () {
  const localUser = useSelector((state: RootState) => state.room.localUser);
  const devicePermissions = useSelector((state: RootState) => state.device.devicePermissions);

  const [joinRTM] = useJoinRTMMutation();
  const { t } = useTranslation();
  const { freeLoginApi } = useFreeLogin();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const listeners = useRtcListeners();

  const [form] = Form.useForm();
  const publishVideo = Form.useWatch('publishVideo', form) ?? true;
  const publishAudio = Form.useWatch('publishAudio', form) ?? true;

  const [joining, setJoining] = useState(false);

  const handleStart = async (formValues: FormProps) => {
    if (joining) {
      return;
    }

    setJoining(true);

    try {
      const freeLoginRes = await freeLoginApi(formValues.username);

      if (!freeLoginRes.login_token) {
        return;
      }
      const joinRtsRes = await joinRTM({
        login_token: freeLoginRes.login_token,
        device_id: Utils.getDeviceId(),
      });

      if (!('data' in joinRtsRes)) {
        return;
      }

      const { response } = joinRtsRes.data;

      RtcClient.createEngine({
        appId: response.app_id,
        roomId: `call_${formValues.roomId}`,
        rtsUid: freeLoginRes.user_id,
        uid: freeLoginRes.user_id,
        rtmToken: response.rtm_token,
        serverUrl: response.server_url,
        serverSignature: response.server_signature,
        bid: response.bid,
      });
      RtcClient.setBusinessId(BusinessId);

      await RtcClient.joinWithRTS();
      RtcClient.addEventListeners(listeners);
      const joinRes: any = await RtcClient.sendServerMessage('videocallJoinRoom');

      if (joinRes.message_type !== 'return') {
        return;
      }

      if (joinRes.code !== 200) {
        if (joinRes.code === 406) {
          Message.error('房间人数超过限制');
        }
        console.log('rts join error: ', joinRes);
        setJoining(false);
        return;
      }

      await RtcClient.joinRoom(joinRes.response.rtc_token, formValues.username);

      const mediaDevices = await RtcClient.getDevices();

      if (devicePermissions.video && formValues.publishVideo) {
        await RtcClient.startVideoCapture();
        RtcClient.setMirrorType(MirrorType.MIRROR_TYPE_RENDER);
      }

      if (devicePermissions.audio) {
        await RtcClient.startAudioCapture();
      }

      if (!formValues.publishAudio) {
        RtcClient.unpublishStream(MediaType.AUDIO);
      }

      dispatch(updateRoomTime({ time: joinRes.response.duration }));

      dispatch(
        localJoinRoom({
          roomId: `call_${formValues.roomId}`,
          user: {
            username: formValues.username,
            userId: freeLoginRes.user_id,
            publishAudio: !!formValues.publishAudio,
            publishVideo: !!formValues.publishVideo,
          },
        })
      );

      dispatch(
        updateSelectedDevice({
          selectedCamera: mediaDevices.videoInputs[0]?.deviceId,
          selectedMicrophone: mediaDevices.audioInputs[0]?.deviceId,
        })
      );

      dispatch(updateMediaInputs(mediaDevices));

      navigate('/');
    } catch (e) {
      console.error(e);
      setJoining(false);
    }
  };

  const getMediaStatus = (media: DeviceType) => {
    switch (media) {
      case DeviceType.Camera:
        return publishVideo;
      case DeviceType.Microphone:
        return publishAudio;
      default:
        return publishAudio;
    }
  };

  const getTransKey = (media: DeviceType, enable: boolean) => {
    return `${media}${enable ? 'Enabled' : 'Disabled'}`;
  };

  useEffect(() => {
    const mount = async () => {
      const permission = await RtcClient.checkPermission();
      if (!permission.video) {
        Message.error(t('noCameraPerm'));
        form.setFieldValue('publishVideo', false);
      }

      if (!permission.audio) {
        Message.error(t('noMicPerm'));
        form.setFieldValue('publishAudio', false);
      }

      dispatch(setDevicePermissions(permission));
    };
    mount();
  }, []);

  return (
    <div className={styles.container}>
      <Header />
      <div className={styles['form-wrapper']}>
        <div className={styles.main}>
          <h1>{t('videocalls')}</h1>
          <Form form={form} onFinish={handleStart} initialValues={localUser}>
            <Form.Item
              name="roomId"
              validateTrigger="onChange"
              rules={[
                {
                  required: true,
                  validator: (_, value) => {
                    const res = /^[0-9]{1,18}$/.test(value);
                    if (!res) {
                      return Promise.reject(new Error(t('roomIdError')));
                    }
                    return Promise.resolve();
                  },
                },
              ]}
            >
              <Input placeholder={t('roomIdPlaceholder')} className={styles['login-input']} />
            </Form.Item>
            <Form.Item
              name="username"
              validateTrigger="onChange"
              rules={[
                {
                  required: true,
                  validator: (_, value) => {
                    const res = /^[a-zA-Z0-9_@\u4e00-\u9fa5]*$/.test(value);
                    if (!res) {
                      return Promise.reject(new Error(t('usernameError')));
                    }

                    if (value.length === 0) {
                      return Promise.resolve();
                    }

                    const lengthRes = /^[a-zA-Z0-9_@\u4e00-\u9fa5]{1,128}$/.test(value);
                    if (!lengthRes) {
                      return Promise.reject(new Error(t('usernameLengthError')));
                    }

                    return Promise.resolve();
                  },
                },
              ]}
            >
              <Input placeholder={t('usernamePlaceHolder')} className={styles['login-input']} />
            </Form.Item>
            <div className={styles.mediaButtons}>
              {medias.map((media) => {
                const mediaStatus = getMediaStatus(media);

                return (
                  <Form.Item
                    key={media}
                    noStyle
                    name={MediaName[media] === 'camera' ? 'publishVideo' : 'publishAudio'}
                  >
                    <MediaButton
                      text={t(getTransKey(media, mediaStatus))}
                      iconClassName={styles.mediaIcon}
                      icon={getIcon(MediaName[media], mediaStatus ? 'On' : 'Off')}
                      disabled={
                        MediaName[media] === 'camera'
                          ? !devicePermissions.video
                          : !devicePermissions.audio
                      }
                      disableMsg={
                        MediaName[media] === 'camera' ? t('noCameraPerm') : t('noMicPerm')
                      }
                    />
                  </Form.Item>
                );
              })}
            </div>

            <Form.Item
              noStyle
              shouldUpdate={(prevValues, curValues) =>
                ['roomId', 'username'].some((key) => prevValues[key] !== curValues[key])
              }
            >
              {({ getFieldValue }) => (
                <Button
                  type="primary"
                  htmlType="submit"
                  disabled={!getFieldValue('roomId') || !getFieldValue('username') || joining}
                  loading={joining}
                  className={styles.startButton}
                >
                  {t('startVideoCall')}
                </Button>
              )}
            </Form.Item>
          </Form>
        </div>
      </div>
    </div>
  );
}
