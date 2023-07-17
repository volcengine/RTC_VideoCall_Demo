音视频通话是火山引擎实时音视频提供的一个开源示例项目。本文介绍如何快速跑通该示例项目，体验 RTC 音视频通话效果。

## 应用使用说明

使用该工程文件构建应用后，即可使用构建的应用进行音视频通话。通过向搜索框输入的用户 ID发起呼叫，创建并进入一个线上实时互动的房间，双方可以在房间内实时畅聊。 
如果你已经安装过 火山引擎场景化 Demo 安装包，示例项目编译运行前请先卸载原有安装包，否则会提示安装失败。

## **环境要求**

- [Xcode](https://developer.apple.com/download/all/?q=Xcode) 14.0+
	
- iOS 12.0+ 真机
	
- 有效的 [AppleID](http://appleid.apple.com/)
	
- 有效的 [火山引擎开发者账号](https://console.volcengine.com/auth/login)
	
- [CocoaPods](https://guides.cocoapods.org/using/getting-started.html#getting-started) 1.10.0+
	

## 操作步骤

### 步骤 1：获取 AppID 和 AppKey

在火山引擎控制台->[应用管理](https://console.volcengine.com/rtc/listRTC)页面创建应用或使用已创建应用获取 AppID 和 AppAppKey

### 步骤 2：获取 AccessKeyID 和 SecretAccessKey

在火山引擎控制台-> [密钥管理](https://console.volcengine.com/iam/keymanage/)页面获取 **AccessKeyID 和 SecretAccessKey**

### 步骤 3：构建工程

1. 打开终端窗口，进入 `VideoCallDemo/iOS/RTCSolution` 根目录<br>
	<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_ae578f95aa1a76886707ee5e65ed42ec.png" width="500px" >
	
2. 执行 `pod install --repo-update` 命令构建工程<br>
	<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_b281f5495e01c48c09073472694ed0f7.png" width="500px" >
	
3. 进入 `VideoCallDemo/iOS/RTCSolution` 根目录，使用 Xcode 打开 `RTCSolution.xcworkspace`<br>
	<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_7174ea3dd209e7946e06f8c28655668f.png" width="500px" >

4. 在 Xcode 中打开 `Pods/Development Pods/ToolKit/BuildConfig.h` 文件<br>
	<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_1f317c652c99f12fbdb15ed7c5155d8a.png" width="500px" >
	
5. 填写 HeadUrl<br>
    HeadUrl是业务服务器的域名，你可以使用 `https://common.rtc.volcvideo.com/rtc\_demo\_special` 作为测试服务器域名，仅提供跑通测试服务，体验Demo功能，无法保障正式需求。<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_441f5526c103bca2b01c600416f45060.png" width="500px" >

6. 填写 APPID、APPKey、AccessKeyID 和 SecretAccessKey<br>
	使用在控制台获取的 **APPID、APPKey、AccessKeyID 和 SecretAccessKey** 填写到 `BuildConfig.h`文件的对应位置。<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_b05145e3e477f71c81c60b0156f192af.png" width="500px" >

### 步骤 4：配置开发者证书

1. 将手机连接到电脑，在 `iOS Device` 选项中勾选您的 iOS 设备。<br>
	<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_d25c1b44ccb8fc976bd9ec7df02d5591.png" width="500px" >
	
2. 登录 Apple ID。
    2.1 选择 Xcode 页面左上角 **Xcode** > **Settings**，或通过快捷键 **Command** + **,**  打开 Settings。<br>
	2.2 选择 **Accounts**，点击左下部 **+**，选择 Apple ID 进行账号登录。<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_bed3fe09ea8fc216ed4fd8cc6eedfd1b.png" width="500px" >
	
3. 配置开发者证书。<br>
    3.1 单击 Xcode 左侧导航栏中的 `RTCSolution` 项目，单击 `TARGETS` 下的 `RTCSolution` 项目，选择 **Signing & Capabilities** > **Automatically manage signing** 自动生成证书<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_697e89c6fe3b729855051a1a148b9543.png" width="500px" >

    3.2 在 **Team** 中选择 Personal Team。<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_393032edb0c3b3a6ad6c452df96d23ef.png" width="500px" >

    3.3 **修改 Bundle** **Identifier**。<br>
    默认的 `vertc.veRTCDemo.ios` 已被注册， 将其修改为其他 Bundle ID，格式为 `vertc.xxx`。<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_c616d8784498f6dc4a10bf147a01cc24.png" width="500px" >

### **步骤 5 ：编译运行**
   选择 **Product** > **Run**， 开始编译。编译成功后你的 iOS 设备上会出现新应用。若为免费苹果账号，需先在`设置->通用-> VPN与设备管理 -> 描述文件与设备管理`中信任开发者 APP。<br>
    <img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_10b2f6dc1ec85fb06426010bbe15c5b1.png" width="500px" >
    
成功运行的开始界面如下：<br>
<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_b629a4bbc3e7bab8bd57c1c154d52599.jpg" width="200px" >

## 使用美颜（付费版）

在执行 `pod install`前完成美颜相关配置，美颜资源文件的获取请参考文档[智能美化特效（付费版）](https://www.volcengine.com/docs/6348/114717)<br>
该美颜方案为付费版方案，你也可以自己实现美颜功能。

1. 将`effect-sdk.framework`放到`RTCSolution/APP/AllEffectKit`目录下<br>
	<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_d05532548d1260d4eb7386904f114215.png" width="500px" >
	

2. 将资源文件放到`RTCSolution/APP/AllEffectKit/Resource`目录下<br>
	<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_2d70b83873592ea50fcbad590b3c94a5.png" width="500px" >


3. 打开`RTCSolution/APP/AllEffectKit/AllEffectKitConstants.h`文件输入 LicenseBag.bundle 中的 license 文件名称<br>
	<img src="https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_9bdb1fc597d2b41eab7f265f5d86c20c.png" width="500px" >
