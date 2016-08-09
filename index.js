/**
* @providesModule react-native-bluetooth-io
*/

// NativeModules.<Name> must match name returned by ReactContextBaseJavaModule::getName in java code
var BluetoothIOModule = require('react-native').NativeModules.BluetoothIOModule;


type BluetoothDeviceInfo = {
  name: string;
  address: string;
};

var BluetoothIO  = {

  sayHello(name: string): Promise<string> {
    return BluetoothIOModule.sayHello(name);
  },

  getGreeting() {
    return "Hello Eddie";
  },

  getIPAddress(ip) {
    BluetoothIOModule.getIPAddress(ip);
  },

  versionInfo() {
    return "Version 0.0.0";
  },

  exists(filepath: string): Promise<boolean> {
    return BluetoothIOModule.exists(filepath);
  },

  // 08-09 08:57:59.423 25258 25435 I ReactNativeJS: 'BluetoothIO.getDeviceList result: ', [ { name: 'TK_0003', address: '00:07:80:46:87:CD' },
  // 08-09 08:57:59.423 25258 25435 I ReactNativeJS:   { name: 'TK_0100', address: '00:07:80:A1:17:69' },
  // 08-09 08:57:59.423 25258 25435 I ReactNativeJS:   { name: 'BlueStar GNSS 18300008', address: '00:07:80:0D:21:0E' } ]
  getDeviceList(deviceNameFilter: string): Promise<BluetoothDeviceInfo[]> {
    return BluetoothIOModule.getDeviceList(deviceNameFilter).then(deviceArray => {
      return deviceArray.map(device => ({
        name: device.name,
        address: device.address,
      }));
    });
  },

  getState(): Promise<number> {
    return BluetoothIOModule.getState();
  },

};

module.exports = BluetoothIO;
