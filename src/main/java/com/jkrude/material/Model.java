package com.jkrude.material;

import java.util.ArrayList;
import java.util.List;

public class Model {
    private List<Camt> camtList;

    public Model(List<Camt> camtList) {
        if(camtList == null)
            throw new NullPointerException();
        this.camtList = camtList;
    }

    public Model() {
        camtList = new ArrayList<>();
    }

    public List<Camt> getCamtList() {
        return camtList;
    }
}
