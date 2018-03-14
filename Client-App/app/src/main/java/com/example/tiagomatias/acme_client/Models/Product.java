package com.example.tiagomatias.acme_client.Models;

/**
 * Created by Henrique on 09/03/2018.
 */

public class Product {

    String name;
    Double price;

    public Product(String name, Double price){
        this.name = name;
        this.price = price;
    }

    public String getName(){
        return name;
    }

    public Double getPrice(){
        return price;
    }

}
