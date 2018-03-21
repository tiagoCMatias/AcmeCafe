package com.example.tiagomatias.acme_client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tiagomatias.acme_client.Adapters.ConfirmOrderProductsListAdapter;
import com.example.tiagomatias.acme_client.Models.Order;
import com.example.tiagomatias.acme_client.Models.OrderProduct;
import com.example.tiagomatias.acme_client.Models.Voucher;


import java.io.Serializable;
import java.util.ArrayList;

public class OrderConfirmActivity extends AppCompatActivity {

    ConfirmOrderProductsListAdapter adapter;
    ArrayList<OrderProduct> productsSelected = new ArrayList<>();
    ArrayList<Voucher> vouchers = new ArrayList<>();
    Double price;

    Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        Bundle extras = getIntent().getExtras();

        productsSelected = (ArrayList<OrderProduct>) extras.getSerializable("productsSelected");
        price = extras.getDouble("price");
        Double priceRound = Math.round(price * 100.0)/100.0;

        TextView price = findViewById(R.id.price);
        price.setText(String.valueOf(priceRound) + " €");

        showProducts(productsSelected);

        Button confirm = findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                order = makeOrder();
                nfcCall();
            }
        });
    }


    public void showProducts(ArrayList<OrderProduct> productsList){
        final ListView products = findViewById(R.id.order_products);

        adapter =  new ConfirmOrderProductsListAdapter(this,
                R.layout.confirm_order_products_item, productsList);

        products.setAdapter(adapter);
    }

    public Order makeOrder(){

        String userId = null;

        Order o = new Order(userId, this.productsSelected, this.vouchers, this.price);

        return o;
    }

    public void nfcCall(){
        Intent intent = new Intent(OrderConfirmActivity.this, NfcActivity.class);
        intent.putExtra("order", (Serializable) order);
    }
}
