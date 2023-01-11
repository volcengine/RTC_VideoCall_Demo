import { Button, Modal } from 'antd';
import { useState } from 'react';
import { ExclamationOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import RtcClient from '@/lib/RtcClient';
import MediaButton from '@/components/MediaButton';
import { getIcon } from '@/components/MediaButton/utils';
import styles from './index.module.less';
import { localLeaveRoom } from '@/store/slices/room';
import { resetConfig } from '@/store/slices/stream';

function Stop() {
  const [modalVisible, setModalVisible] = useState(false);
  const { t } = useTranslation();
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const handleStop = async () => {
    RtcClient.sendServerMessage('videocallLeaveRoom');
    await RtcClient.stopAudioCapture();
    await RtcClient.stopVideoCapture();
    await RtcClient.stopScreenCapture();
    await RtcClient.leaveRoom();

    dispatch(localLeaveRoom());
    dispatch(resetConfig());
    navigate('/login');
  };

  return (
    <div className={styles.menuRight}>
      <MediaButton
        iconClassName={styles.menuCloseIcon}
        className={styles.menuButton}
        onClick={() => {
          setModalVisible(true);
        }}
        text={t('End')}
        icon={getIcon('stop')}
      />

      <Modal
        title={null}
        visible={modalVisible}
        footer={null}
        closable={false}
        transitionName=""
        maskTransitionName=""
      >
        <div className={styles.stopModal}>
          <div className="header">
            <ExclamationOutlined
              style={{
                color: '#fff',
                backgroundColor: '#FF7D00',
                borderRadius: '50%',
              }}
            />
            <span className="stopText">{t('leaveReconfirm')}</span>
          </div>
          <Button
            type="primary"
            danger
            onClick={() => {
              setModalVisible(false);
              handleStop();
            }}
          >
            {t('OK')}
          </Button>
          <Button
            onClick={() => {
              setModalVisible(false);
            }}
          >
            {t('Cancel')}
          </Button>
        </div>
      </Modal>
    </div>
  );
}

export default Stop;
