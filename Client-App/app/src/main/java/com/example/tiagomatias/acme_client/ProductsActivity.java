package com.example.tiagomatias.acme_client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.tiagomatias.acme_client.Adapters.ProductsListAdapter;
import com.example.tiagomatias.acme_client.Models.Product;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ProductsActivity extends AppCompatActivity {

    ProductsListAdapter adapter;
    ArrayList<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        ImageButton shop = findViewById(R.id.shop);
        shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                makeOrder();
            }
        });

        getProducts();
        showProducts(products);
    }

    public void showProducts(ArrayList<Product> productsList){
        final ListView products = findViewById(R.id.order_products);

        adapter =  new ProductsListAdapter(this,
                R.layout.products_item, productsList);

        products.setAdapter(adapter);
    }

    public void makeOrder(){
        Intent intent = new Intent(ProductsActivity.this, OrderActivity.class);
        startActivity(intent);
    }

    public void getProducts(){
        GetProduct getProduct = new GetProduct("/product/all");
        Thread thr = new Thread(getProduct);
        thr.start();
        try {
            thr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        String response = getProduct.response;

        createProductObject(response);

    }

    public void createProductObject(String response){

        try {
            JSONArray json = new JSONArray(response);
            for (int i = 0; i< json.length(); i++){
                String id = (String) json.getJSONObject(i).get("_id");
                String name = (String) json.getJSONObject(i).get("name");
                Double price = (Double) json.getJSONObject(i).get("price");

                Product p = new Product(id, name, price);

                this.products.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
