package com.minewbeacon.blescan.demo;

import kr.hyosang.coordinate.CoordPoint;
import kr.hyosang.coordinate.TransCoord;

public class BeaconData {
    String minor;
    String major;
    double lat_wgs84;
    double lng_wgs84;
    double lat_TM;
    double lng_TM;
    double K_lat_TM;
    double K_lng_TM;
    double K_RSSI = 0;
    double distance = 0;
    String group;

    // 칼만필터 값
    private double Q = 0.00005  ;
    private double R = 0.001;
    private double P = 1, K;

    BeaconData(String argGroup, String argMajor, String argMinor, double argLat, double argLng){
        group = argGroup;
        major = argMajor;
        minor = argMinor;
        lat_wgs84 = argLat;
        lng_wgs84 = argLng;

        System.out.println("wgs84 "+argLat + ", " + argLng);
        setTMLocation();
    }



    String getMinor()       {return this.minor;}
    String getMajor()       {return this.major;}
    double getLat_wgs84()   {return this.lat_wgs84;}
    double getLng_wgs84()   {return this.lng_wgs84;}
    double getLat_TM()      {return this.lat_TM;}
    double getLng_TM()      {return this.lng_TM;}
    double getK_RSSI()      {return this.K_RSSI;}
    double getDistance()    {return this.distance;}

    void setMinor(String argMinor)       {this.minor = argMinor;}
    void setMajor(String argMajor)       {this.minor = argMajor;}
    void setLat_wgs84(double argLat)     {this.lat_TM = argLat;}
    void setLng_wgs84(double argLng)     {this.lng_TM = argLng;}
    void setLat_TM(double argLat)        {this.lat_TM = argLat;}
    void setLng_TM(double argLng)        {this.lng_TM = argLng;}
    void setK_RSSI(double argK_RSSI)     {this.K_RSSI = argK_RSSI;}
    void setDistance(double argDistance) {this.distance = argDistance;}

    //예전값들을 공식으로 계산한다
    private void measurementUpdate(){
        K = (P + Q) / (P + Q + R);
        P = R * (P + Q) / (R + P + Q);
    }
    //현재값을 받아 계산된 공식을 적용하고 반환한다
    public double update(double measurement){
        measurementUpdate();
        K_RSSI = K_RSSI + (measurement - K_RSSI) * K;
        distance = calculateDistance(K_RSSI);
        return K_RSSI;
    }


    double calculateDistance(double rssi) {
        double txPower = -64;
        if (rssi == 0) {
            return -1.0f;
        }
        double ratio = (rssi*1.0/txPower);
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double distance =((0.89976)*Math.pow(ratio,7.7095) + 0.111);

            if(distance > 8.5)
                return 8.5;
            return distance;
        }
    }


    public void setTMLocation() {
        CoordPoint pt = new CoordPoint(this.lng_wgs84, this.lat_wgs84);
        CoordPoint wgsToTM = TransCoord.getTransCoord(pt, TransCoord.COORD_TYPE_WGS84, TransCoord.COORD_TYPE_TM);

        this.lat_TM = wgsToTM.x;
        this.lng_TM = wgsToTM.y;
    }
}
