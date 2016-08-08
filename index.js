/**
 * @providesModule react-native-bluetooth-io
 */

var NativeBluetoothIOModule = require('react-native').NativeModules.BluetoothIOModule;

module.exports = {

  sayHello(name: string): Promise<string> {
    return NativeBluetoothIOModule.sayHello(name);
  },

  getGreeting() {
    return "Hello Eddie";
  },

  versionInfo() {
    return "Version 0.0.0";
  },

};
