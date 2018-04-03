package com.example.tiagomatias.acme_client;

import android.app.Application;

/**
 * Created by Henrique on 03/04/2018.
 */

public class GlobalVariables extends Application {

    public static String url = "http://172.30.25.111:3000";

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
