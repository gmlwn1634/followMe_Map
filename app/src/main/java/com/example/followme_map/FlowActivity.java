package com.example.followme_map;

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
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static com.google.maps.android.PolyUtil.distanceToLine;

//import com.github.nkzawa.emitter.Emitter;
//import com.github.nkzawa.socketio.client.IO;
//import com.github.nkzawa.socketio.client.Socket;


public class FlowActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener, GoogleMap.OnMarkerDragListener {

    private ActivityFlowBinding binding;
    private final String TAG = "FlowActivity";

    //구글맵 Values---------------
    private GoogleMap mMap; //구글맵 오버레이
    private CameraPosition camPosition;
    private float zoomLevel = 25;
    private LatLng thisPoint = new LatLng(35.8967921072645, 128.6212324187639);
    private LatLng schoolPoint = new LatLng(35.89679977286669, 128.62092742557013);
    private LatLng startPoint, endPoint;
    private ArrayList<Node> nodeList = new ArrayList<Node>();

    private Marker thisMarker;
    private boolean setThisMarker = false;


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
    int testNum = 0;


    //Thread
    boolean frag = true;
    private Thread changeTurnThread;
    private Thread setCameraThread;

    //recyclerView
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<DestInfo> destInfoArrayList = new ArrayList<>();


    //소켓통신 Values--------------
//    private Socket mSocket;
//
//    {
//        try {
//            mSocket = IO.socket("http://localhost:3000");
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFlowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        testNum = 0;

        // Volley 통신 requestQueue 생성 및 초기화
        if (AppHelper.requestQueue != null)
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext());

        //센서 값 받기
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //가속도 센서
        mMagnetSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD); //지자기 센서


        //구글맵 오버레이를 위한 프레그먼트
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // node.js 소켓통신
        // mSocket.on(서버로부터 받을 이벤트명, 리스너);
//        mSocket.on("sendData", sendData);
//        mSocket.connect();

        //안내시작 버튼을 누르면 첫번째 동선만 보여준다.
        binding.naviStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.naviStart.setVisibility(View.INVISIBLE);
                binding.navigation.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
                getFirstFlow();
            }
        });

        //recyclerView
        binding.recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(mLayoutManager);


    } //onCreate()

//
//    //파이썬 socket.io
//    private Emitter.Listener sendData = new Emitter.Listener() {
//        @Override
//        public void call(final Object... args) {
//            runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    System.out.println("받은 데이터" + args[0].toString());
//
//
////                    try {
////                        strData = data.getString("Data");
////                        System.out.println(strData);
////
////                    } catch (JSONException e) {
////                        e.getStackTrace();
////                        return;
////                    }
//                }
//            });
//        }
//    };

    //첫번째 진료동선
    public void getFirstFlow() {
        receiveFlow();
        connectPyTest();
        camPosition = new CameraPosition.Builder().target(thisPoint).zoom(25).bearing(-14.7f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));


    } //getFirstFlow()

    //전체 진료동선
    public void getAllFlow() {
        receiveFlow();
        camPosition = new CameraPosition.Builder().target(schoolPoint).zoom(18.5f).bearing(-14.7f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));


    } //getAllFlow()


    @Override
    // 구글맵 준비됨
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapOverlay(); // 본관 좌표를 기준으로 구글맵에 도면 오버레이
        getAllFlow();
    } //onMapReady()

    //파이썬에서 실시간 좌표를 받아왔다치고
    //임의의 데이터로 작업
    void connectPyTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (frag) {
                            testLatLng();
                            setCameraPosition();
                            changeTurn();
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    } //connectPyTest()

    // 방향전환 계산
    // : 현위치에서 가장 가까운 노드 찾기 nearNode()
    // : nearNode()에서 5번째 후의 노드와 방위각 계산
    // : 방위각이 몇에서 몇이면 좌회전, 우회전 -> 테스트필요



    void changeTurn() {
        changeTurnThread = new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        LatLng nodeA = nearNode().getLatLng();
                        if ((nearNode().getIndex() + 1) >= nodeList.size()) {
                            frag = false;

                            // 안내음
                            MediaPlayer mediaPlayer = MediaPlayer.create(FlowActivity.this, R.raw.arrival_sound);
                            mediaPlayer.start();

                            binding.turn.setText("목적지 도착");
                            binding.turn.setText("");
                            binding.turnImg.setImageResource(R.drawable.arrive);
                            changeTurnThread.interrupt();
                            setCameraThread.interrupt();


                            // 안내 메세지
                            CustomDialog customDialog = new CustomDialog(FlowActivity.this, new CustomDialogClickListener() {
                                @Override
                                public void onPositiveClick() {
                                    Intent intent = new Intent(FlowActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            customDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                            customDialog.show();


                            return;

                        } else {
                            if ((nearNode().getIndex() + 2) >= nodeList.size())
                                return;
                        }


                        LatLng nodeB = nodeList.get(nearNode().getIndex() + 1).getLatLng();
                        LatLng nodeC = nodeList.get(nearNode().getIndex() + 2).getLatLng();

                        double dist = getDistanceMeter(thisPoint, nodeB);
                        dist = Math.round(dist * 1000) / 1000.0;


                        Log.i("nearNode", "nodeA : " + nearNode().getIndex());
                        Log.i("nearNode", "nodeB : " + nodeList.get(nearNode().getIndex() + 1).getIndex());
                        Log.i("nearNode", "nodeC : " + nodeList.get(nearNode().getIndex() + 2).getIndex());

                        //좌우판단
                        if (dist < 4) {
                            binding.distance.setText(dist + "m 남음");
                            switch (ccw(nodeA, nodeB, nodeC)) {

                                //이미지 변경
                                case 1:
                                    binding.navigation.setVisibility(View.VISIBLE);
                                    binding.turn.setText("좌회전");
                                    binding.turnImg.setImageResource(R.drawable.turn_left);
                                    break;
                                case -1:
                                    binding.navigation.setVisibility(View.VISIBLE);
                                    binding.turn.setText("우회전");
                                    binding.turnImg.setImageResource(R.drawable.turn_right);
                                    break;
                                case 0:
                                    binding.navigation.setVisibility(View.INVISIBLE);
                                    break;
                            }
                        }
                        Log.i("nearNode", "남은 거리 : " + dist);


                    }
                });
            }
        });
        changeTurnThread.start();
    } //setCameraPosition()

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

        return dist;
    }

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
    }

    //좌회전 우회전 판단
    int ccw(LatLng nodeA, LatLng nodeB, LatLng nodeC) {

        double x1 = nodeA.latitude;
        double y1 = nodeA.longitude;
        double x2 = nodeB.latitude;
        double y2 = nodeB.longitude;
        double x3 = nodeC.latitude;
        double y3 = nodeC.longitude;

        double temp1 = (y2 - y1) * (x3 - x1) + y1 * (x2 - x1);
        double temp2 = (x2 - x1) * y3;

        double angle = getAngle(nodeA, nodeB, nodeC);


        Log.i("nearNode", "angle : " + angle);
        Log.i("nearNode", "temp1 : " + temp1);
        Log.i("nearNode", "temp2 : " + temp2);

        if (temp1 < temp2) {
            if (angle <= 18) {
                Log.i("nearNode", "직진");
                return 0;
            }
            Log.i("nearNode", "< 우회전");
            return -1;
        } else if (temp1 > temp2) { //시계
            if (angle <= 18) {
                Log.i("nearNode", "직진");
                return 0;
            }
            Log.i("nearNode", "< 좌회전");
            return 1;
        } else  //직진
            Log.i("nearNode", "직진");

        return 0;


    }

    // cameraPosition 업데이트
    void setCameraPosition() {
        setCameraThread = new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        zoomLevel = mMap.getCameraPosition().zoom;
                        camPosition = new CameraPosition.Builder(camPosition).zoom(zoomLevel).target(thisPoint).bearing(getBearing(nodeList.get(nearDist()).getLatLng(), nodeList.get(nearDist() + 1).getLatLng())).build();
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
                        if (setThisMarker)
                            thisMarker.remove();
                        thisMarker = mMap.addMarker(new MarkerOptions()
                                .position(thisPoint)
                                .anchor(0.5f, 0.5f)
                                .rotation(getChangedAzimut() - camPosition.bearing)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.this_point)));

                        setThisMarker = true;

                    }
                });
            }
        });
        setCameraThread.start();
    } //setCameraPosition()


    //구글맵 오버레이 (3층) - 구글맵이 준비되면 호출
    void mapOverlay() {

        mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.map_2th_floor))
                .positionFromBounds(new LatLngBounds(new LatLng(35.89651393057683, 128.6201298818298), new LatLng(35.89707923321034, 128.62176975983763))));
    } //mapOverlay()


    //진료동선 받기(임의데이터) - 구글맵이 준비되면 호출
    public void receiveFlow() {
        String url = "http://172.26.3.122:8000/api/patient/flow";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {

                            System.out.println("전송됨");
//                            nodeList.clear();
                            JSONObject jsonResponse = new JSONObject(response);

//                            JSONArray nodeArr = jsonResponse.getJSONArray("nodeFlow"); //첫번째 진료동선에 대한 노드 정보

//                            for (int i = 0; i < nodeArr.length(); i++) {
//                                JSONObject nodeObj = nodeArr.getJSONObject(i);
//                                Node node = new Node();
//
//                                node.setId(nodeObj.getInt("node_id"));
//                                node.setFloor(nodeObj.getInt("floor"));
//                                node.setLatLng(nodeObj.getDouble("lat"), nodeObj.getDouble("lng"));
//                                node.setStairCheck(nodeObj.getInt("stair_check"));
//                                node.setIndex(i);
//                                nodeList.add(node);
//
//                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i(TAG, "서버에 진료동선 요청 실패" + e.getMessage());
                        }


//                        startPoint = nodeList.get(0).getLatLng();
//                        endPoint = nodeList.get(nodeList.size() - 1).getLatLng();
//                        mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));
//                        drawPolyline();
//
//                        destInfoArrayList.add(new DestInfo(startPoint.toString(), endPoint.toString()));
//                        destInfoArrayList.add(new DestInfo("MRI검사실", "채혈실"));
//                        destInfoArrayList.add(new DestInfo("MRI검사실", "채혈실"));
//                        destInfoArrayList.add(new DestInfo("MRI검사실", "채혈실"));
//                        destInfoArrayList.add(new DestInfo("MRI검사실", "채혈실"));
//
//                        DestAdapter destAdapter = new DestAdapter(destInfoArrayList);
//                        binding.recyclerView.setAdapter(destAdapter);
                    }
                },
                new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        e.printStackTrace();
                        Log.i(TAG, "서버에 진료동선 요청 실패" + e.getMessage());
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

        ///test
        nodeList.clear();
        nodeList.add(new Node(1, 2, 35.8967921072645, 128.6212324187639, 0, 0));
        nodeList.add(new Node(1, 2, 35.89683827901731, 128.6212109610923, 0, 1));
        nodeList.add(new Node(1, 2, 35.89688010517016, 128.62119956170423, 0, 2));
        nodeList.add(new Node(1, 2, 35.896861093285246, 128.62111976598788, 0, 3));

        nodeList.add(new Node(1, 2, 35.89683664942651, 128.6211465880774, 0, 4));
        nodeList.add(new Node(1, 2, 35.89679536644795, 128.62112311874907, 0, 5));
        nodeList.add(new Node(1, 2, 35.89681926712237, 128.62110501383864, 0, 6));
        nodeList.add(new Node(1, 2, 35.896851315742666, 128.62109830831625, 0, 7));

        nodeList.add(new Node(1, 2, 35.896840995001526, 128.6210553929682, 0, 8));
        nodeList.add(new Node(1, 2, 35.89681546474243, 128.62103125308758, 0, 9));
        nodeList.add(new Node(1, 2, 35.89683882221384, 128.6209876671921, 0, 10));
        nodeList.add(new Node(1, 2, 35.89679645284199, 128.62098431443093, 0, 11));
        nodeList.add(new Node(1, 2, 35.896756256237445, 128.62099303161, 0, 12));


        nodeList.add(new Node(1, 2, 35.89674213310069, 128.62092932914433, 0, 13));
        nodeList.add(new Node(1, 2, 35.89675354024934, 128.6208414868011, 0, 14));
        nodeList.add(new Node(1, 2, 35.89678232971241, 128.62078516041308, 0, 15));
        nodeList.add(new Node(1, 2, 35.8968203535155, 128.6207462683833, 0, 16));
        nodeList.add(new Node(1, 2, 35.89682469909184, 128.6206852481296, 0, 17));
        nodeList.add(new Node(1, 2, 35.89677146576499, 128.62062623952872, 0, 18));
        nodeList.add(new Node(1, 2, 35.896798082432184, 128.6205947235716, 0, 19));

        nodeList.add(new Node(1, 2, 35.89678830488179, 128.62050419901945, 0, 20));
        nodeList.add(new Node(1, 2, 35.89673996030942, 128.62052096282537, 0, 21));
        nodeList.add(new Node(1, 2, 35.89668238134201, 128.6205437616015, 0, 22));


        startPoint = nodeList.get(0).getLatLng();
        endPoint = nodeList.get(nodeList.size() - 1).getLatLng();
        mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));
        drawPolyline();

        destInfoArrayList.add(new DestInfo(startPoint.toString(), endPoint.toString()));
        destInfoArrayList.add(new DestInfo("MRI검사실", "채혈실"));
        destInfoArrayList.add(new DestInfo("MRI검사실", "채혈실"));
        destInfoArrayList.add(new DestInfo("MRI검사실", "채혈실"));
        destInfoArrayList.add(new DestInfo("MRI검사실", "채혈실"));

        DestAdapter destAdapter = new DestAdapter(destInfoArrayList);
        binding.recyclerView.setAdapter(destAdapter);
        ///>>>>>>>>>>>

        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);
    } //receiveFlow()

    //진료동선 표시
    void drawPolyline() {

        PolylineOptions polyOpt = new PolylineOptions();
        for (int i = 0; i < nodeList.size(); i++) {
            polyOpt.add(nodeList.get(i).getLatLng());
            mMap.addMarker(new MarkerOptions()
                    .position(nodeList.get(i).getLatLng())
                    .draggable(true))
                    .setTitle(nodeList.get(i).getLatLng().toString());

            System.out.println("polyOPT" + nodeList.get(i).getLatLng());
        }


        polyOpt.startCap(new RoundCap());
        polyOpt.endCap(new RoundCap());
        polyOpt.width(25f);
        Polyline polyline = mMap.addPolyline(polyOpt);
    } //drawPolyline()

    //임의의 좌표 생성
    //실제로는 칼만필터 적용한 위치값으로
    void testLatLng() {

        //test 현위치 이동
        if (testNum < 10) {
            thisPoint = new LatLng(thisPoint.latitude + 0.000004617175281, thisPoint.longitude - 0.00000214576716);
        } else if (testNum < 20) {
            thisPoint = new LatLng(thisPoint.latitude + 0.000004182615285, thisPoint.longitude - 0.000001139938807);
        } else if (testNum < 30) {
            thisPoint = new LatLng(thisPoint.latitude - 0.0000019011884914, thisPoint.longitude - 0.000007979571635);
        } else if (testNum < 40) {
            thisPoint = new LatLng(thisPoint.latitude - 0.0000024443858736, thisPoint.longitude + 0.000002682208952);
        } else if (testNum < 50) {
            thisPoint = new LatLng(thisPoint.latitude - 0.000004128297856, thisPoint.longitude - 0.000002346932833);
        } else if (testNum < 60) {
            thisPoint = new LatLng(thisPoint.latitude + 0.000002390067442, thisPoint.longitude - 0.000001810491043);
        } else if (testNum < 70) {
            thisPoint = new LatLng(thisPoint.latitude + 0.0000032048620296, thisPoint.longitude - 0.000000670552239);
        } else if (testNum < 80) {
            thisPoint = new LatLng(thisPoint.latitude - 0.000001032074114, thisPoint.longitude - 0.000004291534805);
        } else if (testNum < 90) {
            thisPoint = new LatLng(thisPoint.latitude - 0.0000025530259096, thisPoint.longitude - 0.000002413988062);
        } else if (testNum < 100) {
            thisPoint = new LatLng(thisPoint.latitude + 0.000002335747141, thisPoint.longitude - 0.000004358589548);
        } else if (testNum < 110) {
            thisPoint = new LatLng(thisPoint.latitude - 0.000004236937185, thisPoint.longitude - 0.000000335276117);
        } else if (testNum < 120) {
            thisPoint = new LatLng(thisPoint.latitude - 0.0000040196604545, thisPoint.longitude + 0.000000871717907);
        } else if (testNum < 130) {
            thisPoint = new LatLng(thisPoint.latitude - 0.0000014123136755, thisPoint.longitude - 0.000006370246567);
        } else if (testNum < 140) {
            thisPoint = new LatLng(thisPoint.latitude + 0.000001140714865, thisPoint.longitude - 0.000008784234323);
        } else if (testNum < 150) {
            thisPoint = new LatLng(thisPoint.latitude + 0.000002878946307, thisPoint.longitude - 0.000005632638802);
        } else if (testNum < 160) {
            thisPoint = new LatLng(thisPoint.latitude + 0.000003802380309, thisPoint.longitude - 0.000003889202978);
        } else if (testNum < 170) {
            thisPoint = new LatLng(thisPoint.latitude + 0.000000434557634, thisPoint.longitude - 0.00000610202537);
        } else if (testNum < 180) {
            thisPoint = new LatLng(thisPoint.latitude - 0.000005323332685, thisPoint.longitude - 0.000005900860088);
        } else if (testNum < 190) {
            thisPoint = new LatLng(thisPoint.latitude + 0.0000026616667194, thisPoint.longitude - 0.000003151595712);
        } else if (testNum < 200) {
            thisPoint = new LatLng(thisPoint.latitude - 0.0000009777550394, thisPoint.longitude - 0.000009052455215);
        } else if (testNum < 210) {
            thisPoint = new LatLng(thisPoint.latitude - 0.000004834457237, thisPoint.longitude + 0.000001676380592);
        } else if (testNum < 220) {
            thisPoint = new LatLng(thisPoint.latitude - 0.000005757896741, thisPoint.longitude + 0.000002279877613);
        }
        testNum++;

    } //testLatLng()


    // 센서의 변화를 감지
    @Override
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

    //현 위치에서 가장 가까운 길 찾기
    int nearDist() {
        double distToLines[][] = new double[nodeList.size()][2];

        //distToLines 배열에 현위치과 길의 거리를 저장
        for (int i = 0; i < nodeList.size() - 1; i++) {
            distToLines[i][0] = distanceToLine(thisPoint, nodeList.get(i).getLatLng(), nodeList.get(i + 1).getLatLng());
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

    } //nearDist()


    //현재 사용자의 층 수 알기
    int getThisFloor() {
        int thisFloor;
        thisFloor = 2; //test
//        신호가 가장 잘 잡히는 비콘 몇 개를 배열에 저장하고 어떤 minor가 가장 많은지 판단
        return thisFloor;
    } //getThisFloor()

    //현 위치에서 가장 가까운 노드 찾기
    Node nearNode() {

        Node nearNode = null;

        double distArr[][] = new double[nodeList.size()][2];

        //현재 층의 노드 중 현위치와 가장 가까운 노드 계산
        for (int i = 0; i < nodeList.size(); i++) {
            final double R = 6372.8 * 1000;

            //같은 층의 노드인지 판단
            if (nodeList.get(i).getFloor() == getThisFloor()) {
                double a = getDistance(thisPoint, nodeList.get(i).getLatLng());
                double c = 2 * Math.asin(a);
                double dist = R * c;
                distArr[i][0] = dist; //거리
                distArr[i][1] = i; //인덱스
            }
        }

        Arrays.sort(distArr, new Comparator<double[]>() {
            public int compare(double[] o1, double[] o2) {
                return Double.compare(o1[0], o2[0]);
            }
        });
        //가장 가까운 노드 저장

        nearNode = nodeList.get((int) distArr[0][1]);

        return nearNode;
    }//nearNode()

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


    }

    private double getDistance(LatLng a, LatLng b, String unit) {


        double lat1 = a.latitude;
        double lng1 = a.longitude;
        double lat2 = b.latitude;
        double lng2 = b.longitude;
        double theta = lng1 - lng2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));

        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515 * 1609.344;


        return dist;


    }

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

    private float getChangedAzimut() {
        if (azimutFull) {
            float min = 1000, max = -1000, sum = 0.0f;
            int i, minI = -1, maxI = -1, count = 0;
            float arr[] = new float[AZIMUT_SIZE];
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
    public void onMarkerDragStart(Marker marker) {

    } //onMarkerDragStart() test용

    @Override
    public void onMarkerDrag(Marker marker) {

    }//onMarkerDrag() test용


    @Override
    public void onMarkerDragEnd(Marker marker) {
        marker.setTitle(marker.getPosition().toString());
    } //onMarkerDragEnd() test용

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //센서의 정확도가 변경되면 조치를 취하자
    } //onAccuracyChanged()

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
    protected void onDestroy() {
        super.onDestroy();

//        mSocket.disconnect();
//        mSocket.off("sendData", sendData);
    } //onDestroy()

}