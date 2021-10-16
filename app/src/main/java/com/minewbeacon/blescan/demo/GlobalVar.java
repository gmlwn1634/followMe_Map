package com.minewbeacon.blescan.demo;

import android.app.Application;

import com.minew.beacon.MinewBeaconManager;

public class GlobalVar extends Application {

    //print MSG
    public static final String MSG_ROADING_PATH = "진료동선을 불러오고 있습니다.";
    public static final String MSG_DIE_PATH = "경로에서 멀어졌습니다.";
    public static final String MSG_RESEARCH_PATH = "경로를 재검색합니다.";
    public static final String MSG_EXIT_PATH = "경로 안내를 종료합니다.";
    public static final String MSG_REQUEST_PATH_FAILED = "서버에 진료동선 요청 실패";
    public static final String MSG_NO_PATH = "등록된 진료동선이 없습니다.";

    public static final String MSG_ARRIVED = "목적지 주변에 도착했습니다.";
    public static final String MSG_ARRIVED_FAILED = "목적지 도착 알림 실패";
    public static final String MSG_ARRIVED_SUCCESS = "목적지 도착 알림 성공";

    public static final String MSG_NO_BLUETOOTH = "해당 기기는 블루투스를 지원하지 않습니다.";
    public static final String MSG_REQUEST_BLUETOOTH = "블루투스를 활성화 해주세요.";


    public static final String MSG_JOIN_BLANK = "패스워드 공란";
    public static final String MSG_JOIN_PW_MISSMATCH = "패스워드 불일치";
    public static final String MSG_JOIN_PWFORM_MISSMATCH = "패스워드 형식 불일치";

    public static final String MSG_REQUEST_JOIN_SUCCESS = "회원가입 성공";
    public static final String MSG_REQUEST_JOIN_FAILED = "서버에 회원가입 요청 실패";

    public static final String MSG_REQUEST_LOGIN_SUCCESS = "로그인 성공";
    public static final String MSG_REQUEST_LOGIN_FAILED = "로그인 실패";

    public static final String MSG_REQUEST_LOGOUT_SUCCESS = "로그아웃 성공";
    public static final String MSG_REQUEST_LOGOUT_FAILED = "로그아웃 실패";

    public static final String MSG_REQUEST_STANDBY_NUMBER_SUCCESS = "서버에 대기순번 요청 성공";
    public static final String MSG_REQUEST_STANDBY_NUMBER_FAILED= "서버에 대기순번 요청 실패";

    public static final String MSG_REQUEST_CLINIC_SUCCESS = "서버에 진료내역 요청 성공";
    public static final String MSG_REQUEST_CLINIC_FAILED = "서버에 진료내역 요청 실패";


    public static final String MSG_REQUEST_PAYMENT_SUCCESS = "서버에 결제내역 요청 성공";
    public static final String MSG_REQUEST_PAYMENT_FAILED = "서버에 결제내역 요청 실패";


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
