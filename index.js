/**
 * @providesModule react-native-bluetooth-io
 */

var BluetoothIOPackage = require('react-native').NativeModules.BluetoothIOPackage;

var RNFSManager = require('react-native').NativeModules.RNFSManager;

var BluetoothIO = {

  sayHello(name: string): Promise<string> {
    return BluetoothIOPackage.sayHello(name);
  },

};

module.exports = BluetoothIO;
