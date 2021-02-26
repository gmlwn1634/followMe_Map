package com.example.followme_map;

import com.google.android.gms.maps.model.LatLng;

public class Node {
    private int id;
    private int floor = 2; //test
    private LatLng latLng;
    private int stairCheck;
    private int index;

    //test용
    public Node(int argId, int argFloor, double argLat, double argLng, int argStairCheck, int argIndex) {
        this.id = argId;
        this.floor = argFloor;
        this.latLng = new LatLng(argLat, argLng);
        this.stairCheck = argStairCheck;
        this.index = argIndex;
    }


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
