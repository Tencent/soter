package com.tencent.soter.serverdemo;

public class SoterPubKeyModel {

    private long counter = -1;
    private int uid = -1;
    private String cpu_id = "";

    public SoterPubKeyModel() {

    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public void setCpu_id(String cpu_id) {
        this.cpu_id = cpu_id;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "SoterPubKeyModel{" +
                "counter=" + counter +
                ", uid=" + uid +
                ", cpu_id='" + cpu_id + '\'' +
                '}';
    }
}
