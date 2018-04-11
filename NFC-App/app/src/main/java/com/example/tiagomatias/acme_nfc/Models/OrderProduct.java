package com.example.tiagomatias.acme_nfc.Models;

import android.content.Intent;

import java.io.Serializable;

/**
 * Created by Henrique on 13/03/2018.
 */

public class OrderProduct implements Serializable {

    public String id;
    public int tag_number;
    public String productName;
    public Double productPrice;
    public int quantity;

    public OrderProduct(String id, String name, Double price, int tag_number){
        this.id = id;
        this.productName = name;
        this.productPrice = price;
        this.tag_number = tag_number;
        this.quantity = 0;
    }

    public String getId(){return this.id;}

    public int getTag_number() {
        return tag_number;
    }

    public String getName(){
        return productName;
    }

    public Double getPrice(){
        return productPrice;
    }

    public int getQuantity(){return quantity;}

    public void setName(String productName){
        this.productName = productName;
    }

    public void setPrice(Double productPrice){
        this.productPrice = productPrice;
    }

    public void setQuantity(Integer quantity){this.quantity = quantity;}
}
