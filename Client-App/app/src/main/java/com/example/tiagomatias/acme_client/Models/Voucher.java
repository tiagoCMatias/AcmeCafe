package com.example.tiagomatias.acme_client.Models;

/**
 * Created by Henrique on 19/03/2018.
 */

public class Voucher {

    public Integer id;
    public String type;

    public Voucher(Integer id, String type){
        this.id = id;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
