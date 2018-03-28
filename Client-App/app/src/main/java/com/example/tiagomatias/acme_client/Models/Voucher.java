package com.example.tiagomatias.acme_client.Models;

/**
 * Created by Henrique on 19/03/2018.
 */

public class Voucher {

    //Integer id;
    String type;

    public Voucher(String type){
        //this.id = id;
        this.type = type;
    }

    public Integer getId() {
        return 0;
    }

    public String getType() {
        return type;
    }
}
