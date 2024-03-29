package com.erimali.cntrygame;

import javafx.scene.shape.SVGPath;

public class SVGProvince extends SVGPath {
    //private static int CURRPROVINCEID = 0;
    private int ownerId;
    private int provId;
    //private AdmDiv admDiv;

    public SVGProvince(int ownerId, int provId){
        this.provId = provId;
        this.ownerId = ownerId;
    }


    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getProvId() {
        return provId;
    }

    public void setProvId(int provId) {
        this.provId = provId;
    }



}
