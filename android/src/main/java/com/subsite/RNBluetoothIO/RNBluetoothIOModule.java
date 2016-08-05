package com.subsite.RNBluetoothIO;

import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings.Secure;

import com.google.android.gms.iid.InstanceID;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Nullable;

public class RNBluetoothIOModule extends ReactContextBaseJavaModule {

  ReactApplicationContext reactContext;

  public RNBluetoothIOModule(ReactApplicationContext reactContext) {
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

}
