package com.example.tiagomatias.acme_client;

import android.app.Application;

/**
 * Created by Henrique on 03/04/2018.
 */

public class GlobalVariables extends Application {

    public static String url = "http://192.168.1.179:3000";
    public static String voucherCoffee = "Free Coffee";
    public static String voucherDiscount = "Discount 5%";
    public static Double coffeePrice = 0.6;
    public static Double discount = 0.05;


    public String getUrl() {
        return url;
    }

    public static String getVoucherCoffe() {
        return voucherCoffee;
    }

    public static String getVoucherDiscount() {
        return voucherDiscount;
    }

    public static Double getCoffeePrice() {
        return coffeePrice;
    }
}