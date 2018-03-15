package com.example.tiagomatias.acme_client.Models;

/**
 * Created by Henrique on 09/03/2018.
 */

public class Product {

    String id;
    String name;
    Double price;

    public Product(String id, String name, Double price){
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName(){
        return name;
    }

    public Double getPrice(){
        return price;
    }

}
