package com.subsite.bluetoothio;

import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothAdapter.BluetoothStateChangeCallback;
import android.bluetooth.BluetoothDevice;

//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.os.Handler;
//import android.os.Message;
//import android.os.ParcelUuid;
//import android.provider.Settings.Secure;
import android.util.Log;

//import com.google.android.gms.iid.InstanceID;


import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
import java.util.HashSet;
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
import java.util.Set;
//import java.util.TimeZone;
import java.util.UUID;

import javax.annotation.Nullable;

public class BluetoothIOModule extends ReactContextBaseJavaModule {

  static final String ERROR_INVALID_CONTENT = "E_INVALID_CONTENT";

  // SPP UUID
  // BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
  private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

  ReactApplicationContext reactContext;

  private static final String TAG = "BluetoothIOModule";

  private Set<BluetoothDevice> m_bondedDevices = new HashSet<BluetoothDevice>();

  // Name of the connected device
  private String mConnectedDeviceName = null;

  // Member object for the chat services
  private BluetoothChatService mChatService = null;

  public BluetoothIOModule(ReactApplicationContext reactContext) {
    super(reactContext);

    final ReactApplicationContext ctx = reactContext;

    // Initialize the BluetoothChatService to perform bluetooth connections
    //mChatService = new BluetoothChatService(getActivity(), mHandler);
    mChatService = new BluetoothChatService(null, null);

  }

  @Override
  public String getName() {
    // name must match ame used in JS index.js require statement.
    // var BluetoothIOModule = require('react-native').NativeModules.BluetoothIOModule;
    return TAG;
  }

  // // The Handler that gets information back from the BluetoothChatService
  // private final Handler mHandler = new Handler() {
  //
  //   @Override
  //   public void handleMessage(Message msg) {
  //     //FragmentActivity activity = getActivity();
  //
  //     switch (msg.what) {
  //       case Constants.MESSAGE_STATE_CHANGE:
  //       switch (msg.arg1) {
  //         case BluetoothChatService.STATE_CONNECTED:
  //         //mConversationArrayAdapter.clear();
  //         Log.d(TAG, "BluetoothChatService connected to " + "deviceName");
  //         break;
  //         case BluetoothChatService.STATE_CONNECTING:
  //         Log.d(TAG, "BluetoothChatService connecting");
  //         break;
  //         case BluetoothChatService.STATE_LISTEN:
  //         case BluetoothChatService.STATE_NONE:
  //         Log.d(TAG, "BluetoothChatService not connected");
  //         break;
  //       }
  //       break;
  //       case Constants.MESSAGE_WRITE:
  //       byte[] writeBuf = (byte[]) msg.obj;
  //       // construct a string from the buffer
  //       String writeMessage = new String(writeBuf);
  //       Log.d(TAG, "Tx: " + writeMessage);
  //       break;
  //       case Constants.MESSAGE_READ:
  //       byte[] readBuf = (byte[]) msg.obj;
  //       // construct a string from the valid bytes in the buffer
  //       String readMessage = new String(readBuf, 0, msg.arg1);
  //       Log.d(TAG, "Rx: " + readMessage);
  //       break;
  //       case Constants.MESSAGE_DEVICE_NAME:
  //       // save the connected device's name
  //       mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
  //       Log.d(TAG, "Connected to " + mConnectedDeviceName);
  //       break;
  //
  //       case Constants.MESSAGE_TOAST:
  //         Log.d(TAG, msg.getData().getString(Constants.TOAST));
  //       break;
  //     }
  //   }
  // };


  ////////////////////////////////////////////////////////////////////////

  private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
    reactContext
    .getJSModule(RCTNativeAppEventEmitter.class)
    .emit(eventName, params);
  }


  // Raise event onDataRx
  // Need to Base64 encode ( See react WebSocketModule.java )
  private void emitOnDataRx(String data) {

    // if (payloadType == WebSocket.PayloadType.BINARY) {
    //   message = Base64.encodeToString(bufferedSource.readByteArray(), Base64.NO_WRAP);
    // } else {
    //   message = bufferedSource.readUtf8();
    // }

    WritableMap params = Arguments.createMap();
    params.putString("data", data);
    sendEvent(getReactApplicationContext(), "onDataRx", params);
  }

  //@ReactMethod
  // callback test
  // public void getIPAddress(final Callback callback) {
  //   String ipAddressString = "10.9.8.7";
  //
  //   getPairedBluetooth();
  //
  //   callback.invoke(ipAddressString);
  // }

  // @ReactMethod
  // public void exists(String filepath, Promise promise) {
  //   try {
  //     promise.resolve(true);
  //   } catch (Exception ex) {
  //     ex.printStackTrace();
  //     promise.reject(ex);
  //   }
  // }


  // http://www.programcreek.com/java-api-examples/index.php?class=android.bluetooth.BluetoothAdapter&method=getBondedDevices
  // private void updateBondedBluetoothDevices() {
  //     m_bondedDevices.clear();
  //
  //     BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
  //     if (adapter != null) {
  //         Set<BluetoothDevice> devices = adapter.getBondedDevices();
  //         if (devices != null) {
  //             for (BluetoothDevice device : devices) {
  //                 if (device.getBondState() != BluetoothDevice.BOND_NONE) {
  //                     m_bondedDevices.add(device);
  //                 }
  //             }
  //         }
  //     }
  // }

  @ReactMethod
  public void getDeviceList(String deviceNameFilter, Promise promise) {

    try {
      Log.d(TAG, "getDeviceList: deviceNameFilter = " + deviceNameFilter);
      WritableArray deviceArray = Arguments.createArray();

      BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if (bluetoothAdapter == null) {
        Log.d(TAG, "BluetoothAdapter not found");
        promise.resolve(deviceArray);
        return;
      }

      // http://www.programcreek.com/java-api-examples/index.php?class=android.bluetooth.BluetoothAdapter&method=getBondedDevices
      Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
      if (deviceSet == null || deviceSet.size() == 0) {
        Log.d(TAG, "getBondedDevices not found");
        promise.resolve(deviceArray);
        return;
      }
      for(BluetoothDevice bluetoothDevice: deviceSet){
        Log.d(TAG, "BluetoothDevice: " + bluetoothDevice.getName()
        + ", " + bluetoothDevice.getAddress());

        WritableMap bluetoothDeviceMap = Arguments.createMap();
        bluetoothDeviceMap.putString("name", bluetoothDevice.getName());
        bluetoothDeviceMap.putString("address", bluetoothDevice.getAddress());

        // getUuids does not start a service discovery procedure to retrieve the UUIDs
        // from the remote device. Instead, the local cached copy of the service UUIDs are returned.
        //         ParcelUuid[] uuidArray = (ParcelUuid[]) bluetoothDevice.getUuids();
        //         for (ParcelUuid uuid: uuidArray) {
        //           Log.d(TAG, "UUID: " + uuid.getUuid().toString());
        //         }

        deviceArray.pushMap(bluetoothDeviceMap);
      }
      promise.resolve(deviceArray);

    } catch (Exception ex) {
      ex.printStackTrace();
      promise.reject(ex);
    }
  }

  @ReactMethod
  public void getState(Promise promise) {
    try {
      Log.d(TAG, "getState");
      int state = 0;

      BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if (bluetoothAdapter == null) {
        Log.d(TAG, "BluetoothAdapter not found");
        promise.resolve(state);
        return;
      }

      // Possible return values are STATE_OFF, STATE_TURNING_ON, STATE_ON, STATE_TURNING_OFF.
      state = bluetoothAdapter.getState();
      Log.d(TAG, "BluetoothAdapter state: 0x" + Integer.toHexString(state));
      promise.resolve(state);

    } catch (Exception ex) {
      ex.printStackTrace();
      promise.reject(ex);
    }
  }




  public static String[] getPairedBluetooth() {

    Log.d(TAG, "getPairedBluetooth");


    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (bluetoothAdapter == null) {
      Log.d(TAG, "BluetoothAdapter not found");
      return new String[0];
    }

    Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
    if (devices == null || devices.size() == 0) {
      Log.d(TAG, "getBondedDevices not found");
      return new String[0];
    }

    // 08-08 16:05:00.808 22170 22242 D BluetoothIOModule: BluetoothDevice: TK_0003, 00:07:80:46:87:CD
    // 08-08 16:05:00.809 22170 22242 D BluetoothIOModule: BluetoothDevice: TK_0100, 00:07:80:A1:17:69
    String[] deviceNames = new String[devices.size()];
    int i = 0;
    for (BluetoothDevice d : devices) {
      deviceNames[i] = d.getName() + ", " + d.getAddress();
      Log.d(TAG, "BluetoothDevice: " + deviceNames[i] );
      ++i;
    }
    return deviceNames;
  }

  @ReactMethod
  public void writeString(String data, Promise promise) {
    try {
      Log.d(TAG, String.format("write[%d]={%s}", data.length(), data));

      // Debug event emitOnDataRx
      //emitOnDataRx(data);

      byte[] byteArray = data.getBytes(StandardCharsets.UTF_8);
      mChatService.write(byteArray);

      //promise.resolve(data.length());
    } catch (Exception ex) {
      ex.printStackTrace();
      //promise.reject(ex);
    }
  }

  private void dumpMap(ReadableMap readableMap) {
    ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
    while (iterator.hasNextKey()) {
      String key = iterator.nextKey();
      Log.d(TAG, String.format("key[%s]=%s", key, readableMap.getString(key)));
    }
  }

  private String mapGetString(ReadableMap readableMap, String key) {
    if (readableMap.hasKey(key)) {
      return readableMap.getString(key);
    }
    return "";
  }

  @ReactMethod
  public void connect(ReadableMap bluetoothDevice, boolean secure) {

    if (null == bluetoothDevice) {
      //promise.reject(ERROR_INVALID_CONTENT, "device cannot be null");
      return;
    }

    dumpMap(bluetoothDevice);


    //public synchronized void connect(BluetoothDevice device, boolean secure)
    try {

      String name = mapGetString(bluetoothDevice, "name");
      String address = mapGetString(bluetoothDevice, "address");
      Log.d(TAG, String.format("connect to bluetoothDevice: %s, {%s}", name, address));

      BluetoothAdapter bluetoothAdapter = mChatService.bluetoothAdapter();

      boolean isValid = bluetoothAdapter.checkBluetoothAddress(address);
      Log.d(TAG, String.format("checkBluetoothAddress: %s", isValid ? "true" : "false"));

      BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
      mChatService.connect(device, secure);

      //promise.resolve(data.length());
    } catch (Exception ex) {
      ex.printStackTrace();
      //promise.reject(ex);
    }
  }


}

/////////////////////////////////////////////////////////////////
// B l u e T o o t h
/////////////////////////////////////////////////////////////////
