package com.example.tiagomatias.acme_client.Models;

import java.util.ArrayList;

/**
 * Created by Henrique on 19/03/2018.
 */

public class Order {


    ArrayList<OrderProduct> products;
    ArrayList<Voucher> vouchers;
    Double price;

    public Order(ArrayList<OrderProduct> products, Double price){
        this.products = products;
        this.price = price;
        this.vouchers = new ArrayList<>();
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
