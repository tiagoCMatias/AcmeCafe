package com.example.tiagomatias.acme_client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tiagomatias.acme_client.Adapters.ConfirmOrderProductsListAdapter;
import com.example.tiagomatias.acme_client.Models.OrderProduct;


import java.util.ArrayList;

public class OrderConfirmActivity extends AppCompatActivity {

    ConfirmOrderProductsListAdapter adapter;
    ArrayList<OrderProduct> productsSelected = new ArrayList<>();
    Double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        Bundle extras = getIntent().getExtras();

        productsSelected = (ArrayList<OrderProduct>) extras.getSerializable("productsSelected");
        price = extras.getDouble("price");
        Double priceRound = Math.round(price * 100.0)/100.0;

        TextView price = findViewById(R.id.price);
        price.setText(String.valueOf(priceRound));

        showProducts(productsSelected);
    }


    public void showProducts(ArrayList<OrderProduct> productsList){
        final ListView products = findViewById(R.id.order_products);

        adapter =  new ConfirmOrderProductsListAdapter(this,
                R.layout.confirm_order_products_item, productsList);

        products.setAdapter(adapter);
    }
}
