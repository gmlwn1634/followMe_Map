package com.minewbeacon.blescan.demo;


import static com.google.maps.android.PolyUtil.distanceToLine;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.minew.beacon.BluetoothState;
import com.minew.beacon.MinewBeacon;
import com.minew.beacon.MinewBeaconManager;
import com.minew.beacon.MinewBeaconManagerListener;
import com.yuliwuli.blescan.demo.R;
import com.yuliwuli.blescan.demo.databinding.ActivityFlowBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


public class FlowActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

    private ActivityFlowBinding binding;

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
    private static final float[] mAzimutArr = new float[AZIMUT_SIZE];
    private static boolean azimutFull = false;
    private static int index = 0;

    //google map Value---------------
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    public static CameraPosition camPosition;
    private final LatLng schoolPoint = new LatLng(35.89672118272869, 128.62047684589012);
    private GroundOverlay groundOverlay;
    private GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
    private boolean overlayCheck = false;


    //recyclerView Value-------------
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<DestInfo> destInfoArrayList = new ArrayList<>();

    //동선, 노드
    public static final ArrayList<FlowNode> flowNodeList = new ArrayList<FlowNode>();
    private final ArrayList<Flow> flowList = new ArrayList<Flow>();
    private JSONArray flowArr;
    private JSONArray nodeArr;

    private Polyline polyline;
    private PolylineOptions polyOpt;
    private boolean polyCheck = false;
    private LatLng startPoint, endPoint;
    private Marker endMarker;
    private Marker startMarker;
    private boolean startMarkerCheck = false;
    private boolean endMarkerCheck = false;
    boolean flag = true;
    FlowNode nearNode;
    Dist nearDist;
    public static String selectedFloor;

    //안내재생
    private MediaPlayer mediaPlayer;

    boolean polyStart_This = false; //현위치와 출발지 연결
    public static boolean naviStartCheck = false;

    public static RadioGroup floorSelector;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFlowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GlobalVar.first = false;
        GlobalVar.BeaconList.setWGS_K_LatLng(0.0, 0.0);
        GlobalVar.recivedBeacon = true;
        GlobalVar.mode = 1; //진료동선 안내

        // Volley 통신 requestQueue 생성 및 초기화
        if (AppHelper.requestQueue != null)
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext());

        //방위각을 구하기 위한 센서 값 받기
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //가속도 센서
        mMagnetSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD); //지자기 센서

        //recyclerView
        binding.recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(mLayoutManager);

        //구글맵 오버레이를 위한 프레그먼트
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.INVISIBLE);

        floorSelector = binding.floorSelector;

    } //onCreate()


    //좌표 간 거리 계산 - meter
    private double getDistanceMeter(LatLng latLng1, LatLng latLng2) {

        double lat1 = latLng1.latitude;

        double lng1 = latLng1.longitude;
        double lat2 = latLng2.latitude;
        double lng2 = latLng2.longitude;


        double theta = lng1 - lng2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));

        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515 * 1609.344;
        Log.i("얼마차이", dist + "");
        return dist;

    } //getDistanceMeter()


    //좌표 간 거리 계산
    private double getDistance(LatLng a, LatLng b) {

        double lat1 = a.latitude;
        double lng1 = a.longitude;
        double lat2 = b.latitude;
        double lng2 = b.longitude;

        double distance = Math.pow(Math.sin(Math.toRadians(lat1 - lat2) / 2), 2.0)
                + Math.pow(Math.sin(Math.toRadians(lng1 - lng2) / 2), 2.0)
                * Math.cos(Math.toRadians(lat2))
                * Math.cos(Math.toRadians(lat1));

        return Math.toDegrees(distance);


    } //getDistance()


    //경로이탈 감지
    void checkOffLoad() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //경로 이탈 - 현위치와 현위치에서 가장 가까운 노드
                        if (getDistanceMeter(GlobalVar.BeaconList.getWGS_K_LatLng(), nearNode.getLatLng()) >= 14) { //test
                            flag = false;
                            //이탈이 감지되면 쓰레드 동작 멈춤


                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "이탈이탈");

                            //네비판 감추기
                            binding.navigation.setVisibility(View.INVISIBLE);

                            // 알림뜨고 메시지 재생
                            mediaPlayer = MediaPlayer.create(FlowActivity.this, R.raw.off_road_sound);
                            mediaPlayer.start();

                            getNFlowNode(0);//경로 재탐색
                            // 안내 메세지
                            CustomDialog customDialog = new CustomDialog(FlowActivity.this, new CustomDialogClickListener() {
                                @Override
                                public void onPositiveClick() {
                                    flag = true;

                                }
                            }, GlobalVar.MSG_DIE_PATH,GlobalVar.MSG_RESEARCH_PATH);

                            customDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                            customDialog.setCancelable(false);
                            customDialog.show();
                        }
                    }
                });
            }
        }).start();
    }//checkOffLoad()

    void naviStart() {
        binding.menu.setVisibility(View.VISIBLE);
        binding.constraintLayout7.setBackgroundColor(Color.BLACK);
        binding.textView10.setText("현위치 → " + destInfoArrayList.get(destInfoArrayList.size() - 1).to);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        if (flag) {

                            checkNearNode(); //가장 가까운 노드 확인
                            checkNearDist(); //가장 가까운 길 확인
                            checkFloor(); //층 바뀌면 도면 전환
                            if (nearNode != null) {
                                checkOffLoad(); //경로이탈 감지
                                changeTurn(); // 방향회전 안내
                            }
                            if (nearDist != null) {
                                setCameraPosition(); //지도 방향
                            }
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    } //naviStart()

    void checkNearNode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        try {

                            ArrayList<FlowNode> nodeList = new ArrayList<FlowNode>();


                            //현재 층의 노드 중 현위치와 가장 가까운 노드 계산
                            for (int i = 0; i < flowNodeList.size(); i++) {
                                final double R = 6372.8 * 1000;
                                //같은 층의 노드인지 판단
                                if (flowNodeList.get(i).getFloor() == Integer.parseInt(GlobalVar.BeaconList.getFloor())) {
                                    double a = getDistance(GlobalVar.BeaconList.getWGS_K_LatLng(), flowNodeList.get(i).getLatLng());
                                    double c = 2 * Math.asin(a);
                                    double dist = R * c;
                                    FlowNode fn = new FlowNode();
                                    fn.setFloor(flowNodeList.get(i).getFloor());
                                    fn.setLatLng(flowNodeList.get(i).getLatLng());
                                    fn.setDist(dist);
                                    fn.setIndex(i);

                                    nodeList.add(fn);
                                }
                            }


                            Iterator<FlowNode> it = nodeList.iterator();
                            FlowNode e = it.next();//hasNext를 해서 다음요소가 있는지 확인해야지만, it.next해서 그 요소를 불러올 수가 있다.
                            nearNode = e;  //가장가까운노드
                            for (int i = 0; i < nodeList.size(); i++) {
                                if (it.hasNext()) {//pc(프로그램카운터가 이동하여, 다음 요소가 있는지 확인)
                                    if (e.getDist() < nearNode.getDist()) {
                                        nearNode = e;
                                    }
                                    e = it.next();//hasNext를 해서 다음요소가 있는지 확인해야지만, it.next해서 그 요소를 불러올 수가 있다.
                                }
                            }


                        } catch (NoSuchElementException e) {
                            return;
                        }
                    }
                });
            }
        }).start();
    }//checkNearNode()


    void checkFloor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //현재 층에 따른 도면 바꾸기
                        if (GlobalVar.BeaconList.getFloor().equals("1")) {
                            floorSelector.check(binding.select2floor.getId());
                        } else if (GlobalVar.BeaconList.getFloor().equals("2")) {
                            floorSelector.check(binding.select3floor.getId());
                        }
                    }
                });
            }
        }).start();
    }//checkFloor()

    void arrived() {

        // 도착안내
        GlobalVar.recivedBeacon = false;
//        mMinewBeaconManager.stopScan();
        mediaPlayer = MediaPlayer.create(FlowActivity.this, R.raw.arrival_sound);
        mediaPlayer.start();
        binding.turn.setText("목적지 도착");
        binding.turnImg.setImageResource(R.drawable.arrive);
        GlobalVar.isScanning = false;


        // 안내 메세지
        CustomDialog customDialog = new CustomDialog(FlowActivity.this, new CustomDialogClickListener() {
            @Override
            public void onPositiveClick() {
                String url = GlobalVar.URL + GlobalVar.URL_FLOW_END;
                StringRequest request = new StringRequest(
                        Request.Method.GET,
                        url,
                        new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    Log.i(GlobalVar.TAG_ACTIVITY_FLOW, GlobalVar.MSG_ARRIVED_SUCCESS);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.i(GlobalVar.TAG_ACTIVITY_FLOW, GlobalVar.MSG_ARRIVED_FAILED + e.getMessage());

                                }

                            }
                        },
                        new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                            @Override
                            public void onErrorResponse(VolleyError e) {
                                e.printStackTrace();
                                Log.i(GlobalVar.TAG_ACTIVITY_FLOW, GlobalVar.MSG_ARRIVED_FAILED + e.getMessage());
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
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
                AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 초기화 필수
                AppHelper.requestQueue.add(request);


                Intent intent = new Intent(FlowActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();

            }


        }, GlobalVar.MSG_ARRIVED, GlobalVar.MSG_EXIT_PATH);
        customDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        customDialog.setCancelable(false);
        customDialog.show();

    }//arrived()


    void changeTurn() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        LatLng nodeA, nodeB, nodeC;
                        nodeA = nearNode.getLatLng();
                        if ((nearNode.getIndex() + 2) >= flowNodeList.size()) {
                            flag = false;
//                            //도착
                            arrived();
                            return;
                        }

                        if ((nearNode.getIndex() + 3) >= flowNodeList.size()) {
                            return;
                        } else {
                            nodeB = flowNodeList.get(nearNode.getIndex() + 3).getLatLng();
                        }

                        if ((nearNode.getIndex() + 4) >= flowNodeList.size()) {
                            return;
                        } else {
                            nodeC = flowNodeList.get(nearNode.getIndex() + 4).getLatLng();
                        }


//                        double dist = getDistanceMeter(BeaconAdapter.thisMarker.getPosition(), nodeB); //test
//                        dist = Math.round(dist * 1000) / 1000.0;

                        //좌우판단
//                        if (dist < 4) {
//                            binding.distance.setText(dist + "m 남음");
                        switch (ccw(nodeA, nodeB, nodeC)) {

                            //이미지 변경
                            case 1:
                                binding.navigation.setVisibility(View.VISIBLE);
                                binding.turn.setText("좌회전");
                                binding.turnImg.setImageResource(R.drawable.turn_left2);
                                break;
                            case -1:
                                binding.navigation.setVisibility(View.VISIBLE);
                                binding.turn.setText("우회전");
                                binding.turnImg.setImageResource(R.drawable.turn_right2);
                                break;
                            case 0:
                                binding.navigation.setVisibility(View.INVISIBLE);
                                break;
                        }
//                        }
//                        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "남은 거리 : " + dist);


                    }
                });
            }
        }).start();
    } //changeTurn()


    //좌회전 우회전 판단
    int ccw(LatLng nodeA, LatLng nodeB, LatLng nodeC) {

        double x1 = nodeA.latitude;
        double y1 = nodeA.longitude;
        double x2 = nodeB.latitude;
        double y2 = nodeB.longitude;
        double x3 = nodeC.latitude;
        double y3 = nodeC.longitude;


        if (nearNode.getFloor() != flowNodeList.get(nearNode.getIndex() + 3).getFloor()) {
            return 0; //직진
        } else if (nearNode.getFloor() != flowNodeList.get(nearNode.getIndex() + 4).getFloor()) {
            return 0; //직진
        }

        double temp1 = (y2 - y1) * (x3 - x1) + y1 * (x2 - x1);
        double temp2 = (x2 - x1) * y3;

        double angle = getAngle(nodeA, nodeB, nodeC);


        if (temp1 < temp2) {
            if (angle <= 18) {
                Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "직진");
                return 0;
            }
            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "< 우회전");
            return -1;
        } else if (temp1 > temp2) { //시계
            if (angle <= 18) {
                Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "직진");
                return 0;
            }
            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "< 좌회전");
            return 1;
        } else  //직진
            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "직진");

        return 0;


    } //ccw()

    //좌회전 우회전 판단
    double getAngle(LatLng NodeA, LatLng NodeB, LatLng NodeC) {

        double dx1 = NodeB.latitude - NodeA.latitude;
        double dy1 = NodeB.longitude - NodeA.longitude;
        double dx2 = NodeC.latitude - NodeB.latitude;
        double dy2 = NodeC.longitude - NodeB.longitude;
        double d = dx1 * dx2 + dy1 * dy2;
        double l2 = (dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2);
        double angle = Math.acos(d / Math.sqrt(l2)); //삼각형 내적각

        return Math.toDegrees(angle);
    } //getAngle()


    // cameraPosition 업데이트
    void setCameraPosition() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        float mapBearing = getBearing(flowNodeList.get(nearDist.getIndex()).getLatLng(), flowNodeList.get(nearDist.getIndex() + 1).getLatLng());
                        float markerBearing = BeaconAdapter.thisMarker.getRotation();

                        Log.i("방위각", "  맵:" + mapBearing);
                        Log.i("방위각", "마커: " + markerBearing);

//                        if (Math.abs(BeaconAdapter.thisMarker.getRotation() - mapBearing) >= 90) {
//                            mapBearing += 180;
//                            if (mapBearing >= 360) mapBearing -= 360;
//                        }

                        camPosition = new CameraPosition.Builder(camPosition).zoom(25).target(GlobalVar.BeaconList.getWGS_K_LatLng()).bearing(mapBearing).build();
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));


                    }
                });
            }
        }).start();

    } //setCameraPosition()

    //지도가 준비되었을 때
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //1. 카메라 포지션
        camPosition = new CameraPosition.Builder().target(schoolPoint).zoom(19.7f).bearing(-14.7f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));


        //현위치 계산
        // 전체 비콘 정보를 받아옴
        if (!GlobalVar.get) {
            getAllBeacon();
            GlobalVar.get = true;
        }

        //3.2 현위치 얻기


        initView(GlobalVar.BeaconList, mMap); //어댑터 생성
        initManager(); //싱글톤 패턴
        initListener(); //비콘의 신호 수신

        //2. 구글맵 오버레이
        // 본관 좌표를 기준으로 구글맵에 도면 오버레이


        //현위치 계산
        //현재 층에 따른 구글맵 오버레이


        //층선택기 값 바뀌면 새로 그리기
        floorSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                drawPolyline();
                if (overlayCheck) {
                    groundOverlay.remove();
                    overlayCheck = false;
                }
                if (floorSelector.getCheckedRadioButtonId() == binding.select2floor.getId()) {
                    groundOverlayOptions = groundOverlayOptions.image(BitmapDescriptorFactory.fromResource(R.drawable.map_2th_floor))
                            .positionFromBounds(new LatLngBounds(new LatLng(35.89651393057683, 128.6201298818298), new LatLng(35.89707923321034, 128.62176975983763)));
                    selectedFloor = "1";

                } else if (floorSelector.getCheckedRadioButtonId() == binding.select3floor.getId()) {
                    groundOverlayOptions = groundOverlayOptions.image(BitmapDescriptorFactory.fromResource(R.drawable.map_3th_floor))
                            .positionFromBounds(new LatLngBounds(new LatLng(35.89651393057683, 128.6201298818298), new LatLng(35.89707923321034, 128.62176975983763)));
                    selectedFloor = "2";
                }

                groundOverlay = mMap.addGroundOverlay(groundOverlayOptions);
                overlayCheck = true;
            }
        });

        //안내시작 버튼을 누르면 첫번째 동선만 보여준다.
        binding.naviStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.naviStart.setVisibility(View.INVISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
                binding.startEndImg.setVisibility(View.GONE);
                floorSelector.setVisibility(View.GONE);


                // 경로 안내 시작 음성
                mediaPlayer = MediaPlayer.create(FlowActivity.this, R.raw.flow_start_sound);
                mediaPlayer.start();

                if (polyCheck) {
                    polyline.remove();
                    polyCheck = false;
                }
//                if (startMarkerCheck) {
//                    startMarker.remove();
//                    startMarkerCheck = false;
//                }
//                if (endMarkerCheck) {
//                    endMarker.remove();
//                    endMarkerCheck = false;
//                }

                polyStart_This = true;
                naviStartCheck = true;
                getNFlowNode(0); //첫번째 동선 표시
                naviStart(); //


            }
        });


    } //onMapReady()


    //현 위치에서 가장 가까운 길 찾기
    void checkNearDist() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        try {

                            ArrayList<Dist> distList = new ArrayList<Dist>();
                            for (int i = 0; i < flowNodeList.size() - 1; i++) {
                                Dist dist = new Dist();
                                dist.setDist(distanceToLine(GlobalVar.BeaconList.getWGS_K_LatLng(), flowNodeList.get(i).getLatLng(), flowNodeList.get(i + 1).getLatLng()));
                                dist.setIndex(i);
                                distList.add(dist);
                            }

                            Iterator<Dist> it = distList.iterator();
                            Dist e = it.next();//hasNext를 해서 다음요소가 있는지 확인해야지만, it.next해서 그 요소를 불러올 수가 있다.
                            nearDist = e;  //가장가까운노드
                            for (int i = 0; i < distList.size(); i++) {
                                if (it.hasNext()) {//pc(프로그램카운터가 이동하여, 다음 요소가 있는지 확인)
                                    if (e.getDist() < nearDist.getDist()) {
                                        nearDist = e;
                                    }
                                    e = it.next();//hasNext를 해서 다음요소가 있는지 확인해야지만, it.next해서 그 요소를 불러올 수가 있다.
                                }
                            }

                        } catch (NoSuchElementException e) {
                            return;
                        }
                    }
                });
            }
        }).start();
    }//checkNearDist()


    //진료동선 표시
    void drawPolyline() {
        //null 방지   ////////////오늘 여기까지 했다!!!!


        if (polyCheck) {
            polyline.remove();
            polyCheck = false;
        }
//        if (startMarkerCheck) {
//            startMarker.remove();
//            startMarkerCheck = false;
//        }
//        if (endMarkerCheck) {
//            endMarker.remove();
//            endMarkerCheck = false;
//        }


        polyOpt = new PolylineOptions();
        polyOpt.color(0xFFFF6347);
        polyOpt.startCap(new RoundCap());
        polyOpt.endCap(new RoundCap());
        polyOpt.width(27f);


        for (int i = 0; i < flowNodeList.size(); i++) {
            if (polyStart_This) { //현위치-출발지 점선으로 연결
                List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20));
                PolylineOptions startPoly = new PolylineOptions();
                startPoly.startCap(new RoundCap());
                startPoly.endCap(new RoundCap());
                startPoly.width(15f);
                startPoly.color(0xFFFF6347);
                startPoly.pattern(pattern);
                startPoly.add(GlobalVar.BeaconList.getWGS_K_LatLng()); //test
                startPoly.add(flowNodeList.get(0).getLatLng());
                mMap.addPolyline(startPoly);
                polyStart_This = false;
            }
            if (floorSelector.getCheckedRadioButtonId() == binding.select2floor.getId()) {
                if (flowNodeList.get(i).getFloor() == 1) {
                    polyOpt.add(flowNodeList.get(i).getLatLng());

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
            if (floorSelector.getCheckedRadioButtonId() == binding.select3floor.getId()) {
                if (flowNodeList.get(i).getFloor() == 2) {
                    polyOpt.add(flowNodeList.get(i).getLatLng());
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

    void mapOverlay() {
        switch (GlobalVar.BeaconList.getFloor()) {
            case "1":
                floorSelector.check(binding.select2floor.getId());
                break;
            case "2":
                floorSelector.check(binding.select3floor.getId());
                break;
        }
    } //mapOverlay()

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
        if (GlobalVar.mMinewBeaconManager != null) {
            BluetoothState bluetoothState = GlobalVar.mMinewBeaconManager.checkBluetoothState();
            switch (bluetoothState) {
                case BluetoothStateNotSupported:
                    Toast.makeText(FlowActivity.this, "Not Support BLE", Toast.LENGTH_SHORT).show();
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

        //
//        if (GlobalVar.isScanning) {
//            GlobalVar.isScanning = false;
//
//            if (GlobalVar.mMinewBeaconManager != null) {
//                GlobalVar.mMinewBeaconManager.stopScan();
//            }
//        } else {
//            GlobalVar.isScanning = true;
//
//            try {
//                GlobalVar.mMinewBeaconManager.startScan();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
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

                        Collections.sort(minewBeacons, GlobalVar.comp);
                        if (GlobalVar.state == 1 || GlobalVar.state == 2) {
                        } else {
                            if (GlobalVar.recivedBeacon) {
                                GlobalVar.mAdapter.setItems(minewBeacons);
                                if (!GlobalVar.first) {
                                    if (GlobalVar.BeaconList.getWGS_K_lat() != 0.0) {
                                        GlobalVar.first = true;
                                        mapOverlay();
                                        getAllFlow();
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

//        String url = GlobalVar.URL + GlobalVar.URL_BEACON_LIST;
//        StringRequest request = new StringRequest(
//                Request.Method.GET,
//                url,
//                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//
//                            JSONObject jsonResponse = new JSONObject(response);
//
//                            JSONArray beaconList = jsonResponse.getJSONArray("beacon_list");
//
//                            for (int i = 0; i < beaconList.length(); i++) {
//                                JSONObject beacon = beaconList.getJSONObject(i);
//                                String group = String.valueOf(beacon.getInt("group"));
//                                String major = String.valueOf(beacon.getInt("major"));
//                                String minor = String.valueOf(beacon.getInt("beacon_id_minor"));
//                                double lat = beacon.getDouble("lat");
//                                double lng = beacon.getDouble("lng");
//                                        " 좌표:" + lat + ", " + lng);
//
//                                BeaconList.add(new BeaconData(group, major, minor, lat, lng));
//
//                            }
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "비콘 정보 요청 실패" + e.getMessage());
//                            Toast.makeText(getApplicationContext(), "등록된 비콘이 없습니다.", Toast.LENGTH_SHORT).show();
//                            finish();
//
//                        }
//                    }
//                },
//                new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
//                    @Override
//                    public void onErrorResponse(VolleyError e) {
//                        e.printStackTrace();
//                        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "비콘 정보 요청 실패" + e.getMessage());
//                        Toast.makeText(getApplicationContext(), "등록된 비콘이 없습니다.", Toast.LENGTH_SHORT).show();
//                        finish();
//                    }
//                }
//        ) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<String, String>();
//                headers.put("Authorization", "Bearer " + LoginActivity.patientToken);
//                return headers;
//            }
//
//        };
//
//
//        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
//        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
//        AppHelper.requestQueue.add(request);

        GlobalVar.BeaconList.getList();


    } //getAllBeacon()

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GlobalVar.REQUEST_ENABLE_BT:
                break;
        }
    } //onActivityResult()

    public void onBackPressed() {
        flag = false;
        finish();
    } //onBackPressed()

    //전체 동선 및 첫번째 동선 가져오기
    public void getAllFlow() {
        String url = GlobalVar.URL + GlobalVar.URL_FLOW;
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {

                            flowList.clear();
                            flowNodeList.clear();

                            JSONObject jsonResponse = new JSONObject(response);

                            Log.i("진료동선", jsonResponse.toString());

                            flowArr = jsonResponse.getJSONArray("flow_list"); //전체 동선
                            nodeArr = jsonResponse.getJSONArray("nodeFlow"); //첫번째 동선

                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "전체 동선 목록 표시 OK" + response);
                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "flowARR 동선 목록 : " + flowArr);
                            for (int i = 0; i < flowArr.length(); i++) {
                                JSONObject flowObj = flowArr.getJSONObject(i);
                                Log.i(GlobalVar.TAG_ACTIVITY_FLOW, flowObj.toString());
                                Flow flow = new Flow();
                                if (i == 0) {
                                    JSONObject roomLocation = flowObj.getJSONObject("room_location");
                                    flow.setRoomName("출발지");
                                    flow.setRoomNode(roomLocation.getInt("node_id"));
                                } else {
                                    flow.setFlowId(flowObj.getInt("flow_id"));
                                    flow.setPatientId(flowObj.getInt("patient_id"));
                                    flow.setFlowSequence(flowObj.getInt("flow_sequence"));
                                    flow.setFlowStatus(flowObj.getInt("flow_status_check"));
                                    JSONObject roomObj = flowObj.getJSONObject("room_location");
                                    flow.setRoomLocationID(roomObj.getInt("room_location_id"));
                                    flow.setRoomNode(roomObj.getInt("room_node"));
                                    flow.setRoomName(roomObj.getString("room_name"));

                                }

                                flowList.add(flow);

                            }

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


                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "nodeList" + flowNodeList.toString());


                            //전체 목록 표시
                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "length" + flowList.size());
                            for (int i = 0; i < flowList.size() - 1; i++)
                                destInfoArrayList.add(new DestInfo((i + 1) + "", flowList.get(i).getRoomName(), flowList.get(i + 1).getRoomName()));


                            DestAdapter destAdapter = new DestAdapter(FlowActivity.this, destInfoArrayList);
                            binding.recyclerView.setAdapter(destAdapter);



                            //첫번째 동선 표시
                            startPoint = flowNodeList.get(0).getLatLng();
                            endPoint = flowNodeList.get(flowNodeList.size() - 1).getLatLng();
                            drawPolyline();


                            if (mapFragment.getView() != null) {
                                mapFragment.getView().setVisibility(View.VISIBLE);
                            }
                            binding.ready.setVisibility(View.INVISIBLE);
                            binding.recyclerView.setVisibility(View.VISIBLE);
                            binding.startEndImg.setVisibility(View.VISIBLE);
                            floorSelector.setVisibility(View.VISIBLE);
                            binding.naviStart.setVisibility(View.VISIBLE);



                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, GlobalVar.MSG_REQUEST_PATH_FAILED + e.getMessage());
                            Toast.makeText(getApplicationContext(), GlobalVar.MSG_NO_PATH , Toast.LENGTH_SHORT).show();
                            finish();

                        }
                    }
                },
                new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        e.printStackTrace();
                        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, GlobalVar.MSG_REQUEST_PATH_FAILED +e.getMessage());
                        Toast.makeText(getApplicationContext(), GlobalVar.MSG_NO_PATH, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("lat", GlobalVar.BeaconList.getWGS_K_lat() + "");
                params.put("lng", GlobalVar.BeaconList.getWGS_K_lng() + "");
                params.put("major", GlobalVar.BeaconList.getFloor()); //층번호
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


    } //getAllFlow()

    //동선의 노드 표시
    public void getNFlowNode(final int n) {

        final String startRoomNode = flowList.get(n).getRoomNode() + "";
        final String endRoomNode = flowList.get(n + 1).getRoomNode() + "";

        if (flowList.get(n).getRoomNode() >= 3000)
            floorSelector.check(binding.select3floor.getId());
        else if (flowList.get(n).getRoomNode() >= 2000)
            floorSelector.check(binding.select2floor.getId());


        String url;
        if (n == 0)
            url = GlobalVar.URL + GlobalVar.URL_CURRENT_FLOW_NODE; //현위치 기준
        else
            url = GlobalVar.URL + GlobalVar.URL_FLOW_NODE; //출발지-도착지


        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {

                            flowNodeList.clear();
                            JSONObject jsonResponse = new JSONObject(response);
                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "response" + response);


                            nodeArr = jsonResponse.getJSONArray("nodeFlow"); //첫번째 동선의 노드

                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "nodeArr 노드 목록 : " + nodeArr);

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
                            drawPolyline();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, GlobalVar.MSG_REQUEST_PATH_FAILED + e.getMessage());
                        }


                    }
                },
                new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        e.printStackTrace();
                        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, GlobalVar.MSG_REQUEST_PATH_FAILED + e.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                if (n == 0) {
                    params.put("lat", GlobalVar.BeaconList.getWGS_K_lat() + "");
                    params.put("lng", GlobalVar.BeaconList.getWGS_K_lng() + "");
                    params.put("major", GlobalVar.BeaconList.getFloor() + "");
                    params.put("end_room_node", endRoomNode);
                } else {
                    params.put("start_room_node", startRoomNode);
                    params.put("end_room_node", endRoomNode);
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


    } //getNFlowNode()

    //구글맵 회전 각도 계산
    public float getBearing(LatLng P1_LatLng, LatLng P2_LatLng) {

        //라디안 각도로 변환
        double radian = Math.PI / 180;
        double startLat = P1_LatLng.latitude * radian;
        double startLng = P1_LatLng.longitude * radian;
        double endLat = P2_LatLng.latitude * radian;
        double endLng = P2_LatLng.longitude * radian;

        //두 좌표의 거리
        double radian_distance = Math.acos(Math.sin(startLat) * Math.sin(endLat)
                + Math.cos(startLat) * Math.cos(endLat) * Math.cos(startLng - endLng));


        //목적지 이동 방향
        double radian_bearing = Math.acos((Math.sin(endLat) - Math.sin(startLat)
                * Math.cos(radian_distance)) / (Math.cos(startLat) * Math.sin(radian_distance)));

        // 방위각
        double true_bearing = 0;
        if (Math.sin(endLng - startLng) < 0) {
            true_bearing = radian_bearing * (180 / Math.PI);
            true_bearing = 360 - true_bearing;
        } else {
            true_bearing = radian_bearing * (180 / Math.PI);
        }

//
        Log.i("방위각", "방위각: " + radian_bearing);
        Log.i("방위각", "현위치 방위각: " + BeaconAdapter.thisMarker.getRotation());
        Log.i("방위각", "방위각 차이: " + Math.abs(BeaconAdapter.thisMarker.getRotation() - true_bearing));
//
//
//        if (Math.abs(BeaconAdapter.thisMarker.getRotation() - true_bearing) >= 45 &&Math.abs(BeaconAdapter.thisMarker.getRotation() - true_bearing) >= 135 ) {
//            true_bearing += 180;
//            if (true_bearing >= 360) true_bearing -= 360;
//            Log.i("방위각", "바뀐 방위각: " + true_bearing);
//        }


        return (float) true_bearing;
    } //getBearing()

    public static float getChangedAzimut() {
        if (azimutFull) {
            float min = 1000, max = -1000, sum = 0.0f;
            int i, minI = -1, maxI = -1, count = 0;
            float[] arr = new float[AZIMUT_SIZE];
            float firstV = mAzimutArr[0];

            arr[0] = mAzimutArr[0];

            for (i = 1; i < AZIMUT_SIZE; i++)
                if (firstV > mAzimutArr[i])
                    if (firstV - mAzimutArr[i] > 180)
                        arr[i] = mAzimutArr[i] + 360;
                    else
                        arr[i] = mAzimutArr[i];

                else if (mAzimutArr[i] - firstV > 180)
                    arr[i] = mAzimutArr[i] - 360;
                else
                    arr[i] = mAzimutArr[i];


            for (i = 0; i < AZIMUT_SIZE; i++) {
                if (arr[i] < min) {
                    min = arr[i];
                    minI = i;
                }
                if (arr[i] > max) {
                    max = arr[i];
                    maxI = i;
                }
            }

            for (i = 0; i < AZIMUT_SIZE; i++) {
                if (minI == i || maxI == i) continue;
                count++;
                sum += arr[i];
            }

            float dir = sum / count;
            while (true) {
                if (dir >= 0 && dir <= 360) break;
                if (dir < 0) dir += 360;
                if (dir > 360) dir -= 360;
            }

            return dir;
        } else
            return mAzimutArr[index];

    } //getChangedAzimut()


    protected void onDestroy() {
        super.onDestroy();
        naviStartCheck = false;
        GlobalVar.isScanning = false;
        flag = false;
        finish();
    } //onDestroy()

    protected void onResume() {
        super.onResume();

        //가속도 센서에 대한 딜레이 설정
        if (mAccelSensor != null)
            sm.registerListener(this, mAccelSensor, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);

        //지자기 센서에 대한 딜레이 설정
        if (mMagnetSensor != null)
            sm.registerListener(this, mMagnetSensor, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
    } //onResume()

    protected void onPause() {
        super.onPause();

        //센서 업데이트 중지
        sm.unregisterListener(this);
    } //onPause()


    @Override
    // 센서의 변화를 감지
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(sensorEvent.values, 0, mLastAccel, 0, mLastAccel.length);
            mLastAccelSet = true;
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(sensorEvent.values, 0, mLastMagnet, 0, mLastMagnet.length);
            mLastMagnetSet = true;
        }

        // 가속도 센서와 지자기 센서가 모두 감지 되었으면
        if (mLastAccelSet && mLastMagnetSet) {

            boolean success = SensorManager.getRotationMatrix(mR, null, mLastAccel, mLastMagnet);
            if (success) {
                SensorManager.getOrientation(mR, mOrientation);
                mAzimut = (float) Math.toDegrees(mOrientation[0]);
                changeAzimut(mAzimut);
            }
        }

        //getRotationMatrix(), getOrientation()
        //getRotationMatrix(float[] R, float[] I, float[] gravity, float[] geomagnetic)
        //경사도와 회전 매트릭스를 구하는 함수
        // 지구에 대한 세계 좌표계를 기준으로 핸드폰 장치의 좌표계의 변화하는 값을 구한다.

        //R: 회전 매트릭스(mR)
        //I: 경사도 매트릭스
        //gravity: 장치 좌표계의 gravity vector(mLastAccelerometer)
        //geomagnetic: 장치 좌표계의 geomagnetic vector(mLastMagnetometer)


        //getOrientation(float[] R, float[] values)
        //회전 매트릭스(mR)를 이용하여 장치의 방향을 구하는 함수
        //기기의 상단이 북쪽을 향하면 0도, 동쪽을 향하면 90도, 남쪽을 향하면 180도, 서쪽을 향하면 270도
        //values[0] : Azimuth - z축에 대한 회전 방위각
        //values[1] : Pitch - x축에 대한 회전 방위각
        //values[2] : Roll - y축에 대한 회전 방위각

    } //onSensorChanged()

    // 현위치 회전 부드럽게
    private void changeAzimut(float mAzimut) {
        // mAzimut -180 ~ +180
        if (mAzimut < 0) mAzimut = mAzimut + 360.0f;
        mAzimutArr[index++] = mAzimut;
        if (AZIMUT_SIZE <= index) {
            index = 0;
            azimutFull = true;
        }
    } //changeAzimut()

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    } //onAccuracyChanged()
}