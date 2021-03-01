package com.example.followme_map;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.Comparator;

import kr.hyosang.coordinate.CoordPoint;
import kr.hyosang.coordinate.TransCoord;

class Node {
    Node nextNode;
    BeaconData BeaconInfo;

    Node(BeaconData argBeaconInfo, Node argNextNode) {
        BeaconInfo = argBeaconInfo;
        nextNode = argNextNode;
    }
}

public class BeaconList {
    private Node head, tail;
    int numOfData = 0;
    private double TM_lat, TM_lng;       // TM 형식, 칼만 x, (위도, 경도)
    private double TM_K_lat, TM_K_lng;   // TM 형식, 칼만 o, (위도, 경도)
    private double WGS_lat, WGS_lng;     // wgs84 형식, 칼만 o, (위도, 경도)
    private double WGS_K_lat, WGS_K_lng; // wgs84 형식, 칼만 x, (위도, 경도)


    private String floor;                   // 현 층수
    private KalmanFilter mKalmanFilterX; // 위도 칼만필터용 클래스
    private KalmanFilter mKalmanFilterY; // 경도 칼만필터용 클래스


//    private double lat, lng;  //위도, 경도


    //    private double testLat;
//    private double testLng;  //위도, 경도
//    private int testFloor = 1; //test
    public int testNum = 1;


    BeaconList() {
//        if (GlobalVar.mode == 1) {
//            testLat = 35.89666866704047;
//            testLng = 128.62027197619136;
//        } else if (GlobalVar.mode == 2) {
//            testLat = 35.896758278816;
//            testLng = 128.62047268466;
//        }

//        System.out.println("비교 - 모드 : " + GlobalVar.mode);


        tail = new Node(null, null);
        head = new Node(null, tail);
    }


    public void kalman() {
        double x = TM_lat;
        double y = TM_lng;

        if (mKalmanFilterX == null && mKalmanFilterY == null) {
            mKalmanFilterX = new KalmanFilter(x);
            mKalmanFilterY = new KalmanFilter(y);
        }
        //칼만필터를 적용함수
        double filteredX = mKalmanFilterX.update(x);
        double filteredY = mKalmanFilterY.update(y);

        //주석 풀 경우 칼만필터 미적용
        //filteredX = x;
        //filteredY = y;

        //이전 값 저장
        setTM_K_LatLng(filteredX, filteredY);
    }


//    public LatLng getLatLng() {
//        return new LatLng(testLat, testLng);
//    }//test

//    public Double getLat() {
//        return testLat;
//    }//test

//    public Double getLng() {
//        return testLng;
//    }//test


    // 비콘 클래스 추가
    public void add(BeaconData argBeaconInfo) {
        Node temp = head;
        while (temp.nextNode != tail)
            temp = temp.nextNode;

        Node newNode = new Node(argBeaconInfo, tail);
        temp.nextNode = newNode;
        numOfData++;
    }

    // 비콘 정보 업데이트
    public void update(String argMinor, float argK_RSSI) {
        Node temp = head;
        do {
            temp = temp.nextNode;
        } while (!temp.BeaconInfo.getMinor().equals(argMinor));

        temp.BeaconInfo.setK_RSSI(argK_RSSI);
    }

    // 특정 비콘 클래스 얻는 메서드
    public BeaconData getBeaconInfo(String argMinor) {
        Node temp = head;
        do {
            temp = temp.nextNode;
        } while (!temp.BeaconInfo.getMinor().equals(argMinor));

        return temp.BeaconInfo;
    }


    // 전체 목록 확인
    public void getList() {
        Node temp = head;
        int i = 0;
        do {
            i++;
            temp = temp.nextNode;
            System.out.println("Get list " + i + ": " + temp.BeaconInfo.getMinor());
        } while (i != numOfData);
    }


    // LBS용 비콘 확인
    public boolean checkLBSBeacon(String argMinor) {
        Node temp = head.nextNode;
        while (!temp.BeaconInfo.getMinor().equals(argMinor)) {
            temp = temp.nextNode;
            if (temp == tail)
                return false;
        }
        return true;
    }

    public String getFloor() {
        return floor;
    }

//    public int getTestFloor() {
//        return this.testFloor;
//    }


//    public void setTestFloor(int argFloor) {
//        this.testFloor = argFloor;
//    }


    public int size() {
        return numOfData;
    }

    public double getTM_lat() {
        return TM_lat;
    }

    public double getTM_lng() {
        return TM_lng;
    }

    public double getTM_K_lat() {
        return TM_K_lat;
    }

    public double getTM_K_lng() {
        return TM_K_lng;
    }

    public double getWGS_lat() {
        return WGS_lat;
    }

    public double getWGS_lng() {
        return WGS_lng;
    }

    public double getWGS_K_lat() {
        return WGS_K_lat;
    }

    public double getWGS_K_lng() {
        return WGS_K_lng;
    }

    public LatLng getWGS_K_LatLng() {
        return new LatLng(WGS_K_lat, WGS_K_lng);
    }

    public void setFloor(String argFloor) {
        this.floor = argFloor;
    }

    public void setTM_K_LatLng(double argLat, double argLng) {
        this.TM_K_lat = argLat;
        this.TM_K_lng = argLng;

        CoordPoint pt = new CoordPoint(this.TM_K_lat, this.TM_K_lng);
        CoordPoint TMToWgs84 = TransCoord.getTransCoord(pt, TransCoord.COORD_TYPE_TM, TransCoord.COORD_TYPE_WGS84);

        this.WGS_K_lat = TMToWgs84.y;
        this.WGS_K_lng = TMToWgs84.x;
    }


    public void setWGS_K_LatLng(double argLat, double argLng) {
        this.WGS_K_lat = argLat;
        this.WGS_K_lng = argLng;

        CoordPoint pt = new CoordPoint(this.WGS_K_lat, this.WGS_K_lng);
        CoordPoint Wgs84ToTM = TransCoord.getTransCoord(pt, TransCoord.COORD_TYPE_WGS84, TransCoord.COORD_TYPE_TM);

        this.TM_K_lat = Wgs84ToTM.y;
        this.TM_K_lng = Wgs84ToTM.x;

    }

    public void setTM_LatLng(double argLat, double argLng) {
        this.TM_lat = argLat;
        this.TM_lng = argLng;

        CoordPoint pt = new CoordPoint(this.TM_lat, this.TM_lng);
        CoordPoint TMToWgs84 = TransCoord.getTransCoord(pt, TransCoord.COORD_TYPE_TM, TransCoord.COORD_TYPE_WGS84);

        this.WGS_lat = TMToWgs84.y;
        this.WGS_lng = TMToWgs84.x;
    }

    public void setWGS_LatLng(double argLat, double argLng) {
        this.WGS_lat = argLat;
        this.WGS_lng = argLng;

        CoordPoint pt = new CoordPoint(this.WGS_lat, this.WGS_lng);
        CoordPoint Wgs84ToTM = TransCoord.getTransCoord(pt, TransCoord.COORD_TYPE_WGS84, TransCoord.COORD_TYPE_TM);

        this.TM_lat = Wgs84ToTM.y;
        this.TM_lng = Wgs84ToTM.x;

    }

    // 칼만필터 적용을 위한 클래스
    private class KalmanFilter {
        private double Q = 0.00001;
        private double R = 0.001;
        private double X = 0, P = 1, K;

        //첫번째값을 입력받아 초기화 한다. 예전값들을 계산해서 현재값에 적용해야 하므로 반드시 하나이상의 값이 필요하므로~
        KalmanFilter(double initValue) {
            X = initValue;
        }

        //예전값들을 공식으로 계산한다
        private void measurementUpdate() {
            K = (P + Q) / (P + Q + R);
            P = R * (P + Q) / (R + P + Q);
        }

        //현재값을 받아 계산된 공식을 적용하고 반환한다
        public double update(double measurement) {
            measurementUpdate();
            X = X + (measurement - X) * K;

            return X;
        }
    }

}
