音视频通话是火山引擎实时音视频提供的一个开源示例项目。本文介绍如何快速跑通该示例项目，体验 RTC 音视频通话效果。

## 应用使用说明

使用该工程文件构建应用后，即可使用构建的应用进行音视频通话。
你和你的同事必须加入同一个房间，才能进行音视频通话。

## 前置条件

- [Xcode](https://developer.apple.com/download/all/?q=Xcode) 14.0+
	

- iOS 12.0+ 真机
	

- 有效的 [AppleID](http://appleid.apple.com/)
	

- 有效的 [火山引擎开发者账号](https://console.volcengine.com/auth/login)
	

- [CocoaPods](https://guides.cocoapods.org/using/getting-started.html#getting-started) 1.10.0+
	

## 操作步骤

### **步骤 1：获取 AppID 和 AppKey**

在火山引擎控制台->[应用管理](https://console.volcengine.com/rtc/listRTC)页面创建应用或使用已创建应用获取 AppID 和 AppAppKey

### **步骤 2：获取 AccessKeyID 和 SecretAccessKey**

在火山引擎控制台-> [密钥管理](https://console.volcengine.com/iam/keymanage/)页面获取 **AccessKeyID 和 SecretAccessKey**

### **步骤 3：构建工程**

1. 打开终端窗口，进入 `VideoCallDemo/iOS/veRTC_Demo_iOS` 根目录 <br>
	<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_ab6885a787371079c8bd5a4e554de58a.png" width="500px" >
2. 执行 `pod install` 命令构建工程 <br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_826318a7b078054b6161b878585f97ad.png" width="500px" >	
3. 进入 `VideoCallDemo/iOS/veRTC_Demo_iOS` 根目录，使用 Xcode 打开 `veRTC_Demo.xcworkspace` <br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_6a5a906a5860abc9457f26c3e141c60c.png" width="500px" >
4. 在 Xcode 中打开 `Pods/Development Pods/Core/BuildConfig.h` 文件<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_0548899b170c606d3fe53adb7c670d4a.jpeg" width="500px" >	
5. 填写 **HeadUrl**<br>
    当前你可以使用 **https://common.rtc.volcvideo.com/rtc_demo_special** 作为测试服务器域名，仅提供跑通测试服务，无法保障正式需求。<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_bfd188ba8820fd30621ba0b3d4ae57b2.jpeg" width="500px" >
6. **填写 APPID、APPKey、AccessKeyID 和 SecretAccessKey**<br>
	使用在控制台获取的 **APPID、APPKey、AccessKeyID 和 SecretAccessKey** 填写到 `BuildConfig.h`文件的对应位置。<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_6ce4ea1d8a0ddbd71c24f02c52d51239.png" width="500px" >

### **步骤 4：配置开发者证书**

1. 将手机连接到电脑，在 `iOS Device` 选项中勾选您的 iOS 设备。<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_9a93660c85d81358d76cae3b77bf3d47.png" width="500px" >
2. 登录 Apple ID。<br>
	2.1 选择 Xcode 页面左上角 **Xcode** > **Preferences**，或通过快捷键 **Command** + **,**  打开 Preferences。<br>
	2.2 选择 **Accounts**，点击左下部 **+**，选择 Apple ID 进行账号登录。<br>
    	<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_cd52826842ced1a0e34487239482dbec.png" width="500px" >
3. 配置开发者证书。	<br>
	3.1 单击 Xcode 左侧导航栏中的 `VeRTC_Demo` 项目，单击 `TARGETS` 下的 `VeRTC_Demo` 项目，选择 **Signing & Capabilities** > **Automatically manage signing** 自动生成证书<br>
    	<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_9976d548a99fd386ce388b4387716470.png" width="500px" >

	3.2 在 **Team** 中选择 Personal Team。<br>
    	<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_a5a6afb855f47cc4491527fddd58f3dc.png" width="500px" >

	3.3 **修改 Bundle Identifier。** <br>
		默认的 `vertc.veRTCDemo.ios` 已被注册， 将其修改为其他 Bundle ID，格式为 `vertc.xxx`。<br>
	    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_0e22af2e926fe73e3f376f0867a9087b.png" width="500px" >

### **步骤 5 ：编译运行**

选择 **Product** > **Run**， 开始编译。编译成功后你的 iOS 设备上会出现新应用。若为免费苹果账号，需先在`设置->通用-> VPN与设备管理 -> 描述文件与设备管理`中信任开发者 APP。<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_a08f5c5e5d31d615d0c60bddb149bc58.png" width="500px" >

## 运行开始界面
 <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_a6478b83faca8c4f285f04b2e48b6c88.jpg" width="200px" >
