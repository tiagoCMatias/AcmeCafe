package com.example.tiagomatias.acme_client.Models;

import java.security.PrivateKey;
import java.util.ArrayList;

/**
 * Created by Henrique on 19/03/2018.
 */

public class Order {

    String userId;
    ArrayList<OrderProduct> products;
    ArrayList<Voucher> vouchers;
    Double price;

    public Order(String userId, ArrayList<OrderProduct> products, ArrayList<Voucher> vouchers, Double price){
        this.userId = userId;
        this.products = products;
        this.price = price;
        this.vouchers = vouchers;
    }

    public ArrayList<OrderProduct> getProducts() {
        return products;
    }

    public ArrayList<Voucher> getVouchers() {
        return vouchers;
    }

    public Double getPrice() {
        return price;
    }

    public void setProducts(ArrayList<OrderProduct> products) {
        this.products = products;
    }

    public void setVouchers(ArrayList<Voucher> vouchers) {
        this.vouchers = vouchers;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
