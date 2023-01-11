import { v4 as uuidv4 } from 'uuid';

interface VideoCall {
  deviceId?: string;
  login_token?: string;
}

class Utils {
  videoCallInfo: VideoCall;

  constructor() {
    const videoCallInfo: VideoCall = JSON.parse(localStorage.getItem('videoCallInfo') || '{}');
    this.videoCallInfo = videoCallInfo;
    this.init();
  }

  private init() {
    if (!this.videoCallInfo.deviceId) {
      const deviceId = uuidv4();
      this.videoCallInfo.deviceId = deviceId;
      localStorage.setItem('videoCallInfo', JSON.stringify(this.videoCallInfo));
    }
  }

  getDeviceId = (): string => this.videoCallInfo.deviceId!;

  setLoginToken = (token: string): void => {
    this.videoCallInfo.login_token = token;
  };

  getLoginToken = (): string => this.videoCallInfo.login_token!;

  formatTime = (time: number): string => {
    if (time < 0) {
      return '00:00';
    }
    let minutes: number | string = Math.floor(time / 60);
    let seconds: number | string = time % 60;
    minutes = minutes > 9 ? `${minutes}` : `0${minutes}`;
    seconds = seconds > 9 ? `${seconds}` : `0${seconds}`;

    return `${minutes}:${seconds}`;
  };
}

export default new Utils();
