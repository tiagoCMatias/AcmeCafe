package com.example.tiagomatias.acme_client.Models;

/**
 * Created by Henrique on 19/03/2018.
 */

public class Voucher {

    String id;
    String type;

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
