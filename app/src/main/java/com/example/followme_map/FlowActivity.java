package com.example.followme_map;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.followme_map.databinding.ActivityFlowBinding;
//import com.github.nkzawa.emitter.Emitter;
//import com.github.nkzawa.socketio.client.IO;
//import com.github.nkzawa.socketio.client.Socket;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static com.google.maps.android.PolyUtil.distanceToLine;


public class FlowActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener, GoogleMap.OnMarkerDragListener {

    private ActivityFlowBinding binding;
    private final String TAG = "FlowActivity";

    //구글맵 Values---------------
    private GoogleMap mMap; //구글맵 오버레이
    private CameraPosition camPosition;
    private float zoomLevel = 25;
    private LatLng movePosition = new LatLng(35.896761232132604, 128.62037402930775);
    private LatLng thisPoint = new LatLng(35.896761232132604, 128.62037402930775);
    private LatLng schoolPoint = new LatLng(35.89679977286669, 128.62092742557013);
    private LatLng startPoint, endPoint;
    private ArrayList<Flow> flowList = new ArrayList<Flow>();

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
                binding.naviStart.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.GONE);
                setFirstFlow();
            }
        });

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

    public void setFirstFlow(){
        receiveFlow();
        connectPyTest();
        camPosition = new CameraPosition.Builder().target(thisPoint).zoom(25).bearing(-14.7f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
    } //setFirstFlow()

    public void setAllFlow(){
        receiveFlow();
        camPosition = new CameraPosition.Builder().target(schoolPoint).zoom(18.5f).bearing(-14.7f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
    } //setAllFlow()

    // 구글맵 준비됨
    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;
        // 본관 좌표를 기준으로 구글맵에 도면 오버레이
        mapOverlay();

//        camPosition = new CameraPosition.Builder().target(thisPoint).zoom(zoomLevel).bearing(getChangedAzimut() - 14.7f).build();
//        camPosition = new CameraPosition.Builder().target(schoolPoint).zoom(zoomLevel).bearing(getChangedAzimut() - 14.7f).build();


        setAllFlow();
        //현위치 갱신
        //connectPy(); //파이썬 소켓통신



        // 동선 정보 받아옴


//        mMap.setOnMarkerDragListener(this);


        //동선표시
//        drawPolyline();

    } //onMapReady()

    //파이썬에서 실시간 좌표를 받아왔다치고
    //임의의 데이터로 작업
    void connectPyTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        testLatLng();
                        setCameraPosition();
                        Thread.sleep(300);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    } //connectPyTest()

    // cameraPosition 업데이트
    void setCameraPosition() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        zoomLevel = mMap.getCameraPosition().zoom;
                        camPosition = new CameraPosition.Builder(camPosition).zoom(zoomLevel).target(thisPoint).bearing(getBearing(flowList.get(nearDist()).getLatLng(), flowList.get(nearDist() + 1).getLatLng())).build();
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
        }).start();
    } //setCameraPosition()


    //구글맵 오버레이 (3층) - 구글맵이 준비되면 호출
    void mapOverlay() {
        mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.map_3th_floor_2))
                .positionFromBounds(new LatLngBounds(new LatLng(35.89651393057683, 128.6201298818298), new LatLng(35.89707923321034, 128.62176975983763))));
    } //mapOverlay()


    //진료동선 받기(임의데이터) - 구글맵이 준비되면 호출
    public void receiveFlow() {
        String url = "http://192.168.0.8:8000/api/patient/flow";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {
                            flowList.clear();
                            JSONObject jsonResponse = new JSONObject(response);
//                            boolean check = jsonResponse.getBoolean("check"); //완료된 동선, 미완료 동선
                            JSONArray flowArr = jsonResponse.getJSONArray("nodeFlow"); //첫번째 진료동선에 대한 노드 정보


//                            for (int i = 0; i < flowArr.length(); i++) {
//                                JSONObject flowObj = flowArr.getJSONObject(i);
//                                Flow flow = new Flow();
//
//                                flow.setMinor(flowObj.getInt("beacon_id_minor"));
//                                flow.setFloor(flowObj.getInt("floor"));
//                                flow.setLatLng(flowObj.getDouble("lat"), flowObj.getDouble("lng"));
//                                flowList.add(flow);
//                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i(TAG, "서버에 진료동선 요청 실패" + e.getMessage());
                        }

                        for (int i = 0; i < flowList.size(); i++) {
                            Log.i(TAG, "진료동선 요청 성공/ 진료동선 1 [" + i + "] minor : " + flowList.get(i).getMinor());
                            Log.i(TAG, "진료동선 요청 성공/ 진료동선 1 [" + i + "] floor : " + flowList.get(i).getFloor());
                            Log.i(TAG, "진료동선 요청 성공/ 진료동선 1 [" + i + "] latLng : " + flowList.get(i).getLatLng());
                        }

                        startPoint = flowList.get(0).getLatLng();
                        endPoint = flowList.get(flowList.size() - 1).getLatLng();
                        mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));
                        drawPolyline();
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
                params.put("patient_id", "1");
                params.put("token", "dslfklsdkflasmfkdsjgkdfkgjskdjfgksdjfksdjfksd");
                return params;
            }
        };

        ///test
        flowList.clear();
        flowList.add(new Flow(1, 1, 35.896761232132604, 128.62037402930775));
        flowList.add(new Flow(1, 1, 35.89671595195291, 128.6203835852756));
        flowList.add(new Flow(1, 1, 35.89683636886484, 128.6210034794277));
        flowList.add(new Flow(1, 1, 35.89679227657561, 128.62100719440897));

        startPoint = flowList.get(0).getLatLng();
        endPoint = flowList.get(flowList.size() - 1).getLatLng();
        mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));
        drawPolyline();
        ///>>>>>>>>>>>

        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);
    } //receiveFlow()

    //진료동선 표시
    void drawPolyline() {

        PolylineOptions polyOpt = new PolylineOptions();
        for (int i = 0; i < flowList.size(); i++) {
            polyOpt.add(flowList.get(i).getLatLng());
            mMap.addMarker(new MarkerOptions()
                    .position(flowList.get(i).getLatLng())
                    .draggable(true))
                    .setTitle(flowList.get(i).getLatLng().toString());

            System.out.println("polyOPT" + flowList.get(i).getLatLng());
        }


        polyOpt.startCap(new RoundCap());
        polyOpt.endCap(new RoundCap());
        polyOpt.width(25f);
        Polyline polyline = mMap.addPolyline(polyOpt);
    } //drawPolyline()

    //임의의 좌표 생성
    //실제로는 칼만필터 적용한 위치값으로
    void testLatLng() {

        testNum++;
        if (testNum < 10)
            thisPoint = new LatLng(thisPoint.latitude - 0.0000045280179694f, thisPoint.longitude + 0.000000955596785f);
        else if (testNum < 50)
            thisPoint = new LatLng(thisPoint.latitude + 0.00000301042279825f, thisPoint.longitude + 0.0000154973538025f);
        else if (testNum < 65)
            thisPoint = new LatLng(thisPoint.latitude - 0.000004409228923f, thisPoint.longitude + 0.000000371498127f);
        else if (testNum == 65) {
        }
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

        double dist = radian_distance * radian;
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        System.out.println("좌표1:" + P1_LatLng + "좌표2:" + P2_LatLng + "거리" + radian_distance + "km" + dist);
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

    //가까운 길 찾기
    int nearDist() {
        double distToLines[][] = new double[flowList.size()][2];

        //distToLines 배열에 현위치과 길의 거리를 저장
        for (int i = 0; i < flowList.size() - 1; i++) {
            distToLines[i][0] = distanceToLine(thisPoint, flowList.get(i).getLatLng(), flowList.get(i + 1).getLatLng());
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