package com.example.tiagomatias.acme_client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.tiagomatias.acme_client.Models.Product;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ProductsActivity extends AppCompatActivity {

    ProductsListAdapter adapter;

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

        Product p1 = new Product("Expresso", 1.00);
        Product p2 = new Product("Tosta mista sem fiambre", 2.00);
        Product p3 = new Product("Um café longo com dois cubos de gelo", 3.00);

        ArrayList<Product> products = new ArrayList<>();
        products.add(p1);
        products.add(p2);
        products.add(p3);

        //showProducts(products);

        getProducts();
    }

    public void showProducts(ArrayList<Product> productsList){
        final ListView products = findViewById(R.id.products);

        adapter =  new ProductsListAdapter(this,
                R.layout.products_item, productsList);

        products.setAdapter(adapter);
    }

    public void makeOrder(){
        Intent intent = new Intent(ProductsActivity.this, OrderActivity.class);
        startActivity(intent);
    }

    public void getProducts(){
        GetProduct getProduct = new GetProduct("10.0.2.2:3000/product/all");
        Thread thr = new Thread(getProduct);
        thr.start();


    }
}
