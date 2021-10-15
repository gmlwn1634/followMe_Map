package com.minewbeacon.blescan.demo;

public class Flow {

    private int flowId;
    private int patientId;
    private int flowSequence;
    private int flowStatus; //완료, 미완료 체크
    private int roomLocationID;
    private int roomNode;
    private String roomName;

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public void setFlowSequence(int flowSequence) {
        this.flowSequence = flowSequence;
    }

    public void setFlowStatus(int flowStatus) {
        this.flowStatus = flowStatus;
    }

    public void setRoomLocationID(int roomLocationID) {
        this.roomLocationID = roomLocationID;
    }

    public void setRoomNode(int roomNode) {
        this.roomNode = roomNode;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }


    public int getFlowId() {
        return this.flowId;
    }

    public int getPatientId() {
        return this.patientId;
    }

    public int getFlowSequence() {
        return this.flowSequence;
    }

    public int getFlowStatus() {
        return this.flowStatus;
    }

    public int getRoomLocationID() {
        return this.roomLocationID;
    }

    public int getRoomNode() {
        return this.roomNode;
    }

    public String getRoomName() {
        return this.roomName;
    }

}
