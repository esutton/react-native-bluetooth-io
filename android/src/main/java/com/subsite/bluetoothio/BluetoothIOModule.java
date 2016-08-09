package com.subsite.bluetoothio;

import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothAdapter.BluetoothStateChangeCallback;
import android.bluetooth.BluetoothDevice;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.provider.Settings.Secure;
import android.util.Log;

//import com.google.android.gms.iid.InstanceID;


import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;


import javax.annotation.Nullable;

public class BluetoothIOModule extends ReactContextBaseJavaModule {

  // SPP UUID
  // BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
  private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

  ReactApplicationContext reactContext;

  private static final String TAG = "BluetoothIOModule";

  private Set<BluetoothDevice> m_bondedDevices = new HashSet<BluetoothDevice>();

  public BluetoothIOModule(ReactApplicationContext reactContext) {
    super(reactContext);

    final ReactApplicationContext ctx = reactContext;
  }

  @Override
  public String getName() {
    // name must match ame used in JS index.js require statement.
    // var BluetoothIOModule = require('react-native').NativeModules.BluetoothIOModule;
    return TAG;
  }

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
      emitOnDataRx(data);

      //promise.resolve(data.length());
    } catch (Exception ex) {
      ex.printStackTrace();
      //promise.reject(ex);
    }
  }

}
