package com.example.followme_map;

public class NearNode {
    double dist;
    int index;

    NearNode(double dist, int index) {
        this.dist = dist;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
