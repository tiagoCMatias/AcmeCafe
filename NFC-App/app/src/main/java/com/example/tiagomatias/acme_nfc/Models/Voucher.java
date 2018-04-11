package com.example.tiagomatias.acme_nfc.Models;

/**
 * Created by Henrique on 19/03/2018.
 */

public class Voucher {

    public String id;
    public String type;

    public Voucher(String id, String type){
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
