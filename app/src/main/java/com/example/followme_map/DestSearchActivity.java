package com.example.followme_map;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.followme_map.databinding.ActivityDestSearchBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.minew.beacon.BluetoothState;
import com.minew.beacon.MinewBeacon;
import com.minew.beacon.MinewBeaconManager;
import com.minew.beacon.MinewBeaconManagerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.maps.android.PolyUtil.distanceToLine;

public class DestSearchActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {


    private ActivityDestSearchBinding binding;


    //구글맵 Values---------------
    private GoogleMap mMap; //구글맵 오버레이
    private SupportMapFragment mapFragment;
    public static CameraPosition camPosition;
    private float zoomLevel = 25;
    //    private LatLng thisPoint = new LatLng(35.89676194374706, 128.62034143980088);
    private LatLng thisPoint = new LatLng(35.896672996764, 128.62037176654);
    private LatLng schoolPoint = new LatLng(35.89679977286669, 128.62092742557013);
    private LatLng startPoint, endPoint;
    private ArrayList<FlowNode> flowNodeList = new ArrayList<FlowNode>();
    private ArrayList<Flow> flowList = new ArrayList<Flow>();
    private JSONArray flowArr;
    private JSONArray nodeArr;

    private GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
    private GroundOverlay groundOverlay;
    private boolean overlayCheck = false;


    public static String selectedFloor;

    private Marker thisMarker;
    private boolean thisMarkerCheck = false;
    private Marker endMarker;
    private Marker startMarker;
    private boolean startMarkerCheck = false;
    private boolean endMarkerCheck = false;
    private Polyline polyline;
    private PolylineOptions polyOpt;
    private boolean polyCheck = false;

    //방위각 계산 Values-----------
    private SensorManager sm;
    private Sensor mAccelSensor; //가속도센서
    private Sensor mMagnetSensor; //지자기센서

    private final float[] mLastAccel = new float[3];
    private final float[] mLastMagnet = new float[3];

    private boolean mLastAccelSet = false;  //받아왔는지 확인
    private boolean mLastMagnetSet = false;

    private final float[] mR = new float[9]; //회전 매트릭스
    private final float[] mOrientation = new float[3];

    private float mAzimut; //mOrientation[0]
    private final static int AZIMUT_SIZE = 10; //평균 낼 데이터 갯수
    private float mAzimutArr[] = new float[AZIMUT_SIZE];
    private boolean azimutFull = false;
    private int index = 0;

    //Thread
//    boolean frag = true;
    private boolean startttt = false;
    private Thread changeTurnThread;
    private Thread setCameraThread;
    private Thread connectThread;
    private Thread setThisMarkerThread;
    private int overlayMap;

    //안내 재생
    private MediaPlayer mediaPlayer;


    //search
    public int selectedStartRoomPosition;
    public int selectedEndRoomPosition;

    ArrayList<String> roomNameList = new ArrayList<String>();
    ArrayList<Integer> roomNodeList = new ArrayList<Integer>();

    //1: 출발지-도착지
    //2: 현위치-도착지
    private int naviMode = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDestSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        GlobalVar.first = false;
        GlobalVar.BeaconList.setWGS_K_LatLng(0.0, 0.0);
        GlobalVar.recivedBeacon = true;
        GlobalVar.mode = 2; //출발지-도착지


        // Volley 통신 requestQueue 생성 및 초기화
        if (AppHelper.requestQueue != null)
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext());


        //방위각을 구하기 위한 센서 값 받기
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //가속도 센서
        mMagnetSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD); //지자기 센서


        //구글맵 오버레이를 위한 프레그먼트
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }//onAccuracyChanged


    // 현재 위치한 층에 따른 2,3층 맵 오버레이
    void mapOverlay() {
        switch (GlobalVar.BeaconList.getFloor()) {
            case "1":
                binding.floorSelector.check(binding.select2floor.getId());
                break;
            case "2":
                binding.floorSelector.check(binding.select3floor.getId());
                break;
        }
    } //mapOverlay()


    protected void getRoom() {


        String url = GlobalVar.URL + GlobalVar.URL_ROOM_LIST;
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray rooms = jsonObject.getJSONArray("room_list");
                            for (int i = 0; i < rooms.length(); i++) {
                                JSONObject room = rooms.getJSONObject(i);
                                roomNameList.add(room.getString("room_name"));
                                roomNodeList.add(room.getInt("room_node"));
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(DestSearchActivity.this, R.layout.support_simple_spinner_dropdown_item, roomNameList);
                            binding.startRoom.setAdapter(adapter);
                            binding.endRoom.setAdapter(adapter);


                            Log.i("main", "진료실 리스트" + rooms);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        e.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + LoginActivity.patientToken);
                return headers;
            }
        };


        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        //센서 업데이트 중지
//        sm.unregisterListener(this);
//    } //onPause()

    public void getAllBeacon() {


        //  2) 서버로부터 받은 비콘 정보를 비콘 리스트에 추가
        // 1층 왼쪽

        GlobalVar.BeaconList.add(new BeaconData("2", "1", "15001", 35.896697983871235, 128.6203462855552));
        GlobalVar.BeaconList.add(new BeaconData("2", "1", "15002", 35.89671482300457, 128.62042574599727));
        GlobalVar.BeaconList.add(new BeaconData("2", "1", "15003", 35.89674234433988, 128.62052931761377));
        GlobalVar.BeaconList.add(new BeaconData("2", "2", "15004", 35.896701975845126, 128.6203400822074));
        GlobalVar.BeaconList.add(new BeaconData("2", "2", "15005", 35.89671707059689, 128.62042146459254));
        GlobalVar.BeaconList.add(new BeaconData("2", "2", "15006", 35.89673472452264, 128.62050360724362));
        GlobalVar.BeaconList.add(new BeaconData("2", "2", "15008", 35.896749390857984, 128.62056999187578));
        GlobalVar.BeaconList.add(new BeaconData("2", "2", "15009", 35.896762699196934, 128.6206427467953));
        GlobalVar.BeaconList.add(new BeaconData("3", "2", "15010", 35.896780344554, 128.62064817263));
        GlobalVar.BeaconList.add(new BeaconData("3", "2", "15011", 35.896842540625, 128.62061430974));
        GlobalVar.BeaconList.add(new BeaconData("3", "2", "15012", 35.896848244192, 128.62064683152));
        GlobalVar.BeaconList.add(new BeaconData("3", "1", "15013", 35.896581540581, 128.62021097642));
        GlobalVar.BeaconList.add(new BeaconData("3", "1", "15014", 35.896596750143, 128.62029613655));
        GlobalVar.BeaconList.add(new BeaconData("3", "1", "15015", 35.896651613183, 128.62019085985));
        GlobalVar.BeaconList.add(new BeaconData("2", "1", "15016", 35.89665887361227, 128.6205622033809));

        GlobalVar.BeaconList.getList();


    } //getAllBeacon()

    private void initManager() {
        GlobalVar.mMinewBeaconManager = MinewBeaconManager.getInstance(this);
    } //initManager()

    private void initView(BeaconList BeaconList, GoogleMap GoogleMap) {
        GlobalVar.mAdapter = new BeaconAdapter(BeaconList, GoogleMap);
    } //initView()

    private void showBLEDialog() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, GlobalVar.REQUEST_ENABLE_BT);
    } //showBLEDialog()

    private void initListener() {
        System.out.println("들어왔니");
        if (GlobalVar.mMinewBeaconManager != null) {
            BluetoothState bluetoothState = GlobalVar.mMinewBeaconManager.checkBluetoothState();
            switch (bluetoothState) {
                case BluetoothStateNotSupported:
                    Toast.makeText(DestSearchActivity.this, "Not Support BLE", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case BluetoothStatePowerOff:
                    showBLEDialog();
                    return;
                case BluetoothStatePowerOn:
                    break;
            }
        }
        if (!GlobalVar.isScanning) {
            if (GlobalVar.mMinewBeaconManager != null) {
                GlobalVar.mMinewBeaconManager.startScan();
            }
        }

        GlobalVar.mMinewBeaconManager.setDeviceManagerDelegateListener(new MinewBeaconManagerListener() {
            /**
             *   if the manager find some new beacon, it will call back this method.
             *
             *  @param minewBeacons  new beacons the manager scanned
             */
            @Override
            public void onAppearBeacons(List<MinewBeacon> minewBeacons) {

            }

            /**
             *  if a beacon didn't update data in 10 seconds, we think this beacon is out of rang, the manager will call back this method.
             *
             *  @param minewBeacons beacons out of range
             */
            @Override
            public void onDisappearBeacons(List<MinewBeacon> minewBeacons) {
                /*for (MinewBeacon minewBeacon : minewBeacons) {
                    String deviceName = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue();
                    Toast.makeText(getApplicationContext(), deviceName + "  out range", Toast.LENGTH_SHORT).show();
                }*/
            }

            /**
             *  the manager calls back this method every 1 seconds, you can get all scanned beacons.
             *
             *  @param minewBeacons all scanned beacons
             */

            @Override
            public void onRangeBeacons(final List<MinewBeacon> minewBeacons) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.latDebug.setText(GlobalVar.BeaconList.getWGS_K_lat() + "");
                        binding.lngDebug.setText(GlobalVar.BeaconList.getWGS_K_lng() + "");

                        Collections.sort(minewBeacons, GlobalVar.comp);
                        if (GlobalVar.state == 1 || GlobalVar.state == 2) {
                        } else {
                            if (GlobalVar.recivedBeacon) {
                                GlobalVar.mAdapter.setItems(minewBeacons);
                                if (!GlobalVar.first) {
                                    if (GlobalVar.BeaconList.getWGS_K_lat() != 0.0) {
                                        System.out.println("들어왔니3");
                                        GlobalVar.first = true;
                                        //출발지-목적지 스피너
                                        mapOverlay();
                                    }
                                }
                            }

                        }
                    }
                });
            }

            /**
             *  the manager calls back this method when BluetoothStateChanged.
             *
             *  @param state BluetoothState
             */
            @Override
            public void onUpdateState(BluetoothState state) {
                switch (state) {
                    case BluetoothStatePowerOn:
                        Toast.makeText(getApplicationContext(), "BluetoothStatePowerOn", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothStatePowerOff:
                        Toast.makeText(getApplicationContext(), "BluetoothStatePowerOff", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    } //initListener()


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //1. 카메라 포지션
        camPosition = new CameraPosition.Builder().target(schoolPoint).zoom(18.5f).bearing(-14.7f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));


        // 전체 비콘 정보를 받아옴
        if (!GlobalVar.get) {
            getAllBeacon();
            GlobalVar.get = true;
        }

        //스피너
        getRoom();
        
        // 현위치 얻기
        initView(GlobalVar.BeaconList, mMap); //어댑터 생성
        initManager(); //싱글톤 패턴
        initListener(); //비콘의 신호 수신


        //층선택기 값 바뀌면 새로 그리기
        binding.floorSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                drawPolyline();
                if (overlayCheck) {
                    groundOverlay.remove();
                    overlayCheck = false;
                }
                if (binding.floorSelector.getCheckedRadioButtonId() == binding.select2floor.getId()) {
                    groundOverlayOptions = groundOverlayOptions.image(BitmapDescriptorFactory.fromResource(R.drawable.map_2th_floor))
                            .positionFromBounds(new LatLngBounds(new LatLng(35.89651393057683, 128.6201298818298), new LatLng(35.89707923321034, 128.62176975983763)));
                    selectedFloor = "1";
                } else if (binding.floorSelector.getCheckedRadioButtonId() == binding.select3floor.getId()) {
                    groundOverlayOptions = groundOverlayOptions.image(BitmapDescriptorFactory.fromResource(R.drawable.map_3th_floor))
                            .positionFromBounds(new LatLngBounds(new LatLng(35.89651393057683, 128.6201298818298), new LatLng(35.89707923321034, 128.62176975983763)));
                    selectedFloor = "2";
                }
                groundOverlay = mMap.addGroundOverlay(groundOverlayOptions);
                overlayCheck = true;
            }
        });


        //출발지 선택 리스너
        binding.startRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStartRoomPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //도착지 선택 리스너
        binding.endRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedEndRoomPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        //출발지-도착지 경로 표시
        binding.showFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFlowNode(); //경로표시
            }
        });

        //현위치-도착지 일 때는 네비게이션 기능 작동
        binding.thisPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.thisP.setVisibility(View.VISIBLE);
                binding.cancel.setVisibility(View.VISIBLE);
                binding.startRoom.setVisibility(View.INVISIBLE);
                naviMode = 2;
            }
        });

        //사용자설정 출발지-도착지 일 때는 네비게이션 x
        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.thisP.setVisibility(View.INVISIBLE);
                binding.cancel.setVisibility(View.INVISIBLE);
                binding.startRoom.setVisibility(View.VISIBLE);
                naviMode = 1;
            }
        });


//        binding.naviStart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                binding.naviSelector.setVisibility(View.INVISIBLE);
//                binding.floorSelector.setVisibility(View.INVISIBLE);
//                binding.naviStart.setVisibility(View.INVISIBLE);
//
//
//                // 경로 안내 시작 음성
//                mediaPlayer = MediaPlayer.create(DestSearchActivity.this, R.raw.flow_start_sound);
//                mediaPlayer.start();
//
//
//                if (startMarkerCheck) {
//                    startMarker.remove();
//                    startMarkerCheck = false;
//                }
//                if (endMarkerCheck) {
//                    endMarker.remove();
//                    endMarkerCheck = false;
//                }
//
//                naviStart(); //
//            }
//        });
    }


    //진료동선 표시
    void drawPolyline() {
        //null 방지   ////////////오늘 여기까지 했다!!!!

        Log.i("draw", "들어옴");


        if (polyCheck) {
            polyline.remove();
            polyCheck = false;
        }


        polyOpt = new PolylineOptions();
        polyOpt.startCap(new RoundCap());
        polyOpt.endCap(new RoundCap());
        polyOpt.width(25f);


        for (int i = 0; i < flowNodeList.size(); i++) {
            if (binding.floorSelector.getCheckedRadioButtonId() == binding.select2floor.getId()) {
                if (flowNodeList.get(i).getFloor() == 1) {
                    polyOpt.add(flowNodeList.get(i).getLatLng());
                    Log.i("draw", "들어옴2");
                    if (i == 0) {
//                        startMarker = mMap.addMarker(new MarkerOptions().position(startPoint).title("출발지").icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
//                        startMarkerCheck = true;
                    }
                    if (i == flowNodeList.size() - 1) {
//                        endMarker = mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));
//                        endMarkerCheck = true;
                    }
                }
            }
            if (binding.floorSelector.getCheckedRadioButtonId() == binding.select3floor.getId()) {
                if (flowNodeList.get(i).getFloor() == 2) {
                    polyOpt.add(flowNodeList.get(i).getLatLng());
                    Log.i("draw", "들어옴3");
                    if (i == 0) {
//                        startMarker = mMap.addMarker(new MarkerOptions().position(startPoint).title("출발지").icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
//                        startMarkerCheck = true;
                    }
                    if (i == flowNodeList.size() - 1) {
//                        endMarker = mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));
//                        endMarkerCheck = true;
                    }
                }
            }

        }


        polyline = mMap.addPolyline(polyOpt);
        polyCheck = true;

    } //drawPolyline()

    //    //동선의 노드 표시
    public void getFlowNode() {

        Log.i("draw", "getFlowNode mode=" + naviMode);

        if (naviMode == 1) { //출발지 - 도착지
            if (roomNodeList.get(selectedStartRoomPosition) >= 3000)
                binding.floorSelector.check(binding.select3floor.getId());
            else if (roomNodeList.get(selectedStartRoomPosition) >= 2000)
                binding.floorSelector.check(binding.select2floor.getId());
        } else if (naviMode == 2) { //현위치 - 도착지
            if (GlobalVar.BeaconList.getFloor().equals("1"))
                binding.floorSelector.check(binding.select2floor.getId());
            else if (GlobalVar.BeaconList.getFloor().equals("2"))
                binding.floorSelector.check(binding.select3floor.getId());

        }


        String url = null;
        if (naviMode == 1) {
            binding.naviStart.setVisibility(View.INVISIBLE);
            url = GlobalVar.URL + GlobalVar.URL_NAVIGATION;
        } else if (naviMode == 2) {
            binding.naviStart.setVisibility(View.VISIBLE);
            url = GlobalVar.URL + GlobalVar.URL_NAVIGATION_CURRUNT;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {

                            flowNodeList.clear();
                            JSONObject jsonResponse = new JSONObject(response);
                            Log.i("dest", "response" + response);


                            nodeArr = jsonResponse.getJSONArray("nodeFlow"); //첫번째 동선의 노드

                            Log.i("dest", "nodeArr 노드 목록 : " + nodeArr);

                            for (int i = 0; i < nodeArr.length(); i++) {
                                JSONObject nodeObj = nodeArr.getJSONObject(i);
                                FlowNode flowNode = new FlowNode();
                                flowNode.setIndex(i);
                                flowNode.setId(nodeObj.getInt("node_id"));
                                flowNode.setFloor(nodeObj.getInt("floor"));
                                flowNode.setLatLng(nodeObj.getDouble("lat"), nodeObj.getDouble("lng"));
                                flowNode.setStairCheck(nodeObj.getInt("stair_check"));
                                flowNodeList.add(flowNode);
                            }

                            startPoint = flowNodeList.get(0).getLatLng();
                            endPoint = flowNodeList.get(flowNodeList.size() - 1).getLatLng();
                            drawPolyline();
                            Log.i("dest", "동선 수신 완료");

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "서버에 진료동선 요청 실패" + e.getMessage());
                        }


                    }
                },
                new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        e.printStackTrace();
                        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "서버에 진료동선 요청 실패" + e.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                if (naviMode == 1) {
                    params.put("start_room", roomNameList.get(selectedStartRoomPosition) + "");
                    params.put("end_room", roomNameList.get(selectedEndRoomPosition) + "");
                } else if (naviMode == 2) {
                    params.put("lat", GlobalVar.BeaconList.getWGS_K_lat() + "");
                    params.put("lng", GlobalVar.BeaconList.getWGS_K_lng() + "");
                    params.put("major", GlobalVar.BeaconList.getFloor());
                    params.put("end_room", roomNameList.get(selectedEndRoomPosition) + "");
                }


                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + LoginActivity.patientToken);
                return headers;
            }


        };


        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);


    } //getFlowNode()


}
