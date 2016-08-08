package com.subsite.bluetoothio;

import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings.Secure;

//import com.google.android.gms.iid.InstanceID;


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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Nullable;

public class BluetoothIOModule extends ReactContextBaseJavaModule {

  ReactApplicationContext reactContext;

  public BluetoothIOModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNBluetoothIO";
  }

  @ReactMethod
  public void sayHello(String name, Promise promise) {
    try {
      promise.resolve("Hello " + name);
    } catch (Exception ex) {
      ex.printStackTrace();
      reject(promise, name, ex);
    }
  }

  private void reject(Promise promise, String name, Exception ex) {
    promise.reject(ex.getMessage());
  }

}
