/**
 * @providesModule react-native-bluetooth-io
 */

var RNBluetoothIO = require('react-native').NativeModules.RNBluetoothIO;

module.exports = {

  function1(): {
    return "Called function1";
  },

  sayHello(name: string): Promise<string> {
    return RNBluetoothIO.sayHello(name);
  },

};
