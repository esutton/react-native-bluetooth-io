Pod::Spec.new do |s|
  s.name         = "RNBluetoothIO"
  s.version      = "0.9.3"
  s.summary      = "Device Information for react-native"

  s.homepage     = "https://github.com/esutton/react-native-bluetooth-io"

  s.license      = "MIT"
  s.authors      = { "Rebecca Hughes" => "rebecca@learnium.net" }
  s.platform     = :ios, "7.0"

  s.source       = { :git => "https://github.com/esutton/react-native-bluetooth-io.git" }

  s.source_files  = "RNBluetoothIO/*.{h,m}"

  s.dependency 'React'
end
