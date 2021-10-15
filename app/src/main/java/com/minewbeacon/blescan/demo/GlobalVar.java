package com.minewbeacon.blescan.demo;

import android.app.Application;

import com.minew.beacon.MinewBeaconManager;

public class GlobalVar extends Application {

    //activity
    public static final String TAG_ACTIVITY_JOIN = "JoinActivity";
    public static final String TAG_ACTIVITY_LOGIN = "LoginActivity";
    public static final String TAG_ACTIVITY_HOME = "HomeActivity";
    public static final String TAG_ACTIVITY_MAIN = "MainActivity";
    public static final String TAG_ACTIVITY_PAYMENT = "PaymentActivity";
    public static final String TAG_ACTIVITY_FLOW = "FlowActivity";
    public static final String TAG_ACTIVITY_DESTSEARCH = "DestSearchActivity";

    //fragment
    public static final String TAG_FRAGMENT_HOME = "HomeFragment";
    public static final String TAG_FRAGMENT_INFO = "InfoFragment";
    public static final String TAG_FRAGMENT_LIST = "ListFragment";
    public static final String TAG_FRAGMENT_CLINIC = "ClinicListFragment";
    public static final String TAG_FRAGMENT_Payment = "PaymentFragment";

    //adapter
    public static final String TAG_ADAPTER_DEST = "DestAdapter";


    public static final String URL = "http://52.78.153.155/index.php"; //aws
//    public static final String URL = "http://172.26.1.17:8000"; //local

    //Server URL
    public static final String URL_LOGIN = "/api/patient/login";
    public static final String URL_LOGOUT = "/api/patient/logout";
    public static final String URL_SIGNUP = "/api/patient/signup";
    public static final String URL_CLINIC = "/api/patient/clinic";
    public static final String URL_FLOW = "/api/patient/flow";
    public static final String URL_FLOW_END = "/api/patient/flow_end";
    public static final String URL_IAMPORT = "/patient/iamport/";
    public static final String URL_FLOW_NODE = "/api/patient/flow_node";
    public static final String URL_CURRENT_FLOW_NODE = "/api/patient/current_flow";
    public static final String URL_NAVIGATION = "/api/patient/navigation";
    public static final String URL_NAVIGATION_CURRUNT = "/api/patient/navigation_current";
    public static final String URL_STORAGE = "/api/patient/storage";
    public static final String URL_STORAGE_RECORD = "/api/patient/storage_record";
    public static final String URL_FLOW_RECORD = "/api/patient/flow_record";
    public static final String URL_STANDBY_NUMBER = "/api/patient/standby_number";
    public static final String URL_GET_NODE_BEACON = "/api/patient/app_node_beacon_get";
    public static final String URL_ROOM_LIST = "/api/patient/navigation_room_list";
    public static final String URL_BEACON_LIST = "/api/patient/beacon_list ";


    public static int mode; //1이면 진료동선안내, 2이면 길찾기
    public static boolean get = false;


    //beacon Value-------------------
    public static BeaconList BeaconList = new BeaconList();
    public static MinewBeaconManager mMinewBeaconManager;
    public static BeaconAdapter mAdapter;
    public static final int REQUEST_ENABLE_BT = 2;
    public static boolean isScanning = false;
    public static UserRssi comp = new UserRssi();
    public static int state;

    public static boolean recivedBeacon = true;
    public static boolean first = false;

    public void onCreate() {

        super.onCreate();
    }

    public void onTerminate() {

        super.onTerminate();
    }

}
