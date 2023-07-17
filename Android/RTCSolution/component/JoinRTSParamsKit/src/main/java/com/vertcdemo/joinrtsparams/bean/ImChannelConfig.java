package com.vertcdemo.joinrtsparams.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class ImChannelConfig {
    @SerializedName("rts_app_id")
    public String rtsAppId;
    @SerializedName("rts_token")
    public String rtsToken;
    @SerializedName("im_user_id")
    public String imUserId;
    @SerializedName("server_url")
    public String serverUrl;
    @SerializedName("server_signature")
    public String serverSignature;
    @SerializedName("scene_infos")
    public HashMap<String, Scene> scenes;

    public static class Scene implements Parcelable {
        @SerializedName("rtc_app_id")
        public String rtcAppId;
        @SerializedName("bid")
        public String bid;

        protected Scene(Parcel in) {
            rtcAppId = in.readString();
            bid = in.readString();
        }

        public static final Creator<Scene> CREATOR = new Creator<Scene>() {
            @Override
            public Scene createFromParcel(Parcel in) {
                return new Scene(in);
            }

            @Override
            public Scene[] newArray(int size) {
                return new Scene[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(rtcAppId);
            dest.writeString(bid);
        }

        /**
         * 场景参数是否有效
         *
         * @return 返回true为有效
         */
        public boolean isValid() {
            return !TextUtils.isEmpty(rtcAppId)
                    && !TextUtils.isEmpty(bid);
        }
    }

    /**
     * 返回场景对应的配置信息
     *
     * @param sceneName 场景名
     */
    public Scene getScene(String sceneName) {
        if (scenes == null) {
            return null;
        }
        return scenes.get(sceneName);
    }


    /**
     * 全局信道参数是否有效
     *
     * @return 返回true为有效
     */
    public boolean isValid() {
        return !TextUtils.isEmpty(rtsAppId)
                && !TextUtils.isEmpty(rtsToken)
                && !TextUtils.isEmpty(imUserId)
                && !TextUtils.isEmpty(serverUrl)
                && !TextUtils.isEmpty(serverSignature);
    }
}
