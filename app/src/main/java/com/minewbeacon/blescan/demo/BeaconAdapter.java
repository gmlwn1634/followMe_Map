package com.minewbeacon.blescan.demo;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.minew.beacon.BeaconValueIndex;
import com.minew.beacon.MinewBeacon;
import com.yuliwuli.blescan.demo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class BeaconAdapter {
    //-------비콘 정보--------
    private List<MinewBeacon> mMinewBeacons = new ArrayList<>(); // 실시간 수신된 비콘 목록
    private List<BeaconData> recievedBeacons = new ArrayList<>();
    private BeaconList BeaconList;   // LBS용 비콘 목록


    //-------구글맵--------
    private GoogleMap mMap;         // 구글맵
    public static Marker thisMarker;          // 구글맵 마커
    private boolean makerState = false;  // 마커는 한개만 유지하는 논리형

//    public int num = 0;
//    public static LatLng testLatLng;

    // BeaconAdapter 생성자
    BeaconAdapter(BeaconList argBeaconList, GoogleMap googleMap) {
        BeaconList = argBeaconList; // LBS용 비콘 목록 받기
        mMap = googleMap;           // 구글맵 함수 사용하기 위함
//        testLatLng = argBeaconList.getLatLng();
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
        int standard = 0;
        while (number < 3) {
            number = 0;
            BeaconData standardBeacon = recievedBeacons.get(standard);
            BeaconList.setFloor(standardBeacon.getMajor());

            for (int iCount = 0; iCount < recievedBeacons.size(); iCount++) {
                if (standardBeacon.group.equals(recievedBeacons.get(iCount).group)) {
                    selectBeacons[number] = recievedBeacons.get(iCount);
                    number++;
                    if (number == 3) {
                        break;
                    }
                }
            }
            standard++;
        }

//        System.out.println("점 ---------------------------------- ");
//        System.out.println("점비1 - " + recievedBeacons.get(0).minor + " " + recievedBeacons.get(0).distance);
//        System.out.println("점비2 - " + recievedBeacons.get(1).minor + " " + recievedBeacons.get(1).distance);
//        System.out.println("점비3 - " + recievedBeacons.get(2).minor + " " + recievedBeacons.get(2).distance);
//        System.out.println("점1 - " + selectBeacons[0].minor + " " + selectBeacons[0].distance);
//        System.out.println("점2 - " + selectBeacons[1].minor + " " + selectBeacons[1].distance);
//        System.out.println("점3 - " + selectBeacons[2].minor + " " + selectBeacons[2].distance);
//        System.out.println("점 층수 - " + BeaconList.getFloor());

        Trilateration tr = new Trilateration(BeaconList.getTM_lat(), BeaconList.getTM_lng(), selectBeacons[0], selectBeacons[1], selectBeacons[2]);
        BeaconList.setTM_LatLng(tr.resultX, tr.resultY);
        BeaconList.kalman(tr.doKalman);

    }

    //구글 마커 찍는 함수
    void location() {
        if (makerState) {
            thisMarker.remove();
            makerState = false;
        }
        makerState = true;
        if (GlobalVar.mode == 1) {

            thisMarker = mMap.addMarker(new MarkerOptions()
                    .position(BeaconList.getWGS_K_LatLng())
                    .anchor(0.5f, 0.5f)
                    .rotation(FlowActivity.getChangedAzimut() - FlowActivity.camPosition.bearing)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.this_point)));

        } else if (GlobalVar.mode == 2) {
            thisMarker = mMap.addMarker(new MarkerOptions()
                    .position(BeaconList.getWGS_K_LatLng())
                    .anchor(0.5f, 0.5f)
                    .rotation(DestSearchActivity.getChangedAzimut() - DestSearchActivity.camPosition.bearing)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.this_point)));
        }
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
