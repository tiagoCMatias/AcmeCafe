package com.example.tiagomatias.acme_client.Models;

import android.content.Intent;

/**
 * Created by Henrique on 13/03/2018.
 */

public class OrderProduct {

    String id;
    String productName;
    Double productPrice;
    Integer quantity;

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

    public Integer getQuantity(){return quantity;}


    public void setName(String productName){
        this.productName = productName;
    }

    public void setPrice(Double productPrice){
        this.productPrice = productPrice;
    }

    public void setQuantity(Integer quantity){this.quantity = quantity;}
}
