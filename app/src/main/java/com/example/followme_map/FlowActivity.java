package com.example.followme_map;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.followme_map.databinding.ActivityFlowBinding;
import com.example.followme_map.databinding.ActivityMainBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FlowActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityFlowBinding binding;
    private final String TAG = "FlowActivity";
    private final String URL = "http://192.168.0.8:8000/api/patient/flow";

    // Google Map Values----------
    private GoogleMap mMap;
    private CameraPosition camPosition;
    private int flowMode = 1; // 1->전체동선, 2->첫번째동선 // 처음 실행 시 1 -> 안내 시작 버튼 누르면 2로 전환
    private int naviMode = 1; // 1->기본모드, 2->사용자조작모드 // 처음 실행 시 1 -> 사용자 조작 감지 시 2로 전환

    // Flow Values--------------
    private LatLng schoolPoint = new LatLng(35.896797, 128.620944);
    private ArrayList<Flow> flowList = new ArrayList<Flow>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFlowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //구글맵 오버레이를 위한 프레그먼트
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    } //onCreate()

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mapOverlay();

        //전체 동선 보여주기
        setAllFlow();

    } //onMapReady()

    void setAllFlow() {
        camPosition = new CameraPosition.Builder().target(schoolPoint).zoom(30).bearing(14.7f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));

        //Volley 통신 - 진료동선 수신
        receiveAllFlow();

    } //setAllFlow()

    void setFirstFlow() {
        //현위치를 따라 이동함
        //기본모드, 사용자 조작 모드에 따라 안내 유무

    } //setFirstFlow()

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

        //test용-------------------------------------------------------------------------
        flowList.add(new Flow(1, 1, 35.896732, 128.620416));
        flowList.add(new Flow(1, 1, 35.896789, 128.620399));
        flowList.add(new Flow(1, 1, 35.896724, 128.620368));
        flowList.add(new Flow(1, 1, 35.896780, 128.620351));

        LatLng startPoint = flowList.get(0).getLatLng();
        LatLng endPoint = flowList.get(flowList.size() - 1).getLatLng();
        mMap.addMarker(new MarkerOptions().position(endPoint).title("도착지").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination)));
        drawPolyline();
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requestQueue 초기화 필수
        AppHelper.requestQueue.add(request);


    } //receiveAllFlow()

    void mapOverlay() {
        mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory
                        .fromResource(R.drawable.map_3th_floor_2))
                .position(schoolPoint, 148f));
    } //mapOverlay()

    void drawPolyline() {
        PolylineOptions polyOpt = new PolylineOptions();
        for (int i = 0; i < flowList.size(); i++)
            polyOpt.add(flowList.get(i).getLatLng());

        polyOpt.startCap(new SquareCap());
        polyOpt.endCap(new SquareCap());
        polyOpt.width(25f);
        Polyline polyline = mMap.addPolyline(polyOpt);
    } //drawPolyline()
}