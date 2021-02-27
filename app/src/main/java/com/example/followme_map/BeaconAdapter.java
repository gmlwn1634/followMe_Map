package com.example.followme_map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.minew.beacon.BeaconValueIndex;
import com.minew.beacon.MinewBeacon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kr.hyosang.coordinate.CoordPoint;
import kr.hyosang.coordinate.TransCoord;


public class BeaconAdapter {
    //-------비콘 정보--------
    private List<MinewBeacon> mMinewBeacons = new ArrayList<>(); // 실시간 수신된 비콘 목록
    private List<BeaconData> recievedBeacons = new ArrayList<>();
    private BeaconList BeaconList;   // LBS용 비콘 목록

    //-------구글맵--------
    public static GoogleMap mMap;         // 구글맵
    public static Marker thisMarker;          // 구글맵 마커
    public static boolean makerState = false;  // 마커는 한개만 유지하는 논리형

    // BeaconAdapter 생성자
    BeaconAdapter(BeaconList argBeaconList, GoogleMap googleMap) {
        BeaconList = argBeaconList; // LBS용 비콘 목록 받기
        mMap = googleMap;           // 구글맵 함수 사용하기 위함
    }

    // 실시간 수신된 받은 모든 비콘 목록 추가
    public void setItems(List<MinewBeacon> newItems) {
        int preSize = 0; // 이전에 수신된 비콘 수
        String mDevice_minor;

        // 이전 리스트값 확인 후 저장된 값이 있을 경우 초기화
        if (this.mMinewBeacons != null) {
            preSize = this.mMinewBeacons.size();
        }
        if (preSize > 0) {
            this.mMinewBeacons.clear();
            this.recievedBeacons.clear();
        }

        // 현재 수신된 비콘 추가
        this.mMinewBeacons.addAll(newItems);

        for (MinewBeacon e : this.mMinewBeacons) {
            mDevice_minor = e.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Minor).getStringValue();
            if (this.BeaconList.checkLBSBeacon(mDevice_minor)) {
                recievedBeaconData a = new recievedBeaconData(e);

                this.recievedBeacons.add(BeaconList.getBeaconInfo(mDevice_minor));
            }
        }

        // RSSI 강도에 따른 값 오름차순 정렬
        Collections.sort(recievedBeacons, new Comparator<BeaconData>() {
            @Override
            public int compare(BeaconData o1, BeaconData o2) {
                if (o1.getK_RSSI() < o2.getK_RSSI())
                    return 1;
                else if (o1.getK_RSSI() == o2.getK_RSSI())
                    return o1.getMinor().compareTo(o2.getMinor());
                else
                    return -1;
            }
        });
        if (recievedBeacons.size() > 2) {
            calculateLocation();

            location();
        }
    }

    // 삼변측량으로 현위치 계산
    void calculateLocation() {
        BeaconData[] selectBeacons = new BeaconData[3];
        int number = 0;

        BeaconData standardBeacon = recievedBeacons.get(0);

        for (int iCount = 0; iCount < recievedBeacons.size(); iCount++) {
            if (standardBeacon.major.equals(recievedBeacons.get(iCount).major)) {
                selectBeacons[number] = recievedBeacons.get(iCount);
                number++;
                if (number == 3) {

                    break;
                }
            }
        }

//        System.out.println("점 ---------------------------------- ");
//        System.out.println("점1 - " + selectBeacons[0].minor + " " + selectBeacons[0].distance);
//        System.out.println("점2 - " + selectBeacons[1].minor + " " + selectBeacons[1].distance);
//        System.out.println("점3 - " + selectBeacons[2].minor + " " + selectBeacons[2].distance);

        if (selectBeacons[0].major == "2") {
            BeaconList.setLat(selectBeacons[0].lat_wgs84);
            BeaconList.setLng(selectBeacons[0].lng_wgs84);
        } else {
            Trilateration tr = new Trilateration(recievedBeacons.get(0), recievedBeacons.get(1), recievedBeacons.get(2));

            CoordPoint pt = new CoordPoint(tr.resultX, tr.resultY);
            CoordPoint TMToWgs84 = TransCoord.getTransCoord(pt, TransCoord.COORD_TYPE_TM, TransCoord.COORD_TYPE_WGS84);

            BeaconList.setLat(TMToWgs84.y);
            BeaconList.setLng(TMToWgs84.x);
        }

//        System.out.println("Result Data: " +  BeaconList.getLat() + ", " +BeaconList.getLng());
    }

//    boolean calculaorsDgree(BeaconData[] argBeaconData) {
//        double[] pointsDistance = new double[3];
//        double x = 0;
//        double y = 0;
//        double distance = 0;
//
//        x = Math.pow(argBeaconData[0].lat_TM - argBeaconData[1].lat_TM, 2);
//        y = Math.pow(argBeaconData[0].lat_TM - argBeaconData[1].lng_TM, 2);
//        pointsDistance[0] = Math.pow(x+y,2);
//
//        x = Math.pow(argBeaconData[1].lat_TM - argBeaconData[2].lat_TM, 2);
//        y = Math.pow(argBeaconData[1].lat_TM - argBeaconData[2].lng_TM, 2);
//        pointsDistance[1] = Math.pow(x+y,2);
//
//        x = Math.pow(argBeaconData[2].lat_TM - argBeaconData[0].lat_TM, 2);
//        y = Math.pow(argBeaconData[2].lat_TM - argBeaconData[0].lng_TM, 2);
//        pointsDistance[2] = Math.pow(x+y,2);
//
//        Arrays.sort(pointsDistance);
//
//        if(pointsDistance[2]/4 > pointsDistance[2] - pointsDistance[0] + pointsDistance[0])
//
//
//        return true;
//    }

    //구글 마커 찍는 함수
    void location() {
        if (makerState) {
            thisMarker.remove();
            makerState = false;
        }
        makerState = true;

//        if(BeaconList.getFloor() == )
        thisMarker = mMap.addMarker(new MarkerOptions()
                .position(BeaconList.getLatLng())
                .anchor(0.5f, 0.5f)
                .rotation(FlowActivity.getChangedAzimut() - FlowActivity.camPosition.bearing)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.this_point)));
    }

    // 삼변측량에 필요한 비콘 3개 선별하는 함수
    List<BeaconData> get3Point() {
        List<BeaconData> resultList = new ArrayList<>();

        return resultList;
    }

    public class recievedBeaconData {
        private MinewBeacon mMinewBeacon;
        private final String mDevice_name;
        private final String mDevice_uuid;
        private final String mDevice_majar;
        private final String mDevice_minor;
        private final double mDevice_rssi;

        recievedBeaconData(MinewBeacon minewBeacon) {
            mMinewBeacon = minewBeacon;
            mDevice_name = mMinewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_MAC).getStringValue();
            mDevice_uuid = mMinewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_UUID).getStringValue();
            mDevice_majar = mMinewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Major).getStringValue();
            mDevice_minor = mMinewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Minor).getStringValue();
            mDevice_rssi = mMinewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).getFloatValue();

            updateRSSI();
            // System.out.printf("Recieved Data - Name: %s, Major: %s, Minor: %s, RSSI: %.2f \n", mDevice_name, mDevice_majar, mDevice_minor, mDevice_rssi);
        }

        void updateRSSI() {
            BeaconData temp = BeaconList.getBeaconInfo(mDevice_minor);
            temp.update(mDevice_rssi);
        }
    }
}
