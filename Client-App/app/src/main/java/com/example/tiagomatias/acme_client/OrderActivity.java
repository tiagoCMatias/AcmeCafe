package com.example.tiagomatias.acme_client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.example.tiagomatias.acme_client.Adapters.OrderProductsListAdapter;
import com.example.tiagomatias.acme_client.Models.OrderProduct;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class OrderActivity extends AppCompatActivity {

    OrderProductsListAdapter adapter;
    ArrayList<OrderProduct> products = new ArrayList<>();
    ArrayList<OrderProduct> productsSelected = new ArrayList<>();
    Double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Button confirm = findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                makeOrder();

                Intent i = new Intent(OrderActivity.this, OrderConfirmActivity.class);
                i.putExtra("productsSelected", productsSelected);
                i.putExtra("price", price);
                startActivity(i);

            }
        });


        getProducts();
        showProducts(products);

    }

    public void showProducts(ArrayList<OrderProduct> productsList){
        final ListView products = findViewById(R.id.order_products);

        adapter =  new OrderProductsListAdapter(this,
                R.layout.order_item, productsList);

        products.setAdapter(adapter);
    }

    public void getProducts(){

        GetProduct getProduct = new GetProduct("/product/all");
        Thread thr = new Thread(getProduct);
        thr.start();
        try {
            thr.join();
            String response = getProduct.response;
            createProductObject(response);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void createProductObject(String response){

        try {
            JSONArray json = new JSONArray(response);
            for (int i = 0; i< json.length(); i++){
                String id = (String) json.getJSONObject(i).get("_id");
                String name = (String) json.getJSONObject(i).get("name");
                Double price = (Double) json.getJSONObject(i).get("price");
                Integer tag_number = (Integer)json.getJSONObject(i).get("tag_number");

                OrderProduct p = new OrderProduct(id, name, price, tag_number);

                this.products.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void makeOrder(){
        productsSelected.clear();
        for (OrderProduct o: adapter.orderProducts) {
            if(o.getQuantity()>0){
                productsSelected.add(o);
            }
        }

        price = calculatePrice(productsSelected);
    }

    public Double calculatePrice(ArrayList<OrderProduct> productsSelected){
        Double price = 0.0;

        for (OrderProduct p: productsSelected) {
            price += (p.getPrice() * p.getQuantity());
        }

        return price;
    }
}
