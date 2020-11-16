package com.example.followme_map;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.followme_map.databinding.ActivityMainBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

    private ActivityMainBinding binding;
    private GoogleMap mMap; //구글맵 오버레이
    private final String TAG = "MainActivity";

    //소켓통신 Values---------
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;

    //Volley통신 Values--------
    private RequestQueue requestQueue;

    //방위각 계산 Values-----------
    private SensorManager sm;
    private Sensor mAccelSensor; //가속도센서 값
    private Sensor mMagnetSensor; //지자기센서 값

    private final float[] mLastAccel = new float[3]; //최근 가속도 센서 3개
    private final float[] mLastMagnet = new float[3];  //최근 지자기 센서 3개

    private boolean mLastAccelSet = false;  //배열이 가득찼는지 확인
    private boolean mLastMagnetSet = false;

    private final float[] mR = new float[9]; //회전 매트릭스
    private final float[] mOrientation = new float[3];

    private float mAzimut; //mOrientation[0]
    private final static int AZIMUT_SIZE = 10; //평균 낼 데이터 갯수
    private float mAzimutArr[] = new float[AZIMUT_SIZE];
    private boolean azimutFull = false;
    private int index = 0;

    //스마트폰의 가속도센서 및 지자기센서 값 최근 3개를 저장하여 방위각을 계산
    //방위각(-z축을 중심으로 한 회전 각도) : 기기의 현재 나침반 방향과 자북 사이의 각도
    //기기의 상단이 북쪽을 향하면 0도, 동쪽을 향하면 90도, 남쪽을 향하면 180도, 서쪽을 향하면 270도

    // Azimuth, pitch, roll
    //[0] : Azimuth - z축에 대한 회전 방위각
    //[1] : Pitch - x축에 대한 회전 방위각
    //[2] : Roll - y축에 대한 회전 방위각

    //좌표 Valus-----------
    private float[][] flowLatLng = new float[20][2]; //진료동선을 담을 배열
    private LatLng schoolPoint = new LatLng(35.896797, 128.620944);  //본관좌표
    private LatLng startPoint, endPoint;
    private LatLng thisPoint = new LatLng(35.896759, 128.620387);
    private float thisLat, thisLng; //현위치의 위/경도
    private CameraPosition camPosition;
    private Marker thisMarker;
    private boolean setThisMarker = false;
    private float zoomLevel = 25;

    //실시간 이동 임의 좌표---------
//    float lat = 35.896688f;
//    float lng = 128.620400f;
//    int num = 0;


    @SuppressLint("ServiceCast")
    //@SuppressLint("NewApi")는
    //해당 프로젝트의 설정 된 minSdkVersion 이후에 나온 API를 사용할 때
    //Warring을 없애고 개발자가 해당 APi를 사용할 수 있게 합니다.


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // volley 통신 request 초기화
        requestQueue = Volley.newRequestQueue(this);

        //센서 값 받기
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //가속도 센서
        mMagnetSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD); //지자기 센서

        //현위치 갱신
        //connectPy(); //파이썬 소켓통신
        connectPyTest(); //임의로

        //구글맵 오버레이를 위한 프레그먼트
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    //gg

    // cameraPosition 업데이트
    void setCameraPosition() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        zoomLevel = mMap.getCameraPosition().zoom;
                        camPosition = new CameraPosition.Builder(camPosition).zoom(zoomLevel).target(thisPoint).bearing(getChangedAzimut() - 14.7f).build();
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
                        if (setThisMarker)
                            thisMarker.remove();
                        thisMarker = mMap.addMarker(new MarkerOptions()
                                .position(thisPoint)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.this_point)));

                        setThisMarker = true;
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //가속도 센서에 대한 딜레이 설정
        if (mAccelSensor != null)
            sm.registerListener(this, mAccelSensor, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);

        //지자기 센서에 대한 딜레이 설정
        if (mMagnetSensor != null)
            sm.registerListener(this, mMagnetSensor, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //센서 업데이트 중지
        sm.unregisterListener(this);
    }

    // 구글맵 준비됨
    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;

        camPosition = new CameraPosition.Builder().target(thisPoint).zoom(zoomLevel).bearing(getChangedAzimut() - 14.7f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));


        // 본관 좌표를 기준으로 구글맵에 도면 오버레이
        mapOverlay();

        // 동선 정보 받아옴
        receiveFlow();

        //도착지에 마커표시
        mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));

        //동선표시
        drawPolyline();

    }

    // 센서의 변화를 감지
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        //최근 3개를 저장
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

            changeCamera();

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
        //values[0] : Azimuth - z축에 대한 회전 방위각
        //values[1] : Pitch - x축에 대한 회전 방위각
        //values[2] : Roll - y축에 대한 회전 방위각

    }

    // 화면 회전 부드럽게
    private void changeAzimut(float mAzimut) {
        // mAzimut -180 ~ +180
        if (mAzimut < 0) mAzimut = mAzimut + 360.0f;
        mAzimutArr[index++] = mAzimut;
        if (AZIMUT_SIZE <= index) {
            index = 0;
            azimutFull = true;
        }
    }

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

    }

    //진료동선 받기(임의데이터) - 구글맵이 준비되면 호출
    void receiveFlow() {

//라라벨 volley통신으로 좌표 가져오기
//        int patient_id = 1901141, token = 1234;
//        String url = "http://192.168.0.8:8000/api/patient/flow";
//
//        StringRequest request;

//        request = new StringRequest(Request.Method.POST, url, response->{
//            try{
//                JSONObject jsonObject = new JSONObject(response);
//
//
//            }
//        }

        //임의로 찍은 좌표
        flowLatLng[0][0] = 35.896761f;
        flowLatLng[0][1] = 128.620373f;
        flowLatLng[1][0] = 35.896708f;
        flowLatLng[1][1] = 128.620389f;
        flowLatLng[2][0] = 35.896750f;
        flowLatLng[2][1] = 128.620584f;
        flowLatLng[3][0] = 35.896784f;
        flowLatLng[3][1] = 128.620588f;
        flowLatLng[4][0] = 35.896789f;
        flowLatLng[4][1] = 128.620633f;

        startPoint = new LatLng(flowLatLng[0][0], flowLatLng[0][1]);
        endPoint = new LatLng(flowLatLng[4][0], flowLatLng[4][1]);
    }

    //구글맵 오버레이 (3층) - 구글맵이 준비되면 호출
    void mapOverlay() {
        mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory
                        .fromResource(R.drawable.map_3th_floor))
                .position(schoolPoint, 148f));
    }

    //파이썬에서 실시간 좌표를 받아왔다치고
    //임의의 데이터로 작업
    void connectPyTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
//                        randLatLng();
                        setCameraPosition();
                        Thread.sleep(300);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //진료동선 표시
    void drawPolyline() {

        mMap.addPolyline(new PolylineOptions()
                .add(startPoint,
                        new LatLng(flowLatLng[1][0], flowLatLng[1][1]), //꺾는점1
                        new LatLng(flowLatLng[2][0], flowLatLng[2][1]), //꺾는점2
                        new LatLng(flowLatLng[3][0], flowLatLng[3][1]), //꺾는점3
                        endPoint)
                .startCap(new RoundCap())
                .endCap(new RoundCap()));


    }

    //파이썬 Socket 통신
    void connectPy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //ui 변경을 위해
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String pyIp = "192.168.10.10";
                        int port = 8000;

                        //--- 서버 접속
                        try {
                            socket = new Socket(pyIp, port);
                            Log.i(TAG, "Python 서버 접속 성공");
                        } catch (IOException e) {
                            Log.i(TAG, "Python 서버 접속 실패");
                            e.printStackTrace();
                        }
                        Log.i(TAG, "안드로이드 -> Python 서버 연결 요청");

                        //
                        try {

                            //데이터 송신을 위한 버퍼
                            dos = new DataOutputStream(socket.getOutputStream());

                            //데이터 수신을 위한 버퍼
                            dis = new DataInputStream(socket.getInputStream());

                            Log.i(TAG, "버퍼 생성 성공");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i(TAG, "버퍼 생성 실패");
                        }

                        while (true) {
                            try {
                                //2개를 받는 것이 불가능하다면 json객체나 String으로 변환해서 주고 받기
                                thisLat = (float) dis.readFloat();
                                thisLng = (float) dis.readFloat();

                                if (thisLat > 0 && thisLng > 0) {
                                    dos.writeUTF("Python 서버로부터 수신 / 위도 : " + thisLat + "경도 : " + thisLng);
                                    dos.flush(); //찌꺼기 털어주기

                                    //카메라 포지션 및 현위치 바꾸기
                                    setCameraPosition();

                                }

                            } catch (Exception e) {
                                Log.i(TAG, "Python 서버로부터 수신 실패");
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    //임의의 좌표 생성
//    void randLatLng() {
//        if (num == 52) {
//            //끝지점에 도달->시작지점으로 초기화
//            lat = 35.896688f;
//            lng = 128.620400f;
//            num = 0;
//        }
//        thisPoint = new LatLng(lat, lng);
//        lat += 0.000003f;
//        lng += 0.000008f;
//        num++;
//
//    }

    //방위각에 따른 지도 회전
    void changeCamera() {
        zoomLevel = mMap.getCameraPosition().zoom;
        camPosition = new CameraPosition.Builder(camPosition).zoom(zoomLevel).bearing(getChangedAzimut() - 14.7f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //센서의 정확도가 변경되면 조치를 취하자
    }


}