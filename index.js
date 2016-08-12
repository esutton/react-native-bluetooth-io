/**
* @providesModule react-native-bluetooth-io
*/

// NativeModules.<Name> must match name returned by ReactContextBaseJavaModule::getName in java code
let BluetoothIOModule = require('react-native').NativeModules.BluetoothIOModule;
let Buffer = require('buffer').Buffer;

type BluetoothDeviceInfo = {
  name: string;
  address: string;
};

let DeviceEventEmitter = require('react-native').DeviceEventEmitter;
let listeners = {};
let onDataRxEvent = "onDataRx";
let onConnectEvent = "onConnect";
let onDisconnectEvent = "onDisconnect";

const BluetoothConnectionState = {
  "None":       0,
  "Listen":     1,
  "Connecting": 2,
  "Connected":  3
};

let BluetoothIO  = {

  versionInfo() {
    return "Version 0.0.0";
  },

  listenerAdd(cb) {
    listeners[cb] = DeviceEventEmitter.addListener(onDataRxEvent,
      (messageRx) => {
        cb(messageRx);
      });
  },

  listenerRemove(cb) {
    if (!listeners[cb]) {
      return;
    }
    listeners[cb].remove();
    listeners[cb] = null;
  },

  // Returns number of bytes written
  writeString(data: string): Promise<number> {
    return BluetoothIOModule.writeString(data);
  },

  connect(device: object, secure: boolean): Promise<BluetoothDeviceInfo[]> {
    return BluetoothIOModule.connect(device, secure);
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
