package com.example.followme_map;

import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static com.google.maps.android.PolyUtil.distanceToLine;

public class DestSearchActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener, GoogleMap.OnMarkerDragListener {


    private ActivityDestSearchBinding binding;


    //구글맵 Values---------------
    private GoogleMap mMap; //구글맵 오버레이
    private SupportMapFragment mapFragment;
    private CameraPosition camPosition;
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
    boolean frag = true;
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
    private int mode = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDestSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


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


        //전체 비콘 가져옴
        //현위치, 층알아내고 도면오버레이
        //현위치 버튼 누르면
        //현위치-도착지 안내
        //출발지-도착지 검색하여 안내


    }


    @Override
    public void onBackPressed() {
//        frag = false;
        finish();
    }


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

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    void getBeaconList() {
        //전체 비콘 목록을 가져올 것
    }

    //현재 사용자의 층 수 알기
    int getThisFloor() {
        int thisFloor = 2; //test //신호가 가장 잘 잡히는 비콘 몇 개를 배열에 저장하고 어떤 minor가 가장 많은지 판단
        return thisFloor;
    } //getThisFloor()


    // 현재 위치한 층에 따른 2,3층 맵 오버레이
    void mapOverlay() {
        if (getThisFloor() == 1) {
            groundOverlayOptions = groundOverlayOptions.image(BitmapDescriptorFactory.fromResource(R.drawable.map_2th_floor))
                    .positionFromBounds(new LatLngBounds(new LatLng(35.89651393057683, 128.6201298818298), new LatLng(35.89707923321034, 128.62176975983763)));
            binding.floorSelector.check(binding.select2floor.getId());
            overlayMap = 1;
        } else if (getThisFloor() == 2) {
            groundOverlayOptions = groundOverlayOptions.image(BitmapDescriptorFactory.fromResource(R.drawable.map_3th_floor))
                    .positionFromBounds(new LatLngBounds(new LatLng(35.89651393057683, 128.6201298818298), new LatLng(35.89707923321034, 128.62176975983763)));
            binding.floorSelector.check(binding.select3floor.getId());
            overlayMap = 2;
        }
        groundOverlay = mMap.addGroundOverlay(groundOverlayOptions);


    } //mapOverlay()


    // 현위치 찍기
    protected void setThisMarker() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {




                        if (overlayMap == getThisFloor()) {
                            if (thisMarkerCheck)
                                thisMarker.remove();
                            thisMarker = mMap.addMarker(new MarkerOptions()
                                    .position(thisPoint)
                                    .anchor(0.5f, 0.5f)
                                    .rotation(getChangedAzimut() - camPosition.bearing)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.this_point)));

//                        Log.i("thisMarker", "" + thisMarker.getRotation());
//                        Log.i("thisMarker", "돌아가는 중");
                            thisMarkerCheck = true;
                        } else {
                            if (thisMarkerCheck)
                                thisMarker.remove();
                        }


                    }
                });
            }
        }).start();


    }

    // 센서의 변화를 감지
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

    @Override
    protected void onPause() {
        super.onPause();
        //센서 업데이트 중지
        sm.unregisterListener(this);
    } //onPause()

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        camPosition = new CameraPosition.Builder().target(schoolPoint).zoom(18.5f).bearing(-14.7f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));

        getBeaconList(); //전체 비콘 목록 가져올 것
        mapOverlay(); // 현재 위치한 층에 따른 2,3층 맵 오버레이
        getRoom();


        //현위치 방위각
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (frag) {
                            setThisMarker();
                            Thread.sleep(100);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


        //층선택기 값 바뀌면 새로 그리기
        binding.floorSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                drawPolyline();
                if (binding.floorSelector.getCheckedRadioButtonId() == binding.select2floor.getId()) {
                    groundOverlayOptions = groundOverlayOptions.image(BitmapDescriptorFactory.fromResource(R.drawable.map_2th_floor));
                    overlayMap = 1;
                } else if (binding.floorSelector.getCheckedRadioButtonId() == binding.select3floor.getId()) {
                    groundOverlayOptions = groundOverlayOptions.image(BitmapDescriptorFactory.fromResource(R.drawable.map_3th_floor));
                    overlayMap = 2;
                }
                groundOverlay = mMap.addGroundOverlay(groundOverlayOptions);
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

        //출발지-도착지 경로 표시
        binding.showFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFlowNode(); //경로표시
            }
        });

        binding.thisPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.thisP.setVisibility(View.VISIBLE);
                binding.cancel.setVisibility(View.VISIBLE);
                binding.startRoom.setVisibility(View.INVISIBLE);
                mode = 2;
            }
        });

        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.thisP.setVisibility(View.INVISIBLE);
                binding.cancel.setVisibility(View.INVISIBLE);
                binding.startRoom.setVisibility(View.VISIBLE);
                mode = 1;
            }
        });


        binding.naviStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.naviSelector.setVisibility(View.INVISIBLE);
                binding.floorSelector.setVisibility(View.INVISIBLE);
                binding.naviStart.setVisibility(View.INVISIBLE);


                // 경로 안내 시작 음성
                mediaPlayer = MediaPlayer.create(DestSearchActivity.this, R.raw.flow_start_sound);
                mediaPlayer.start();


                if (startMarkerCheck) {
                    startMarker.remove();
                    startMarkerCheck = false;
                }
                if (endMarkerCheck) {
                    endMarker.remove();
                    endMarkerCheck = false;
                }

                naviStart(); //
            }
        });
    }


    void naviStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (frag) {
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
    }


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
    FlowNode getNearNode() {

        FlowNode nearFlowNode = null;

        double distArr[][] = new double[flowNodeList.size()][2];

        //현재 층의 노드 중 현위치와 가장 가까운 노드 계산
        for (int i = 0; i < flowNodeList.size(); i++) {
            final double R = 6372.8 * 1000;

            //같은 층의 노드인지 판단
            if (flowNodeList.get(i).getFloor() == getThisFloor()) {
                double a = getDistance(thisPoint, flowNodeList.get(i).getLatLng());
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

        nearFlowNode = flowNodeList.get((int) distArr[0][1]);

        return nearFlowNode;
    } //getNearNode()

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

        return dist;
    } //getDistanceMeter()


    void arrived() {

        // 도착안내
        mediaPlayer = MediaPlayer.create(DestSearchActivity.this, R.raw.arrival_sound);
        mediaPlayer.start();
        binding.turn.setText("목적지 도착");
        binding.turnImg.setImageResource(R.drawable.arrive);


        // 안내 메세지
        CustomDialog customDialog = new CustomDialog(DestSearchActivity.this, new CustomDialogClickListener() {
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
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Authorization", "Bearer " + LoginActivity.patientToken);
                        return headers;
                    }


                };
                request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
                AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 초기화 필수
                AppHelper.requestQueue.add(request);

                Intent intent = new Intent(DestSearchActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }, "목적지 부근에 도착했습니다.", "경로 안내를 종료합니다.");
        customDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        customDialog.setCancelable(false);
        customDialog.show();

    }

    //카메라 회전
    void changeTurn() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        LatLng nodeA = getNearNode().getLatLng();
                        if ((getNearNode().getIndex() + 1) >= flowNodeList.size()) {
                            frag = false;


                            //도착
                            arrived();


                            return;

                        } else {
                            if ((getNearNode().getIndex() + 3) >= flowNodeList.size())
                                return;
                        }


                        LatLng nodeB = flowNodeList.get(getNearNode().getIndex() + 1).getLatLng();
                        LatLng nodeC = flowNodeList.get(getNearNode().getIndex() + 3).getLatLng();

                        double dist = getDistanceMeter(thisPoint, nodeB);
                        dist = Math.round(dist * 1000) / 1000.0;


                        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "nodeA : " + getNearNode().getIndex());
                        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "nodeB : " + flowNodeList.get(getNearNode().getIndex() + 2).getIndex());
                        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "nodeC : " + flowNodeList.get(getNearNode().getIndex() + 3).getIndex());

                        //좌우판단
                        if (dist < 4) {
//                            binding.distance.setText(dist + "m 남음");
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
                        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "남은 거리 : " + dist);


                    }
                });
            }
        }).start();
    } //changeTurn()

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


        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "angle : " + angle);
        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "temp1 : " + temp1);
        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "temp2 : " + temp2);

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

    // cameraPosition 업데이트
    void setCameraPosition() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

//                        zoomLevel = mMap.getCameraPosition().zoom;
                        camPosition = new CameraPosition.Builder(camPosition).zoom(zoomLevel).target(thisPoint).bearing(getBearing(flowNodeList.get(getNearDist()).getLatLng(), flowNodeList.get(getNearDist() + 1).getLatLng())).build();
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));


                    }
                });
            }
        }).start();

    } //setCameraPosition()

    //현 위치에서 가장 가까운 길 찾기
    int getNearDist() {
        double distToLines[][] = new double[flowNodeList.size()][2];

        //distToLines 배열에 현위치과 길의 거리를 저장
        for (int i = 0; i < flowNodeList.size() - 1; i++) {
            distToLines[i][0] = distanceToLine(thisPoint, flowNodeList.get(i).getLatLng(), flowNodeList.get(i + 1).getLatLng());
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
        Log.i("dest", "polyLine");
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

            if (binding.floorSelector.getCheckedRadioButtonId() == binding.select2floor.getId()) {
                if (flowNodeList.get(i).getFloor() == 1) {
                    polyOpt.add(flowNodeList.get(i).getLatLng());

                    if (i == 0) {
                        startMarker = mMap.addMarker(new MarkerOptions().position(startPoint).title("출발지").icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
                        startMarkerCheck = true;
                    }
                    if (i == flowNodeList.size() - 1) {
                        endMarker = mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));
                        endMarkerCheck = true;
                    }
                }
            }
            if (binding.floorSelector.getCheckedRadioButtonId() == binding.select3floor.getId()) {
                if (flowNodeList.get(i).getFloor() == 2) {
                    polyOpt.add(flowNodeList.get(i).getLatLng());
                    if (i == 0) {
                        startMarker = mMap.addMarker(new MarkerOptions().position(startPoint).title("출발지").icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
                        startMarkerCheck = true;
                    }
                    if (i == flowNodeList.size() - 1) {
                        endMarker = mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));
                        endMarkerCheck = true;
                    }
                }
            }

//            mMap.addMarker(new MarkerOptions()
//                    .position(flowNodeList.get(i).getLatLng())
//                    .draggable(true))
//                    .setTitle(flowNodeList.get(i).getLatLng().toString());

        }
        polyline = mMap.addPolyline(polyOpt);
        polyCheck = true;

    } //drawPolyline()


    //동선의 노드 표시
    public void getFlowNode() {


        if (roomNodeList.get(selectedStartRoomPosition) >= 3000)
            binding.floorSelector.check(binding.select3floor.getId());
        else if (roomNodeList.get(selectedStartRoomPosition) >= 2000)
            binding.floorSelector.check(binding.select2floor.getId());
        Log.i("mode2", "도착room:" + roomNameList.get(selectedEndRoomPosition));
        Log.i("mode2", "mode:" + mode);

        String url = null;
        if (mode == 1) {
            binding.naviStart.setVisibility(View.INVISIBLE);
            url = GlobalVar.URL + GlobalVar.URL_NAVIGATION;
        } else if (mode == 2) {
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
                if (mode == 1) {
                    params.put("start_room", roomNameList.get(selectedStartRoomPosition) + "");
                    params.put("end_room", roomNameList.get(selectedEndRoomPosition) + "");
                } else if (mode == 2) {
                    Log.i("mode2", "mode: 들어옴");
                    params.put("lat", thisPoint.latitude + "");
                    params.put("lng", thisPoint.longitude + "");
                    params.put("major", getThisFloor() + "");
                    params.put("end_room", roomNameList.get(selectedEndRoomPosition) + "");
                    Log.i("mode2", "mode: 값 전달");
                    Log.i("mode2", "lat" + thisPoint.latitude + "");
                    Log.i("mode2", "lng" + thisPoint.longitude + "");
                    Log.i("mode2", "major" + getThisFloor() + "");
                    Log.i("mode2", "도착룸" + roomNameList.get(selectedEndRoomPosition) + "");
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


//        String url = null;
//        if (mode == 1) {
//            url = GlobalVar.URL + GlobalVar.URL_FLOW_NODE;
//            binding.naviStart.setVisibility(View.INVISIBLE);
//        } else if (mode == 2) {
//            url = GlobalVar.URL + GlobalVar.URL_CURRENT_FLOW_NODE;
//            binding.naviStart.setVisibility(View.VISIBLE);
//        }
//
//        StringRequest request = new StringRequest(
//                Request.Method.POST,
//                url,
//                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//
//                            flowNodeList.clear();
//                            JSONObject jsonResponse = new JSONObject(response);
//                            Log.i("dest", "response" + response);
//
//
//                            nodeArr = jsonResponse.getJSONArray("nodeFlow"); //첫번째 동선의 노드
//
//                            Log.i("dest", "nodeArr 노드 목록 : " + nodeArr);
//
//                            for (int i = 0; i < nodeArr.length(); i++) {
//                                JSONObject nodeObj = nodeArr.getJSONObject(i);
//                                FlowNode flowNode = new FlowNode();
//                                flowNode.setIndex(i);
//                                flowNode.setId(nodeObj.getInt("node_id"));
//                                flowNode.setFloor(nodeObj.getInt("floor"));
//                                flowNode.setLatLng(nodeObj.getDouble("lat"), nodeObj.getDouble("lng"));
//                                flowNode.setStairCheck(nodeObj.getInt("stair_check"));
//                                flowNodeList.add(flowNode);
//                            }
//
//                            startPoint = flowNodeList.get(0).getLatLng();
//                            endPoint = flowNodeList.get(flowNodeList.size() - 1).getLatLng();
//                            drawPolyline();
//                            Log.i("dest", "동선 수신 완료");
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "서버에 진료동선 요청 실패" + e.getMessage());
//                        }
//
//
//                    }
//                },
//                new Response.ErrorListener() { //에러 발생시 호출될 리스너 객체
//                    @Override
//                    public void onErrorResponse(VolleyError e) {
//                        e.printStackTrace();
//                        Log.i(GlobalVar.TAG_ACTIVITY_FLOW, "서버에 진료동선 요청 실패" + e.getMessage());
//                    }
//                }
//        ) {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                if (mode == 1) {
//                    params.put("start_room_node", roomNodeList.get(selectedStartRoomPosition) + "");
//                    params.put("end_room_node", roomNodeList.get(selectedEndRoomPosition) + "");
//                } else if (mode == 2) {
//                    params.put("lat", thisPoint.latitude + "");
//                    params.put("lng", thisPoint.longitude + "");
//                    params.put("major", getThisFloor() + "");
//                    params.put("end_room_node", roomNodeList.get(selectedEndRoomPosition) + "");
//                }
//
//                return params;
//            }
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<String, String>();
//                headers.put("Authorization", "Bearer " + LoginActivity.patientToken);
//                return headers;
//            }
//
//
//        };
//
//
//        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
//        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
//        AppHelper.requestQueue.add(request);


    } //getFlowNode()


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
    protected void onDestroy() {
        super.onDestroy();
        frag = false;
    } //onDestroy()
}