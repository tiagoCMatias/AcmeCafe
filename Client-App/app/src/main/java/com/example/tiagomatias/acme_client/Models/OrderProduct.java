package com.example.tiagomatias.acme_client.Models;

import android.content.Intent;

import java.io.Serializable;

/**
 * Created by Henrique on 13/03/2018.
 */

public class OrderProduct implements Serializable {

    String id;
    String productName;
    Double productPrice;
    int quantity;

    public OrderProduct(String id, String name, Double price){
        this.id = id;
        this.productName = name;
        this.productPrice = price;
        this.quantity = 0;
    }

    public String getId(){return this.id;}

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
