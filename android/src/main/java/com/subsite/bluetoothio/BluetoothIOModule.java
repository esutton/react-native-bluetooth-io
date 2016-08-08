package com.subsite.bluetoothio;

import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothAdapter.BluetoothStateChangeCallback;
import android.bluetooth.BluetoothDevice;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
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
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;



import javax.annotation.Nullable;

public class BluetoothIOModule extends ReactContextBaseJavaModule {

  ReactApplicationContext reactContext;

  private static final String TAG = "BluetoothIOModule";

  private Set<BluetoothDevice> m_bondedDevices = new HashSet<BluetoothDevice>();

  public BluetoothIOModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    // This must match the name used in JS index.js
    // var BluetoothIOModule = require('react-native').NativeModules.BluetoothIOModule;
    return TAG;
  }

  @ReactMethod
  public void sayHello(String name, Promise promise) {
    try {
      promise.resolve("Hello " + name);
    } catch (Exception ex) {
      ex.printStackTrace();
      promise.reject(ex);
    }
  }

  @ReactMethod
  // callback test
  public void getIPAddress(final Callback callback) {
    String ipAddressString = "10.9.8.7";

    getPairedBluetooth();

    callback.invoke(ipAddressString);
  }

  @ReactMethod
  public void exists(String filepath, Promise promise) {
    try {
      promise.resolve(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      promise.reject(ex);
    }
  }
  
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

  public static String[] getPairedBluetooth() {

    Log.d(TAG, "getPairedBluetooth");


    BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    if (mBtAdapter == null) {
      Log.d(TAG, "BluetoothAdapter not found");
      return new String[0];
    }

    Set<BluetoothDevice> devices = mBtAdapter.getBondedDevices();
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


}
