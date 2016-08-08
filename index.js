/**
 * @providesModule react-native-bluetooth-io
 */

var BluetoothIOModule = require('react-native').NativeModules.BluetoothIOModule;

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

};

module.exports = BluetoothIO;
