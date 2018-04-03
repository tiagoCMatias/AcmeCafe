package com.example.tiagomatias.acme_client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class OrderFinalInfoActivity extends AppCompatActivity {

    Double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_final_info);

        Bundle extras = getIntent().getExtras();
        price = extras.getDouble("price");
        Double priceRound = Math.round(price * 100.0)/100.0;

        TextView success = findViewById(R.id.orderText);
        success.setText("Order Confirmed");

        TextView price = findViewById(R.id.finalPrice);
        price.setText(String.valueOf(priceRound) + " €");

    }
}
