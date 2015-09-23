package codefury.wifip2p;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pDevice;
import 	android.net.wifi.p2p.WifiP2pConfig;
import android.os.Looper;

import java.lang.Override;
import java.util.List;

public class WifiP2P extends CordovaPlug {

    private WifiP2pManager mManager;
    private Channel mChannel;

    public static final int SUCCESS_DISCOVER_PEERS = 200;
    public static final int SUCCESS_CONNECT_TO_PEER = 201;


    public static final int ERROR_NOT_ENABLED = 400;
    public static final int ERROR_CONNECT_TO_PEER = 401;


    public static String ACTION_START_SCAN = "ACTION_START_SCAN";
    public static String ACTION_CONNECT_TO_PEER = "ACTION_CONNECT_TO_PEER";
    public static String ACTION_STOP_DISCOVER_PEER = "ACTION_STOP_DISCOVER_PEER";
    public static String ACTION_CANCEL_CONNECT = "ACTION_CANCEL_CONNECT";

    private AndroidBroadcastReceiver wifiBroadcastReceiver;

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION_START_SCAN)) {
            initialize(callbackContext);
            return true;
        } else if (action.equals(ACTION_CONNECT_TO_PEER)) {
            String address = args.getJSONObject(0);
            connectToPeer(address, callbackContext);
            return true;
        } else if (action.equals(ACTION_STOP_DISCOVER_PEER)) {
            stopPeerDiscovery();
            return true;
        } else if (action.equals(ACTION_CANCEL_CONNECT)) {
            cancelConnect();
            return true;
        }
        return false;
    }

    private void echo(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }


    private void initialize(CallbackContext cb) {
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(cordova, Looper.getMainLooper(), null);
        mReceiver = new AndroidBroadcastReceiver(mManager, mChannel, cordova);
    }


    private JSONArray constructPeerDevicesJSONString(WifiP2pDeviceList devices) {
        JSONArray devicesListJSON = new JSONArray();
        JSONObject json = new JSONObject();
        List<WifiP2pDevice> devicesList = devices.getDeviceList();
        for (WifiP2pDevice dv: devicesList) {
            JSONObject json = new JSONObject();
            json.put("name", dv.deviceName);
            json.put("address", dv.deviceAddress);
            json.put("status", dv.status);
            devicesListJSON.put(json);
        }
        return devicesListJSON;
    }

    public void stopPeerDiscovery() {
       if (mManager != null) {
           mManager.stopPeerDiscovery(mChannel, null);
       }
    }

    public void cancelConnect() {
        if (mManager != null) {
            mManager.cancelConnect(mChannel, null);
        }
    }



    public void connectToPeer(final String deviceAddress, CallbackContext cb) {
        //obtain a peer from the WifiP2pDeviceList
        WifiP2pDevice device;
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                cb.success(deviceAddress);
            }

            @Override
            public void onFailure(int reason) {
                cb.error(deviceAddress);
            }
        });
    }

    private class AndroidBroadcastReceiver extends BroadcastReceiver {

        private CallbackContext cb;
        public AndroidBroadcastReceiver (CallbackContext cb) {
            this.cb = cb;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)) {
                //wifi p2p started or stopped
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // Wifi P2P is enabled

                    // request available peers from the wifi p2p manager. This is an
                    // asynchronous call and the calling activity is notified with a
                    // callback on PeerListListener.onPeersAvailable()
                    if (mManager != null) {
                        mManager.requestPeers(mChannel, new PeerListListener() {
                            @Override
                            public void onPeersAvailable(WifiP2pDeviceList peers) {
                                JSONArray devices = constructPeerDevicesJSONString(peers);
                                cb.success(devices);
                            }
                        });
                    }

                } else {
                    // Wi-Fi P2P is not enabled
                    cb.error(ERROR_NOT_ENABLED);
                }
            }
        }
    }
}
