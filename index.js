/**
 * @providesModule react-native-bluetooth-io
 */

var RNBluetoothIO = require('react-native').NativeModules.RNBluetoothIO;

module.exports = {
  getUniqueID: function () {
    return RNBluetoothIO.uniqueId;
  },
  getInstanceID: function() {
    return RNBluetoothIO.instanceId;
  },
  getDeviceId: function () {
    return RNBluetoothIO.deviceId;
  },
  getManufacturer: function () {
    return RNBluetoothIO.systemManufacturer;
  },
  getModel: function () {
    return RNBluetoothIO.model;
  },
  getSystemName: function () {
    return RNBluetoothIO.systemName;
  },
  getSystemVersion: function () {
    return RNBluetoothIO.systemVersion;
  },
  getBundleId: function() {
    return RNBluetoothIO.bundleId;
  },
  getBuildNumber: function() {
    return "RNBluetoothIO.buildNumber";
  },
  getVersion: function() {
    return RNBluetoothIO.appVersion;
  },
  getReadableVersion: function() {
    return RNBluetoothIO.appVersion + "." + RNBluetoothIO.buildNumber;
  },
  getDeviceName: function() {
    return RNBluetoothIO.deviceName;
  },
  getUserAgent: function() {
    return RNBluetoothIO.userAgent;
  },
  getDeviceLocale: function() {
    return RNBluetoothIO.deviceLocale;
  },
  getDeviceCountry: function() {
    return RNBluetoothIO.deviceCountry;
  },
};
