Pod::Spec.new do |spec|
  spec.name         = 'AllIMKit'
  spec.version      = '1.0.0'
  spec.summary      = 'AllIMKit'
  spec.description  = 'AllIMKit ... '
  spec.homepage     = 'https://github.com/volcengine'
  spec.license      = { :type => 'Copyright', :text => 'Bytedance copyright' }
  spec.author       = { 'bytertc' => 'volcengine rtc' }
  spec.source       = { :path => './'}
  spec.ios.deployment_target = '9.0'
  spec.vendored_frameworks = 'VolcEngineRTS.xcframework'
  spec.requires_arc = true



  spec.source_files = '**/*.{h,m,c,mm,a}'
 
  spec.pod_target_xcconfig = {'CODE_SIGN_IDENTITY' => ''}
  
  spec.prefix_header_contents = '#import "ToolKit.h"', '#import "AllIMKitConstants.h"'

  spec.dependency 'ToolKit'
end
