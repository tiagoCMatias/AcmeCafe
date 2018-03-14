package com.example.tiagomatias.acme_client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.example.tiagomatias.acme_client.Models.OrderProduct;
import com.example.tiagomatias.acme_client.Models.Product;

import java.util.ArrayList;

public class OrderActivity extends AppCompatActivity {

    OrderProductsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        OrderProduct p1 = new OrderProduct("Expresso", 1.00);
        OrderProduct p2 = new OrderProduct("Tosta mista sem fiambre", 2.00);
        OrderProduct p3 = new OrderProduct("Um café longo com dois cubos de gelo", 3.00);

        ArrayList<OrderProduct> products = new ArrayList<>();
        products.add(p1);
        products.add(p2);
        products.add(p3);

        showProducts(products);

    }

    public void showProducts(ArrayList<OrderProduct> productsList){
        final ListView products = findViewById(R.id.products);

        adapter =  new OrderProductsListAdapter(this,
                R.layout.order_item, productsList);

        products.setAdapter(adapter);
    }
}
