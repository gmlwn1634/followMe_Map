package com.example.followme_map;

import android.app.Application;

public class GlobalVar extends Application {

    //activity
    public static final String TAG_ACTIVITY_JOIN = "JoinActivity";
    public static final String TAG_ACTIVITY_LOGIN = "LoginActivity";
    public static final String TAG_ACTIVITY_HOME = "HomeActivity";
    public static final String TAG_ACTIVITY_MAIN = "MainActivity";
    public static final String TAG_ACTIVITY_PAYMENT = "PaymentActivity";
    public static final String TAG_ACTIVITY_FLOW = "FlowActivity";

    //fragment
    public static final String TAG_FRAGMENT_HOME = "HomeFragment";
    public static final String TAG_FRAGMENT_INFO = "InfoFragment";
    public static final String TAG_FRAGMENT_LIST = "ListFragment";
    public static final String TAG_FRAGMENT_CLINIC = "ClinicListFragment";
    public static final String TAG_FRAGMENT_Payment = "PaymentFragment";

    //adapter
    public static final String TAG_ADAPTER_DEST = "DestAdapter";


    //    public static final String URL = "http://172.26.3.122:8000";
    public static final String URL = "http://49.143.18.165:8000";

    //Server URL
    public static final String URL_LOGIN = "/api/patient/login";
    public static final String URL_LOGOUT = "/api/patient/logout";
    public static final String URL_SIGNUP = "/api/patient/signup";
    public static final String URL_CLINIC = "/api/patient/clinic";
    public static final String URL_FLOW = "/api/patient/flow";
    public static final String URL_FLOW_NODE = "/api/patient/flow_node";
    public static final String URL_NAVIGATION = "/api/patient/navigation";
    public static final String URL_STORAGE = "/api/patient/storage";
    public static final String URL_STORAGE_RECORD = "/api/patient/storage_record";
    public static final String URL_FLOW_RECORD = "/api/patient/flow_record";
    public static final String URL_STANDBY_NUMBER = "/api/patient/standby_number";
    public static final String URL_GET_NODE_BEACON = "/api/patient/app_node_beacon_get";

    public void onCreate() {

        super.onCreate();
    }

    public void onTerminate() {

        super.onTerminate();
    }

}
