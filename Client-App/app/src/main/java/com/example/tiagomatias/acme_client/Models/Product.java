package com.example.tiagomatias.acme_client.Models;

/**
 * Created by Henrique on 09/03/2018.
 */

public class Product {

    String id;
    String name;
    Double price;
    Integer tag_number;

    public Product(String id, String name, Double price, Integer tag_number){
        this.id = id;
        this.name = name;
        this.price = price;
        this.tag_number = tag_number;
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
    public Integer getTag_number() {
        return tag_number;
    }
}
