package com.example.followme_map;

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
import com.example.followme_map.databinding.ActivityFlowBinding;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.maps.android.PolyUtil.distanceToLine;


public class FlowActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

    private ActivityFlowBinding binding;

    //beacon Value-------------------
    public BeaconList BeaconList = new BeaconList();
    private MinewBeaconManager mMinewBeaconManager;
    public BeaconAdapter mAdapter;
    private static final int REQUEST_ENABLE_BT = 2;
    private boolean isScanning;
    UserRssi comp = new UserRssi();
    private int state;

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
    private final LatLng schoolPoint = new LatLng(35.89679977286669, 128.62092742557013);
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
    private LatLng startPoint, endPoint;
    private Polyline polyline;
    private PolylineOptions polyOpt;
    private boolean polyCheck = false;
    private Marker endMarker;
    private Marker startMarker;
    private boolean startMarkerCheck = false;
    private boolean endMarkerCheck = false;
    boolean flag = true;

    //안내재생
    private MediaPlayer mediaPlayer;

    //현위치 받아왔는지 표시
    boolean first = false;
    boolean polyStart_This = false; //현위치와 출발지 연결
    public static boolean naviStartCheck = false;

    //test 현위치
    int testNum = 0;

    FlowNode nearNode;

    public static boolean recivedBeacon = true;
    public static String selectedFloor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFlowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


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
                    groundOverlayOptions = groundOverlayOptions.image(BitmapDescriptorFactory.fromResource(R.drawable.map_2th_floor));
                    selectedFloor = "1";
                } else if (binding.floorSelector.getCheckedRadioButtonId() == binding.select3floor.getId()) {
                    groundOverlayOptions = groundOverlayOptions.image(BitmapDescriptorFactory.fromResource(R.drawable.map_3th_floor));
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
                binding.floorSelector.setVisibility(View.GONE);


                // 경로 안내 시작 음성
                mediaPlayer = MediaPlayer.create(FlowActivity.this, R.raw.flow_start_sound);
                mediaPlayer.start();

                if (polyCheck) {
                    polyline.remove();
                    polyCheck = false;
                }
                if (startMarkerCheck) {
                    startMarker.remove();
                    startMarkerCheck = false;
                }
                if (endMarkerCheck) {
                    endMarker.remove();
                    endMarkerCheck = false;
                }

                polyStart_This = true;
                naviStartCheck = true;
                getNFlowNode(0); //첫번째 동선 표시
                naviStart(); //

            }
        });

        //> onCreate()
        //맵 불러옴

        //> onMapReady()
        //카메라포지션
        //전체 비콘 정보를 받아옴
        //비콘으로 현위치 계산
        ////구글맵 오버레이
        //현위치에 따른 현위치 마커 찍기 (Thread)
        //서버에서 동선 받아옴
        //여기까지 완료되면 로딩화면 해제하고 맵표시
        //방위각에 따라 오버레이된 화면 회전 및 현위치 마커 회전 (Thread)


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
        System.out.println("얼마 차이나" + dist);
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

    //현 위치에서 가장 가까운 노드 찾기
//    FlowNode getNearNode() {
//
//        FlowNode nearFlowNode = null;
//        int size = 0;
//
//        for (int i = 0; i < flowNodeList.size(); i++) {
//            if (flowNodeList.get(i).getFloor() == Integer.parseInt(BeaconList.getFloor())) {
//                size++;
//            }
//        }
//        System.out.println("ㅓㅓㅓㅓㅓㅓ size" + size);
//        double[][] distArr = new double[size][2];
//
//        //현재 층의 노드 중 현위치와 가장 가까운 노드 계산
//        for (int i = 0; i < flowNodeList.size(); i++) {
//            final double R = 6372.8 * 1000;
//            //같은 층의 노드인지 판단
//            if (flowNodeList.get(i).getFloor() == Integer.parseInt(BeaconList.getFloor())) {
//                double a = getDistance(BeaconList.getWGS_K_LatLng(), flowNodeList.get(i).getLatLng());
////                double a = getDistance(BeaconAdapter.thisMarker.getPosition(), flowNodeList.get(i).getLatLng()); //test
//                double c = 2 * Math.asin(a);
//                double dist = R * c;
//                distArr[i][0] = dist; //거리
//                distArr[i][1] = i; //인덱스
//            }
//        }
//
//        for (int i = 0; i < distArr.length; i++) {
//            System.out.println("ㅓㅓㅓㅓㅓㅓ 정렬 전index" + i + "dist:" + distArr[i][0] + " index " + distArr[i][1]);
//        }
//
////        double[][] min = new double[1][2];
////        min[0][0] = distArr[0][0];
////        min[0][1] = distArr[0][1];
////        for (int i = 1; i < distArr.length; i++) {
////            if (distArr[i][0] < min[0][0]) {
////                min[0][0] = distArr[i][0];
////                min[0][1] = distArr[i][1];
////            }
////        }
//
//        Arrays.sort(distArr, new Comparator<double[]>() {
//            public int compare(double[] o1, double[] o2) {
//                return Double.compare(o1[0], o2[0]);
//            }
//        });
//
//        //가장 가까운 노드 저장
//
//        nearFlowNode = flowNodeList.get((int) distArr[0][1]);
//        for (int i = 0; i < distArr.length; i++) {
//            System.out.println("ㅓㅓㅓㅓㅓㅓ index" + i + "dist:" + distArr[i][0] + " index " + distArr[i][1]);
//        }
//        System.out.println("ㅓㅓㅓㅓㅓㅓㅓㅓ 디버깅: 가까운 노드" + nearFlowNode.getIndex());
//        System.out.println("ㅓㅓㅓㅓㅓㅓㅓㅓㅓ 현위치" + BeaconList.getWGS_K_LatLng());
//
//        return nearFlowNode;
//    } //getNearNode()

    //경로이탈 감지
    void checkOffLoad() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //경로 이탈 - 현위치와 현위치에서 가장 가까운 노드
//                        if (getDistanceMeter(BeaconList.getLatLng(), getNearNode().getLatLng()) >= 14) {
                        if (getDistanceMeter(BeaconAdapter.thisMarker.getPosition(), nearNode.getLatLng()) >= 14) { //test
                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "이탈이탈");
//                            System.out.println("디버깅 이탈: 현위치" + BeaconAdapter.thisMarker.getPosition());
//                            System.out.println("디버깅 이탈: 가까운 노드" + getNearNode().getIndex() + " 좌표 : " + getNearNode().getLatLng());
                            binding.navigation.setVisibility(View.INVISIBLE);
                            flag = false;
                            mediaPlayer = MediaPlayer.create(FlowActivity.this, R.raw.off_road_sound);
                            mediaPlayer.start();


                            // 안내 메세지
                            CustomDialog customDialog = new CustomDialog(FlowActivity.this, new CustomDialogClickListener() {
                                @Override
                                public void onPositiveClick() {
                                    getNFlowNode(0);//경로 재탐색
                                    flag = true;
                                }
                            }, "경로에서 벗어났습니다.", "현재 위치를 재탐색합니다.");
                            customDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                            customDialog.setCancelable(false);
                            customDialog.show();
                        }
                    }
                });
            }
        }).start();
    }

    void naviStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        if (flag) {
                            setCameraPosition(); //지도 방향
                            checkFloor(); //층 바뀌면 도면 전환
                            checkNearNode(); //가장 가까운 노드 확인
                            checkOffLoad(); //경로이탈 감지
                            changeTurn(); // 방향회전 안내
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


                        ArrayList<FlowNode> nodeList = new ArrayList<FlowNode>();


                        //현재 층의 노드 중 현위치와 가장 가까운 노드 계산
                        for (int i = 0; i < flowNodeList.size(); i++) {
                            final double R = 6372.8 * 1000;
                            //같은 층의 노드인지 판단
                            if (flowNodeList.get(i).getFloor() == Integer.parseInt(BeaconList.getFloor())) {
                                double a = getDistance(BeaconList.getWGS_K_LatLng(), flowNodeList.get(i).getLatLng());
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
                        System.out.println("맨 처음 가장 가까운 노드:" + nearNode.getIndex() + "," + nearNode.getDist());
                        for (int i = 0; i < nodeList.size(); i++) {
                            if (it.hasNext()) {//pc(프로그램카운터가 이동하여, 다음 요소가 있는지 확인)
                                if (e.getDist() < nearNode.getDist()) {
                                    nearNode = e;
                                    System.out.println("갱신 가장 가까운 노드:" + nearNode.getIndex() + "," + nearNode.getDist());
//                                    System.out.println("갱신 가장 가까운 노드:" + nearNode.getIndex());
                                }
                                e = it.next();//hasNext를 해서 다음요소가 있는지 확인해야지만, it.next해서 그 요소를 불러올 수가 있다.
                            }
                        }

                        System.out.println("마지막 가장 가까운 노드:" + nearNode.getIndex() + "," + nearNode.getDist());


                    }
                });
            }
        }).start();
    }


    void checkFloor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //현재 층에 따른 도면 바꾸기
                        if (BeaconList.getFloor().equals("1"))
                            binding.floorSelector.check(binding.select2floor.getId());
                        else if (BeaconList.getFloor().equals("2"))
                            binding.floorSelector.check(binding.select3floor.getId());
                    }
                });
            }
        }).start();
    }

    void arrived() {

        // 도착안내
        recivedBeacon = false;
        mMinewBeaconManager.stopScan();
        mediaPlayer = MediaPlayer.create(FlowActivity.this, R.raw.arrival_sound);
        mediaPlayer.start();
        binding.turn.setText("목적지 도착");
        binding.turnImg.setImageResource(R.drawable.arrive);


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
                                    Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "목적지 도착 알림 성공");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "목적지 도착 알림 실패" + e.getMessage());

                                }

                            }
                        },
                        new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                            @Override
                            public void onErrorResponse(VolleyError e) {
                                e.printStackTrace();
                                Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "목적지 도착 알림 실패" + e.getMessage());
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


//                GlobalVar.thisPoint = new LatLng(35.896758278816, 128.62047268466);
//                GlobalVar.testNum = 0;
//                GlobalVar.mode = 2; //220호에서 305호
//                GlobalVar.mode = 2;
                System.out.println("비교 - mode : 2 로 바뀜");

                Intent intent = new Intent(FlowActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();

            }


        }, "목적지 부근에 도착했습니다.", "경로 안내를 종료합니다.");
        customDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        customDialog.setCancelable(false);
        customDialog.show();

    }


    void changeTurn() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        LatLng nodeA = nearNode.getLatLng();
                        if ((nearNode.getIndex() + 1) >= flowNodeList.size()) {
                            System.out.println("!!!!!도착 현노드 다음노드가 flowNodeList초과");
                            flag = false;
                            arrived();//도착
                        } else if ((nearNode.getIndex() + 3) >= flowNodeList.size()) {
                            return;
                        } else if ((nearNode.getIndex() + 4) >= flowNodeList.size()) {
                            return;
                        }
                        LatLng nodeB = flowNodeList.get(nearNode.getIndex() + 3).getLatLng();
                        LatLng nodeC = flowNodeList.get(nearNode.getIndex() + 4).getLatLng();

//                        double dist = getDistanceMeter(BeaconAdapter.thisMarker.getPosition(), nodeB); //test
//                        dist = Math.round(dist * 1000) / 1000.0;

                        //좌우판단
//                        if (dist < 4) {
//                            binding.distance.setText(dist + "m 남음");
                        switch (ccw(nodeA, nodeB, nodeC)) {

                            //이미지 변경
                            case 1:
                                System.out.println("!!!! case 1 : 좌회전");

                                binding.navigation.setVisibility(View.VISIBLE);
                                binding.turn.setText("좌회전");
                                binding.turnImg.setImageResource(R.drawable.turn_left);
                                break;
                            case -1:
                                System.out.println("!!!! case -1 : 우회전");
                                binding.navigation.setVisibility(View.VISIBLE);
                                binding.turn.setText("우회전");
                                binding.turnImg.setImageResource(R.drawable.turn_right);
                                break;
                            case 0:
                                System.out.println("!!!! case 0 : 직진");
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

        System.out.println("!!!! nodeA : " + nearNode.getIndex() + "," + nearNode.getFloor() + "층");
        System.out.println("!!!! nodeB : " + flowNodeList.get(nearNode.getIndex() + 3).getIndex() + "," + flowNodeList.get(nearNode.getIndex() + 3).getFloor() + "층");
        System.out.println("!!!! nodeC : " + flowNodeList.get(nearNode.getIndex() + 4).getIndex() + "," + flowNodeList.get(nearNode.getIndex() + 4).getFloor() + "층");


        if (nearNode.getFloor() != flowNodeList.get(nearNode.getIndex() + 3).getFloor()) {
            return 0; //직진
        } else if (nearNode.getFloor() != flowNodeList.get(nearNode.getIndex() + 4).getFloor()) {
            return 0; //직진
        }

        double temp1 = (y2 - y1) * (x3 - x1) + y1 * (x2 - x1);
        double temp2 = (x2 - x1) * y3;

        double angle = getAngle(nodeA, nodeB, nodeC);


        System.out.println("!!!!! angle : " + angle);
        System.out.println("!!!!! temp1 : " + temp1);
        System.out.println("!!!!! temp2 : " + temp2);

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


    //임의의 좌표 생성
    //실제로는 칼만필터 적용한 위치값으로
    void testLatLng() {
        //-,+ 위->아래
        //+,- 아래->위
        //+,+ 왼쪽->오른쪽
        //-,- 오른쪽->왼쪽

//        if (testNum < 50) {
//            BeaconAdapter.thisMarker.setPosition(new LatLng(BeaconAdapter.thisMarker.getPosition().latitude + 0.0000011729902506, BeaconAdapter.thisMarker.getPosition().longitude + 0.0000042220403728));
//        }
//        testNum++;


        //35.89666866704047, 128.62027197619136 //출발지
        //35.896758278816, 128.62047268466 //220호
        //35.896752650043,128.62071220482 //305호
//        System.out.println("22222222222222난데스까???????????");
//        if (GlobalVar.mode == 1) {
//            //출발지에서 220호
//            if (GlobalVar.testNum < 50) {
//                GlobalVar.thisPoint = new LatLng(GlobalVar.thisPoint.latitude + 0.0000011729902506, GlobalVar.thisPoint.longitude + 0.0000042220403728); //아래에서 위
//
//            } else if (GlobalVar.testNum < 60) {
//                GlobalVar.thisPoint = new LatLng(GlobalVar.thisPoint.latitude + 0.0000030962263, GlobalVar.thisPoint.longitude - 0.000001039355); //아래에서 위
//            }
//
//        } else if (GlobalVar.mode == 2) {
//            //220호에서 305호
//            if (GlobalVar.testNum < 10) {
//                GlobalVar.thisPoint = new LatLng(GlobalVar.thisPoint.latitude - 0.0000030962263, GlobalVar.thisPoint.longitude + 0.000001039355);
//            } else if (GlobalVar.testNum < 30) {
//                GlobalVar.thisPoint = new LatLng(GlobalVar.thisPoint.latitude + 0.00000061109735, GlobalVar.thisPoint.longitude + 0.000002883375);
//            } else if (GlobalVar.testNum < 50) {
//                GlobalVar.thisPoint = new LatLng(GlobalVar.thisPoint.latitude - 0.0000040332444, GlobalVar.thisPoint.longitude + 0.0000010728835);
//            }
//        }
//        GlobalVar.testNum++;


        //다시 올바른 길로 갈 때
//            thisPoint = new LatLng(35.8967921072645, 128.6212324187639);
//            testNum = 0;
    }


    // cameraPosition 업데이트
    void setCameraPosition() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        zoomLevel = BeaconAdapter.mMap.getCameraPosition().zoom;
//                        camPosition = new CameraPosition.Builder(camPosition).zoom(25).target(BeaconList.getLatLng()).bearing(getBearing(flowNodeList.get(getNearDist()).getLatLng(), flowNodeList.get(getNearDist() + 1).getLatLng())).build();
//                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));

                        camPosition = new CameraPosition.Builder(camPosition).zoom(25).target(BeaconAdapter.thisMarker.getPosition()).bearing(getBearing(flowNodeList.get(getNearDist()).getLatLng(), flowNodeList.get(getNearDist() + 1).getLatLng())).build();
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition)); //test


                    }
                });
            }
        }).start();

    } //setCameraPosition()

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //1. 카메라 포지션
        camPosition = new CameraPosition.Builder().target(schoolPoint).zoom(18.5f).bearing(-14.7f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));


        //3. 현위치 계산
        //3.1 전체 비콘 정보를 받아옴
        getAllBeacon();
        //3.2 현위치 얻기
        initView(BeaconList, mMap); //어댑터 생성
        initManager(); //싱글톤 패턴
        initListener(); //비콘의 신호 수신

        //2. 구글맵 오버레이
        // 본관 좌표를 기준으로 구글맵에 도면 오버레이


        //현위치 계산
        //현재 층에 따른 구글맵 오버레이


    } //onMapReady()


    //현 위치에서 가장 가까운 길 찾기
    int getNearDist() {
        double[][] distToLines = new double[flowNodeList.size()][2];

        //distToLines 배열에 현위치과 길의 거리를 저장
        for (int i = 0; i < flowNodeList.size() - 1; i++) {
//            distToLines[i][0] = distanceToLine(BeaconList.getLatLng(), flowNodeList.get(i).getLatLng(), flowNodeList.get(i + 1).getLatLng());
            distToLines[i][0] = distanceToLine(BeaconAdapter.thisMarker.getPosition(), flowNodeList.get(i).getLatLng(), flowNodeList.get(i + 1).getLatLng()); //test
            distToLines[i][1] = i; //몇번째 동선
        }

        //거리의 기준으로 정렬
        Arrays.sort(distToLines, new Comparator<double[]>() {
            public int compare(double[] o1, double[] o2) {
                return Double.compare(o1[0], o2[0]);
            }
        });

        //가장 가까운 길 반환
        return (int) distToLines[1][1];

    } //getNearDist()

    //진료동선 표시
    void drawPolyline() {
        //null 방지   ////////////오늘 여기까지 했다!!!!


        if (polyCheck) {
            polyline.remove();
            polyCheck = false;
        }
        if (startMarkerCheck) {
            startMarker.remove();
            startMarkerCheck = false;
        }
        if (endMarkerCheck) {
            endMarker.remove();
            endMarkerCheck = false;
        }


        polyOpt = new PolylineOptions();
        polyOpt.startCap(new RoundCap());
        polyOpt.endCap(new RoundCap());
        polyOpt.width(25f);


        for (int i = 0; i < flowNodeList.size(); i++) {
            if (polyStart_This) { //현위치-출발지 점선으로 연결
                List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20));
                PolylineOptions startPoly = new PolylineOptions();
                startPoly.startCap(new RoundCap());
                startPoly.endCap(new RoundCap());
                startPoly.width(15f);
                startPoly.pattern(pattern);
//                startPoly.add(BeaconList.getLatLng());
                startPoly.add(BeaconAdapter.thisMarker.getPosition()); //test
                startPoly.add(flowNodeList.get(0).getLatLng());
                mMap.addPolyline(startPoly);
                polyStart_This = false;
            }
            if (binding.floorSelector.getCheckedRadioButtonId() == binding.select2floor.getId()) {
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
            if (binding.floorSelector.getCheckedRadioButtonId() == binding.select3floor.getId()) {
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

//            mMap.addMarker(new MarkerOptions()
//                    .position(flowNodeList.get(i).getLatLng())
//                    .draggable(true))
//                    .setTitle(flowNodeList.get(i).getIndex() + "," + flowNodeList.get(i).getFloor());

        }


        polyline = mMap.addPolyline(polyOpt);
        polyCheck = true;

    } //drawPolyline()

    void mapOverlay() {

        if (overlayCheck) {
            groundOverlay.remove();
            overlayCheck = false;
        }

        switch (BeaconList.getFloor()) {
            case "1":
                groundOverlayOptions = groundOverlayOptions.image(BitmapDescriptorFactory.fromResource(R.drawable.map_2th_floor))
                        .positionFromBounds(new LatLngBounds(new LatLng(35.89651393057683, 128.6201298818298), new LatLng(35.89707923321034, 128.62176975983763)));
                binding.floorSelector.check(binding.select2floor.getId());
                break;
            case "2":
                groundOverlayOptions = groundOverlayOptions.image(BitmapDescriptorFactory.fromResource(R.drawable.map_3th_floor))
                        .positionFromBounds(new LatLngBounds(new LatLng(35.89651393057683, 128.6201298818298), new LatLng(35.89707923321034, 128.62176975983763)));
                binding.floorSelector.check(binding.select3floor.getId());
                break;
        }

        groundOverlay = mMap.addGroundOverlay(groundOverlayOptions);
        overlayCheck = true;

    } //mapOverlay()

    private void initManager() {
        mMinewBeaconManager = MinewBeaconManager.getInstance(this);
    } //initManager()

    private void initView(BeaconList BeaconList, GoogleMap GoogleMap) {
        mAdapter = new BeaconAdapter(BeaconList, GoogleMap);
    } //initView()

    private void showBLEDialog() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    } //showBLEDialog()


    private void initListener() {
        if (mMinewBeaconManager != null) {
            BluetoothState bluetoothState = mMinewBeaconManager.checkBluetoothState();
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
        if (isScanning) {
            isScanning = false;

            if (mMinewBeaconManager != null) {
                mMinewBeaconManager.stopScan();
            }
        } else {
            isScanning = true;

            try {
                mMinewBeaconManager.startScan();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mMinewBeaconManager.setDeviceManagerDelegateListener(new MinewBeaconManagerListener() {
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
                        Collections.sort(minewBeacons, comp);
                        Log.e("tag", state + "");
                        if (state == 1 || state == 2) {
                        } else {
                            if (recivedBeacon) {
                                mAdapter.setItems(minewBeacons);
                                System.out.println("현위치 : " + BeaconList.getWGS_K_lat() + "," + BeaconList.getWGS_K_lng());
                                if (!first) {
                                    if (BeaconList.getWGS_K_lat() != 0.0) {
                                        first = true;
                                        mapOverlay();
                                        getAllFlow();

                                        binding.lat.setText(BeaconList.getWGS_K_lat() + "");
                                        binding.lng.setText(BeaconList.getWGS_K_lng() + "");

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

        BeaconList.add(new BeaconData("2", "1", "15001", 35.896697983871235, 128.6203462855552));
        BeaconList.add(new BeaconData("2", "1", "15002", 35.89671482300457, 128.62042574599727));
        BeaconList.add(new BeaconData("2", "1", "15003", 35.89673274852964, 128.6205085592006));
        BeaconList.add(new BeaconData("2", "2", "15004", 35.896701975845126, 128.6203400822074));
        BeaconList.add(new BeaconData("2", "2", "15005", 35.89671707059689, 128.62042146459254));
        BeaconList.add(new BeaconData("2", "2", "15006", 35.89673472452264, 128.62050360724362));
        BeaconList.add(new BeaconData("2", "2", "15008", 35.896749390857984, 128.62056999187578));
        BeaconList.add(new BeaconData("2", "2", "15009", 35.896762699196934, 128.6206427467953));
        BeaconList.add(new BeaconData("3", "2", "15010", 35.896780344554, 128.62064817263));
        BeaconList.add(new BeaconData("3", "2", "15011", 35.896842540625, 128.62061430974));
        BeaconList.add(new BeaconData("3", "2", "15012", 35.896848244192, 128.62064683152));
        BeaconList.add(new BeaconData("3", "1", "15013", 35.896581540581, 128.62021097642));
        BeaconList.add(new BeaconData("3", "1", "15014", 35.896596750143, 128.62029613655));
        BeaconList.add(new BeaconData("3", "1", "15015", 35.896651613183, 128.62019085985));
        BeaconList.add(new BeaconData("2", "1", "15016", 35.89665887361227, 128.6205622033809));

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
//                                System.out.println("천은: group" + group + "major : " + major + "minor: " + minor +
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

        BeaconList.getList();


    } //getAllBeacon()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                break;
        }
    } //onActivityResult()

    @Override
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


                            flowArr = jsonResponse.getJSONArray("flow_list"); //전체 동선
                            nodeArr = jsonResponse.getJSONArray("nodeFlow"); //첫번째 동선

                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "전체 동선 목록 표시 OK" + response);
                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "flowARR 동선 목록 : " + flowArr);
                            for (int i = 0; i < flowArr.length(); i++) {
                                JSONObject flowObj = flowArr.getJSONObject(i);
                                Log.i(GlobalVar.TAG_ACTIVITY_FLOW, flowObj.toString());
                                Flow flow = new Flow();
                                if (i == 0) {
                                    JSONObject roomRaca = flowObj.getJSONObject("room_location");
                                    flow.setRoomName("출발지");
                                    flow.setRoomNode(roomRaca.getInt("node_id"));
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
                                if (i == 0)
                                    destInfoArrayList.add(new DestInfo((i + 1) + "", "출발지", flowList.get(i + 1).getRoomName()));
                                else
                                    destInfoArrayList.add(new DestInfo((i + 1) + "", flowList.get(i).getRoomName(), flowList.get(i + 1).getRoomName()));


                            DestAdapter destAdapter = new DestAdapter(FlowActivity.this, destInfoArrayList);
                            binding.recyclerView.setAdapter(destAdapter);


                            //첫번째 동선 표시
                            startPoint = flowNodeList.get(0).getLatLng();
                            endPoint = flowNodeList.get(flowNodeList.size() - 1).getLatLng();
                            drawPolyline();


                            mapFragment.getView().setVisibility(View.VISIBLE);
                            binding.ready.setVisibility(View.INVISIBLE);
                            binding.recyclerView.setVisibility(View.VISIBLE);
                            binding.startEndImg.setVisibility(View.VISIBLE);
                            binding.floorSelector.setVisibility(View.VISIBLE);
                            binding.naviStart.setVisibility(View.VISIBLE);


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "서버에 진료동선 요청 실패" + e.getMessage());
                            Toast.makeText(getApplicationContext(), "등록된 진료 동선이 없습니다.", Toast.LENGTH_SHORT).show();
                            finish();

                        }
                    }
                },
                new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        e.printStackTrace();
                        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "서버에 진료동선 요청 실패" + e.getMessage());
                        Toast.makeText(getApplicationContext(), "등록된 진료 동선이 없습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("lat", BeaconList.getWGS_K_lat() + "");
                params.put("lng", BeaconList.getWGS_K_lng() + "");
//                System.out.println("비교 - getAllFlow mode : " + GlobalVar.mode);
//                System.out.println("비교 - 출발지 lat : " + BeaconList.getLat());
//                System.out.println("비교 - 출발지 lng : " + BeaconList.getLng());
                params.put("major", BeaconList.getFloor()); //층번호
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
            binding.floorSelector.check(binding.select3floor.getId());
        else if (flowList.get(n).getRoomNode() >= 2000)
            binding.floorSelector.check(binding.select2floor.getId());


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

                            startPoint = flowNodeList.get(0).getLatLng();
                            endPoint = flowNodeList.get(flowNodeList.size() - 1).getLatLng();
                            drawPolyline();

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
                if (n == 0) {
                    params.put("lat", BeaconList.getWGS_K_lat() + "");
                    params.put("lng", BeaconList.getWGS_K_lng() + "");
                    params.put("major", BeaconList.getFloor() + "");
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        naviStartCheck = false;
        mMinewBeaconManager.stopScan();
        flag = false;

    } //onDestroy()

    @Override
    protected void onResume() {
        super.onResume();

        //가속도 센서에 대한 딜레이 설정
        if (mAccelSensor != null)
            sm.registerListener(this, mAccelSensor, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);

        //지자기 센서에 대한 딜레이 설정
        if (mMagnetSensor != null)
            sm.registerListener(this, mMagnetSensor, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
    } //onResume()

    @Override
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