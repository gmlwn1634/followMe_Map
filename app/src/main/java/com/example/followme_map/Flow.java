package com.example.followme_map;

import com.google.android.gms.maps.model.LatLng;

public class Flow {
        private int minor;
        private int floor;
        private LatLng latLng;

        //test용
        public Flow(int argMinor, int argFloor, double argLat, double argLng) {
            minor = argMinor;
            floor = argFloor;
            latLng = new LatLng(argLat, argLng);
        }


        public int getMinor() {
            return minor;
        }

        public int getFloor() {
            return floor;
        }

        public LatLng getLatLng() {
            return latLng;
        }

        public void setMinor(int argMinor) {
            this.minor = argMinor;
        }

        public void setFloor(int argFloor) {
            this.floor = argFloor;
        }

        public void setLatLng(double argLat, double argLng) {
            this.latLng = new LatLng(argLat, argLng);
        }
    }
