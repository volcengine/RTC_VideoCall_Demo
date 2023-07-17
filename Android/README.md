音视频通话是火山引擎实时音视频提供的一个开源示例项目。本文介绍如何快速跑通该示例项目，体验 RTC 音视频通话效果。

## 应用使用说明

使用该工程文件构建应用后，即可使用构建的应用进行音视频通话。通过向搜索框输入的用户 ID发起呼叫，创建并进入一个线上实时互动的房间，双方可以在房间内实时畅聊。
如果你已经安装过 火山引擎场景化 Demo 安装包，示例项目编译运行前请先卸载原有安装包，否则会提示安装失败。

## 环境要求

- Android Studio 推荐版本 Chipmunk
	
- Gradle 版本 gradle-7.4.2-all
	
- Android 4.4系统及以上真机
	
- 有效的 [火山引擎开发者账号](https://console.volcengine.com/auth/login)
	

## 操作步骤

### 步骤 1：获取 AppID 和 AppKey

在火山引擎控制台->[应用管理](https://console.volcengine.com/rtc/listRTC)页面创建应用或使用已创建应用获取 AppID 和 AppAppKey

### 步骤 2：获取 AccessKeyID 和 SecretAccessKey

在火山引擎控制台-> [密钥管理](https://console.volcengine.com/iam/keymanage/)页面获取 **AccessKeyID 和 SecretAccessKey**

### 步骤 3：构建工程

1. 使用 AndroidStudio 打开`VideoCallDemo/Android/RTCSolution` 目录下的Demo工程<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_6a11dd80052271778484f92194ad4c5a.png" width="500px" >

2. 填写 HEAD_URL<br>
    进入 `component/ToolKit/gradle.properties` 填写 HEAD_URL 字段。<br>
    当前你可以使用 **`https://common.rtc.volcvideo.com/rtc_demo_special`** 作为测试服务器域名，仅提供跑通测试服务，无法保障正式需求。<br>
    > 不需要加引号，例如 HEAD_URL 的值是`http://path` ，则填写后的效果为HEAD_URL=`http://path`，且末尾不要带空格。<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_be88be508cc2fb48ee83faeae50aa648.png" width="500px" >

3. 填写 APPID、APPKey、AccessKeyID、SecretAccessKey <br>
    进入 `component/JoinRTSParamsKit` 目录下 `gradle.properties`文件，填写 **APPID、APPKey、AccessKeyID、SecretAccessKey** <br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_976937bcaa9ddda941ed8e7591e270b3.png" width="500px" >


## 步骤 4：构建运行

1. 将手机连接到电脑，并在开发者选项中打开调试功能。连接成功后，设备名称会出现在界面右上方。
	
2. 选择手机，并点击运行按钮。部分手机需要在安装App时二次确认，选择确认安装<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_bedd0561653846f6141e0fb84033597c.png" width="500px" >

成功运行的开始界面如下：<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_299ced2232d7cc4b91b499695b87e38f.jpg" width="200px" >


## 使用美颜 （付费版）

该美颜方案为付费版方案，你也可以自己实现美颜功能。

1. 获取美颜相关资源，获取方式请参考文档[智能美化特效（付费版）](https://www.volcengine.com/docs/6348/114717)
	
2. 将美颜相关资源放入RTCSolution/solutions/videocall/src/main/assets/cv 文件夹下（下面图1处）。
	
3. 在美颜资源管理类EffectMaterialUtil.java中填入申请到的美颜license文件路径。
	<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_d8ff2be2a8828789816720cf74bf24ea.png" width="500px" >
