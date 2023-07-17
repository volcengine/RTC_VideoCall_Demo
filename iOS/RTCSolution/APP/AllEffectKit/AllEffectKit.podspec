Pod::Spec.new do |spec|
  spec.name         = 'AllEffectKit'
  spec.version      = '1.0.0'
  spec.summary      = 'AllEffectKit'
  spec.description  = 'AllEffectKit ... '
  spec.homepage     = 'https://github.com/volcengine'
  spec.license      = { :type => 'Copyright', :text => 'Bytedance copyright' }
  spec.author       = { 'bytertc' => 'volcengine rtc' }
  spec.source       = { :path => './'}
  spec.ios.deployment_target = '9.0'
  spec.vendored_frameworks = 'effect-sdk.framework'
  spec.requires_arc = true
  spec.libraries = 'stdc++', 'z'
  spec.frameworks   = 'Accelerate','AssetsLibrary','AVFoundation','CoreGraphics','CoreImage','CoreMedia','CoreVideo','Foundation','QuartzCore','UIKit','CoreMotion'
  spec.weak_frameworks = 'Metal','MetalPerformanceShaders', 'Photos', 'CoreML'
  spec.source_files = '**/*.{h,m,c,mm,a}'
  spec.resource_bundles = {
    'AllEffectKit' => ['Resource/Localizable.bundle',
                         'Resource/*.xcassets']
  }
  spec.pod_target_xcconfig = {'CODE_SIGN_IDENTITY' => ''}
  spec.resources = ['Resource/*.{licbag,plist,json}',
                    'Resource/ComposeMakeup.bundle',
                    'Resource/FilterResource.bundle',
                    'Resource/LicenseBag.bundle',
                    'Resource/ModelResource.bundle',
                    'Resource/StickerResource.bundle',]
  spec.prefix_header_contents = '#import "Masonry.h"',
                                '#import "ToolKit.h"',
                                '#import "AllEffectKitConstants.h"'

  spec.dependency 'ToolKit'
end
