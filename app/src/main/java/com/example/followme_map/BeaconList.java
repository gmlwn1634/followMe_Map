package com.example.followme_map;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.Comparator;

class Node {
    Node nextNode;
    BeaconData BeaconInfo;

    Node(BeaconData argBeaconInfo, Node argNextNode) {
        BeaconInfo = argBeaconInfo;
        nextNode = argNextNode;
    }
}

public class BeaconList {
    Node head, tail;
    int numOfData = 0;
    private double lat, lng;  //위도, 경도
    private int floor = 1; //test

    BeaconList() {
        tail = new Node(null, null);
        head = new Node(null, tail);
    }

    private boolean IsEmpty() {
        if (numOfData == 0)
            return true;
        return false;
    }

    public void add(BeaconData argBeaconInfo) {
        Node temp = head;
        while (temp.nextNode != tail)
            temp = temp.nextNode;

        Node newNode = new Node(argBeaconInfo, tail);
        temp.nextNode = newNode;
        numOfData++;

        //System.out.println("add Data Minor: "+newNode.BeaconInfo.getMinor()+", List Size: " + numOfData);
    }

    public void update(String argMinor, float argK_RSSI) {
        Node temp = head;
        do {
            temp = temp.nextNode;
        } while (!temp.BeaconInfo.getMinor().equals(argMinor));

        temp.BeaconInfo.setK_RSSI(argK_RSSI); // 값 변경
        //System.out.println("Update Minor: " + temp.BeaconInfo.getMinor() + ", K_RSSI: " + temp.BeaconInfo.getK_RSSI());
    }

    public BeaconData getBeaconInfo(String argMinor) {
        Node temp = head;
        do {
            temp = temp.nextNode;
        } while (!temp.BeaconInfo.getMinor().equals(argMinor));

        return temp.BeaconInfo;
    }

    public void getList() {
        Node temp = head;
        int i = 0;
        do {
            i++;
            temp = temp.nextNode;
            System.out.println("Get list " + i + ": " + temp.BeaconInfo.getMinor());
        } while (i != numOfData);
    }

    public int[][] sortList() {
        Node temp = head.nextNode;
        int[][] sortList = new int[numOfData][2];

        for (int i = 0; i < numOfData; i++) {
            sortList[i][0] = Integer.parseInt(temp.BeaconInfo.minor);
            sortList[i][1] = (int) Math.round(temp.BeaconInfo.distance * 10);

            temp = temp.nextNode;
        }
        Arrays.sort(sortList, new Comparator<int[]>() {
            @Override
            public int compare(int[] t1, int[] t2) {
                return t1[1] - t2[1];
            }
        });

        for (int i = 0; i < numOfData; i++) {
            System.out.println(i + 1 + ": " + sortList[i][0] + ", " + sortList[i][1]);
        }

        return sortList;
    }

    public boolean checkLBSBeacon(String argMinor) {
        Node temp = head.nextNode;
        while (!temp.BeaconInfo.getMinor().equals(argMinor)) {
            temp = temp.nextNode;
            if (temp == tail)
                return false;
        }
        return true;
    }


    public double getLat() {
        System.out.println("오ㅑ 안대+" + this.lat);
//        return this.lat;
        return 35.89675110987505;
    }


    public double getLng() {
        System.out.println("오ㅑ 안대+" + this.lng);
//        return this.lng;
        return 128.6204937327133;
    }

    public LatLng getLatLng() {

//        return new LatLng(this.lat, this.lng);
//        return new LatLng(35.89656366127246, 128.62022174135475);
        return new LatLng(35.89675110987505, 128.6204937327133);
    }

    public int getFloor() {
        return this.floor;
    }

    public void setLat(double argLat) {
        this.lat = argLat;
    }

    public void setLng(double argLng) {
        this.lng = argLng;
    }

    public void setFloor(int argFloor) {
        this.floor = argFloor;
    }


}
