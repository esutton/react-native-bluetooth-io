package com.subsite.bluetoothio;

import android.app.Activity;

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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;

import android.util.Base64;
import android.util.Log;

//import com.google.android.gms.iid.InstanceID;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.common.ReactConstants;
//import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
//import java.util.Locale;
import java.util.Map;
import java.util.Set;
//import java.util.TimeZone;
import java.util.UUID;

import javax.annotation.Nullable;

// http://cleancodedevelopment-qualityseal.blogspot.com.br/2012/10/understanding-callbacks-with-java.html
interface IConnection {
  void bytesReceived(byte[] byteArray);
  void signalConnect();
  void signalDisonnect();
  void signalStateChanged(int state);
}

public class BluetoothIOModule extends ReactContextBaseJavaModule implements IConnection {

  static final String ERROR_INVALID_CONTENT = "E_INVALID_CONTENT";

  // SPP UUID
  // BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
  private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

  ReactApplicationContext mReactContext;

  private static final String TAG = "BluetoothIOModule";

  private Set<BluetoothDevice> m_bondedDevices = new HashSet<BluetoothDevice>();

  // Name of the connected device
  private String mConnectedDeviceName = null;

  // Member object for the chat services
  private BluetoothChatService mChatService = null;

  public BluetoothIOModule(ReactApplicationContext reactContext) {
    super(reactContext);

    mReactContext = reactContext;

    // Initialize the BluetoothChatService to perform bluetooth connections
    mChatService = new BluetoothChatService(null, null);
    mChatService.subscribe(this);
  }

  @Override
  public String getName() {
    // name must match ame used in JS index.js require statement.
    // var BluetoothIOModule = require('react-native').NativeModules.BluetoothIOModule;
    return TAG;
  }

  @ReactMethod
  public void componentDidMount() {

    Activity activity = getCurrentActivity();
    if( null == activity ){
      Log.d(TAG, String.format("*** Error cannot register BroadcastReceiver null == activity"));
      return;
    }

    // http://stackoverflow.com/questions/9693755/detecting-state-changes-made-to-the-bluetoothadapter
    // https://github.com/yamill/react-native-orientation/blob/master/android/src/main/java/com/github/yamill/orientation/OrientationModule.java
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
          final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
          BluetoothAdapter.ERROR);

          String stateName = "";
          switch (state) {
            case BluetoothAdapter.STATE_OFF:
            stateName = "off";
            break;
            case BluetoothAdapter.STATE_TURNING_OFF:
            stateName = "offTransition";
            break;
            case BluetoothAdapter.STATE_ON:
            stateName = "on";
            break;
            case BluetoothAdapter.STATE_TURNING_ON:
            stateName = "onTransition";
            break;
          }
          Log.d(TAG, String.format("BluetoothAdapter.ACTION_STATE_CHANGED to %s", stateName));

          WritableMap params = Arguments.createMap();
          params.putInt("state", state);
          params.putString("name", stateName);
          sendEvent(Constants.EVENT_ON_BLUETOOTH_STATE_CHANGE, params);

      // When discovery finds a device

        } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {

        // Get the BluetoothDevice object from the Intent
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        Log.d(TAG, String.format("BluetoothAdapter.ACTION_FOUND: %s %s, BondState=%d",
        device.getName(), device.getAddress(), device.getBondState()));

        WritableMap params = Arguments.createMap();
        params.putString("name", device.getName());
        params.putString("address", device.getAddress());
        params .putBoolean("bondState", device.getBondState());
        sendEvent(Constants.EVENT_ON_BLUETOOTH_DISCOVERY_FOUND, params);

        // If it's already paired, skip it, because it's been listed already
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
          //mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
          Log.d(TAG, String.format("Found new: %s %s", device.getName(), device.getAddress()));
        }
        // When discovery is finished, change the Activity title
      } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {

        //setProgressBarIndeterminateVisibility(false);
        //setTitle(R.string.select_device);
        Log.d(TAG, String.format("BluetoothAdapter.ACTION_DISCOVERY_FINISHED"));

        sendEvent(Constants.EVENT_ON_BLUETOOTH_DISCOVERY_STOP);

      }
      }
    };


    // Register for broadcasts on BluetoothAdapter state change
    IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    getCurrentActivity().registerReceiver(mReceiver, filter);

    // Register for broadcasts when a device is discovered
    filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    getCurrentActivity().registerReceiver(mReceiver, filter);

    // Register for broadcasts when discovery has finished
    filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    getCurrentActivity().registerReceiver(mReceiver, filter);


    // From: https://github.com/yamill/react-native-orientation/blob/master/android/src/main/java/com/github/yamill/orientation/OrientationModule.java
    LifecycleEventListener listener = new LifecycleEventListener() {
      @Override
      public void onHostResume() {
        getCurrentActivity().registerReceiver(mReceiver, new IntentFilter("onConfigurationChanged"));
      }

      @Override
      public void onHostPause() {
        try
        {
          getCurrentActivity().unregisterReceiver(mReceiver);
        }
        catch (java.lang.IllegalArgumentException e) {
          FLog.e(ReactConstants.TAG, "receiver already unregistered", e);
        }
      }

      @Override
      public void onHostDestroy() {
        try
        {
          getCurrentActivity().unregisterReceiver(mReceiver);
        }
        catch (java.lang.IllegalArgumentException e) {
          FLog.e(ReactConstants.TAG, "receiver already unregistered", e);
        }
      }
    };

    mReactContext.addLifecycleEventListener(listener);
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();

    constants.put("EventOnDataRx", Constants.EVENT_ON_DATA_RX);
    constants.put("EventOnStateChange", Constants.EVENT_ON_STATE_CHANGE);
    constants.put("EventOnBluetoothStateChange", Constants.EVENT_ON_BLUETOOTH_STATE_CHANGE);
    return constants;
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

  private void sendEvent(String eventName,
  @Nullable WritableMap params) {
    getReactApplicationContext()
    .getJSModule(RCTNativeAppEventEmitter.class)
    .emit(eventName, params);
  }
    private void sendEvent(String eventName) {
    getReactApplicationContext()
    .getJSModule(RCTNativeAppEventEmitter.class)
    .emit(eventName, null);
  }


  // Raise event onDataRx
  // Need to Base64 encode ( See react WebSocketModule.java )
  private void emitOnDataRx(String base64Content) {

    // if (payloadType == WebSocket.PayloadType.BINARY) {
    //   message = Base64.encodeToString(bufferedSource.readByteArray(), Base64.NO_WRAP);
    // } else {
    //   message = bufferedSource.readUtf8();
    // }

    WritableMap params = Arguments.createMap();
    params.putString("data", base64Content);
    sendEvent(Constants.EVENT_ON_DATA_RX, params);
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
        params.putBoolean("bondState", bluetoothDevice.getBondState());        

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

  @ReactMethod
  public void debugFunction1() {
    Activity activity = getCurrentActivity();
    if( null == activity ) {
      Log.d(TAG, String.format("*** Warning getCurrentActivity is null"));
    } else {
      Log.d(TAG, String.format("    Success getCurrentActivity is not null"));
    }
  }

    @ReactMethod
  public void discoveryStart() {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (bluetoothAdapter == null) {
      Log.d(TAG, "BluetoothAdapter not found in discoveryStart");
      return;
    }

    // If we're already discovering, stop it
    if (bluetoothAdapter.isDiscovering()) {
      bluetoothAdapter.cancelDiscovery();
    }

    // Request discover from BluetoothAdapter
    bluetoothAdapter.startDiscovery();
    sendEvent(Constants.EVENT_ON_BLUETOOTH_DISCOVERY_START);
  }

    @ReactMethod
  public void discoveryStop() {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (bluetoothAdapter == null) {
      Log.d(TAG, "BluetoothAdapter not found in discoveryStop");
      return;
    }
    bluetoothAdapter.cancelDiscovery();
  }



  // ToDo: Add BroadcastReceiver
  // http://stackoverflow.com/questions/9693755/detecting-state-changes-made-to-the-bluetoothadapter
  // https://github.com/yamill/react-native-orientation/blob/master/android/src/main/java/com/github/yamill/orientation/OrientationModule.java
  @ReactMethod
  public void setBluetoothEnable(boolean value) {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (bluetoothAdapter == null) {
      Log.d(TAG, "BluetoothAdapter not found");
      return;
    }

    if(value) {
      bluetoothAdapter.enable();
      return;
    }
    bluetoothAdapter.disable();
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
      Log.d(TAG, String.format("writeString[%d]={%s}", data.length(), data));

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

  /////////////////////////////////////////////////////////////////
  // B l u e T o o t h
  //
  // IConnection
  /////////////////////////////////////////////////////////////////

  // Convert Java byte[] into JavaScript
  public void bytesReceived(byte[] byteArray) {
    Log.d(TAG, String.format("IConnection: bytesReceived %d bytes", byteArray.length));

    // byte[] rawbytes={0xa, 0x2, (byte) 0xff};
    // byte[] encoded = Base64.getEncoder().encode(rawbytes);
    // String return = new String(encoded);

    String base64Content = Base64.encodeToString(byteArray, Base64.NO_WRAP);
    emitOnDataRx(base64Content);
  }

  public void signalConnect() {
    Log.d(TAG, String.format("IConnection: signalConnect"));
  }
  public void signalDisonnect() {
    Log.d(TAG, String.format("IConnection: signalDisonnect"));
  }
  public void signalStateChanged(int state) {

    String stateName = mChatService.ConnectionState[state];
    Log.d(TAG, String.format("IConnection: signalStateChanged:[%d]=%s",
    state, stateName));

    WritableMap params = Arguments.createMap();
    params.putInt("state", state);
    params.putString("name", stateName);
    sendEvent(Constants.EVENT_ON_STATE_CHANGE, params);
  }

}
