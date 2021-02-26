package com.example.followme_map;

import com.google.android.gms.maps.model.LatLng;

public class FlowNode {
    private int id;
    private int floor; //test
    private LatLng latLng;
    private int stairCheck;
    private int index;

    //test용
//    public Node(int id, int floor, double lat, double lng, int stairCheck, int index) {
//        this.id = id;
//        this.floor = floor;
//        this.latLng = new LatLng(lat, lng);
//        this.stairCheck = stairCheck;
//        this.index = index;
//    }


    public int getId() {
        return this.id;
    }

    public int getFloor() {
        return this.floor;
    }

    public LatLng getLatLng() {
        return this.latLng;
    }

    public int getStairCheck() {
        return this.stairCheck;
    }


    public int getIndex() {
        return this.index;
    }

    public void setId(int argId) {
        this.id = argId;
    }

    public void setFloor(int argFloor) {
        this.floor = argFloor;
    }


    public void setLatLng(double argLat, double argLng) {
        this.latLng = new LatLng(argLat, argLng);
    }

    public void setStairCheck(int argStairCheck) {
        this.stairCheck = argStairCheck;
    }

    public void setIndex(int argIndex) {
        this.index = argIndex;
    }
}
