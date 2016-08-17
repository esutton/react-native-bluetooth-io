/**
* @providesModule react-native-bluetooth-io
*/

type BluetoothDeviceInfo = {
  name: string;
  address: string;
};


var NativeAppEventEmitter = require('react-native').NativeAppEventEmitter;  // iOS
var DeviceEventEmitter = require('react-native').DeviceEventEmitter;        // Android
var base64 = require('base-64');
var utf8 = require('utf8');

// NativeModules.<Name> must match name returned by ReactContextBaseJavaModule::getName in java code
let BluetoothIOModule = require('react-native').NativeModules.BluetoothIOModule;
let Buffer = require('buffer').Buffer;

let listeners = {};
// let onDataRxEvent = "onDataRx";
// let onConnectEvent = "onConnect";
// let onDisconnectEvent = "onDisconnect";

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

  listenerAdd(eventName, cb) {
    listeners[cb] = DeviceEventEmitter.addListener(eventName,
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

  // Call from componentDidMount (when getCurrentActivity() is not null)
  componentDidMount() {
      BluetoothIOModule.componentDidMount();
  },


  debugFunction1() {
      BluetoothIOModule.debugFunction1();
  },

  // Returns number of bytes written
  writeString(data: string): Promise<number> {
    return BluetoothIOModule.writeString(data);
  },

  connect(device: object, secure: boolean): Promise<BluetoothDeviceInfo[]> {
    return BluetoothIOModule.connect(device, secure);
  },

  discoveryStart() {
      BluetoothIOModule.discoveryStart();
  },

  discoveryStop() {
      BluetoothIOModule.discoveryStop();
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

  // Get Bluetooth Adapter state
  getState(): Promise<number> {
    return BluetoothIOModule.getState();
  },

  // Set
  // ToDo: Register a BroadcastReceiver to listen for any changes in the state of the BluetoothAdapter:
  // http://stackoverflow.com/questions/9693755/detecting-state-changes-made-to-the-bluetoothadapter
  setBluetoothEnable(value) {
    console.log('setBluetoothEnable:', value);
    return BluetoothIOModule.setBluetoothEnable(value);
  }

};

module.exports = BluetoothIO;
