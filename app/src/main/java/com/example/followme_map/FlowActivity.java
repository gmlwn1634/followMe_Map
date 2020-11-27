package com.example.followme_map;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import static com.google.maps.android.PolyUtil.distanceToLine;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

public class FlowActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityFlowBinding binding;
    private final String TAG = "FlowActivity";
    private final String URL = "http://192.168.0.8:8000/api/patient/flow";

    // Google Map Values----------
    private GoogleMap mMap;
    private CameraPosition camPosition;
    private Polyline polyline;
    private boolean setPolyline = false;
    private int naviMode = 1; // 1->기본모드, 2->사용자조작모드 // 처음 실행 시 1 -> 사용자 조작 감지 시 2로 전환

    // Flow Values--------------
    private LatLng schoolPoint = new LatLng(35.896797, 128.620944);
    private ArrayList<Flow> flowList = new ArrayList<Flow>();
    private LatLng thisStartP;
    private LatLng thisEndP;
    private LatLng thisPoint = new LatLng(35.89674711683938, 128.62060305686575); //현위치 출발점
    private Marker endMarker;
    private Marker thisMarker;
    private boolean setThisMarker = false;
    private boolean setEndMarker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFlowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //비콘으로 부터 값을 수신하고 사용자 위치를 알아내 laravel에 회원번호와 함께 보낸다.
        //laravel에서 해당 회원의 목적지에 따라 최단거리를 알아내고 진료동선을 리턴해준다.


        //구글맵 오버레이를 위한 프레그먼트
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mapOverlay();

        //전체 동선 보여주기
        setAllFlow();

    } //onMapReady()

    void setAllFlow() {
        //전체 진료동선과 단계별 목적지를 간단히 알려준다.

        //Volley 통신 - 진료동선 수신
        receiveAllFlow();

        camPosition = new CameraPosition.Builder().target(schoolPoint).zoom(18.5f).bearing(-14.7f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));


    } //setAllFlow()

    void setThisPoint() {
        thisMarker = mMap.addMarker(new MarkerOptions()
                .position(thisPoint)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.this_point)));
        thisMarker.setTitle(thisPoint.latitude + ":" + thisPoint.longitude);

        int dist = nearDist();
        camPosition = new CameraPosition.Builder(camPosition).target(thisPoint).zoom(25).bearing(getBearing(flowList.get(dist).getLatLng(), flowList.get(dist + 1).getLatLng())).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                setThisPoint();
            }

            @Override
            public void onCancel() {

            }
        });

    } //setThisPoint()

    int nearDist() {
        double distToLines[][] = new double[flowList.size()][2];

        //distToLines 배열에 현위치과 길의 거리를 저장
        for (int i = 0; i < flowList.size() - 1; i++) {
            distToLines[i][0] = distanceToLine(thisPoint, flowList.get(i).getLatLng(), flowList.get(i + 1).getLatLng());
            distToLines[i][1] = i; //몇번째 동선
            System.out.println("인덱스" + i + (i + 1) + "번째 동선과 현위치 간의 거리" + distToLines[i][0]);
        }

        //거리의 기준으로 정렬
        Arrays.sort(distToLines, new Comparator<double[]>() {
            public int compare(double[] o1, double[] o2) {
                return Double.compare(o1[0], o2[0]);
            }
        });

        //가장 가까운 길 반환
//        System.out.println((int)distToLines[1][1]);
        return (int) distToLines[1][1];

    }


    //https://qastack.kr/gis/11409/calculating-the-distance-between-a-point-and-a-virtual-line-of-two-lat-lngs
    //광명이다 광명

    void setFirstFlow() {
        //첫번째 진료동선을 알려준다.

        if (naviMode == 1) {
            //안내모드

            receiveFirstFlow();

            camPosition = new CameraPosition.Builder(camPosition).target(thisPoint).zoom(25).bearing(getBearing(flowList.get(0).getLatLng(), flowList.get(1).getLatLng())).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    setThisPoint();
                }

                @Override
                public void onCancel() {

                }
            });

        } else if (naviMode == 2) {
            //사용자 조작모드
            //구글맵의 확대/축소/이동이 감지되면
            //사용자 조작모드로
            //naviMode = 2, setFirstFlow()


        }


    } //setFirstFlow()
//
//
//    public LatLng getTarget() {
//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//        builder.include(thisStartP);
//        builder.include(thisEndP);
//        LatLngBounds bounds = builder.build();
//        return bounds.getCenter();
//    }


    public float getBearing(LatLng P1_LatLng, LatLng P2_LatLng) {

        //라디안 각도로 변환
        double startLat = P1_LatLng.latitude * (3.141592 / 180);
        double startLng = P1_LatLng.longitude * (3.141592 / 180);
        double endLat = P2_LatLng.latitude * (3.141592 / 180);
        double endLng = P2_LatLng.longitude * (3.141592 / 180);

        //두 좌표의 거리
        double radian_distance = Math.acos(Math.sin(startLat) * Math.sin(endLat)
                + Math.cos(startLat) * Math.cos(endLat) * Math.cos(startLng - endLng));

        System.out.println("거리" + radian_distance);

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

    public void receiveAllFlow() {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {
                            flowList.clear();
                            JSONObject jsonResponse = new JSONObject(response);
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

                        LatLng startPoint = flowList.get(0).getLatLng();
                        LatLng endPoint = flowList.get(flowList.size() - 1).getLatLng();
                        endMarker = mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));
                        setEndMarker = true;
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
                //회원번호와 현재 좌표를 보낼 것
                //라라벨에서 현재 좌표와 가까운 노드를 계산
                params.put("patient_id", "1");
                params.put("token", "dslfklsdkflasmfkdsjgkdfkgjskdjfgksdjfksdjfksd");
                return params;
            }
        };

        //test용-------------------------------------------------------------------------
        flowList.clear();
        if (setEndMarker) {
            endMarker.remove();
        }
        flowList.add(new Flow(1, 1, 35.896761232132604, 128.62037402930775));
        flowList.add(new Flow(1, 1, 35.89671595195291, 128.6203835852756));
        flowList.add(new Flow(1, 1, 35.89683636886484, 128.6210034794277));
        flowList.add(new Flow(1, 1, 35.89679227657561, 128.62100719440897));

        LatLng startPoint = flowList.get(0).getLatLng();
        LatLng endPoint = flowList.get(flowList.size() - 1).getLatLng();
        endMarker = mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));
        setEndMarker = true;
        drawPolyline();
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);


    } //receiveAllFlow()


    public void receiveFirstFlow() {

        if (setPolyline)
            polyline.remove();


        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                new Response.Listener<String>() { //응답을 잘 받았을 때 이 메소드가 자동으로 호출
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
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
                        LatLng startPoint = flowList.get(0).getLatLng();
                        LatLng endPoint = flowList.get(flowList.size() - 1).getLatLng();
                        endMarker = mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));
                        setEndMarker = true;
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
                //회원번호와 현재 좌표를 보낼 것
                //라라벨에서 현재 좌표와 가까운 노드를 계산
                params.put("patient_id", "1");
                params.put("token", "dslfklsdkflasmfkdsjgkdfkgjskdjfgksdjfksdjfksd");
                return params;
            }
        };

        //test용-------------------------------------------------------------------------
        flowList.clear();
        if (setEndMarker) {
            endMarker.remove();
        }
        flowList.add(new Flow(1, 1, 35.896761232132604, 128.62037402930775));
        flowList.add(new Flow(1, 1, 35.89671595195291, 128.6203835852756));
        flowList.add(new Flow(1, 1, 35.89683636886484, 128.6210034794277));
        flowList.add(new Flow(1, 1, 35.89679227657561, 128.62100719440897));

        LatLng startPoint = flowList.get(0).getLatLng();
        LatLng endPoint = flowList.get(flowList.size() - 1).getLatLng();
        endMarker = mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));
        setEndMarker = true;
        drawPolyline();
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);


    } //receiveFirstFlow()

    void mapOverlay() {
        mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory
                        .fromResource(R.drawable.map_3th_floor_2))
                .position(schoolPoint, 148f));
    } //mapOverlay()

    void drawPolyline() {
        PolylineOptions polyOpt = new PolylineOptions();
        for (int i = 0; i < flowList.size(); i++) {
            polyOpt.add(flowList.get(i).getLatLng());
            mMap.addMarker(new MarkerOptions()
                    .position(flowList.get(i).getLatLng())
                    .draggable(true))
                    .setTitle(flowList.get(i).getLatLng().toString());

        }


        polyOpt.startCap(new SquareCap());
        polyOpt.endCap(new SquareCap());
        polyOpt.width(25f);
        polyline = mMap.addPolyline(polyOpt);
        setPolyline = true;
    } //drawPolyline()
}