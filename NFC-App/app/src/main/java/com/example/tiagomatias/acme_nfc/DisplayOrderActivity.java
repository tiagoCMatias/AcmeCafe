package com.example.tiagomatias.acme_nfc;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import java.util.ArrayList;

public class DisplayOrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_order);

        this.presentOrder();
    }

    private void presentOrder() {
        ArrayList<String> order = getIntent().getStringArrayListExtra("order");
        String orderNum = order.get(0);
        String[] products = order.get(1).split(";");
        String[] vouchers = order.get(2).split(";");
        String price = order.get(3);

        // set order number
        TextView err = findViewById(R.id.orderNumber);
        err.setText(orderNum);

        // set order products
        LinearLayout productList = findViewById(R.id.productsList);
        for (String product : products) {
            productList.addView(new TextView(this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            TextView productView = (TextView)productList.getChildAt(productList.getChildCount() - 1);
            productView.setTextColor(Color.parseColor("#000000"));
            productView.setText(product);
        }

        // set order vouchers
        LinearLayout voucherList = findViewById(R.id.vouchersList);
        for (String voucher : vouchers) {
            voucherList.addView(new TextView(this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            TextView voucherView = (TextView)voucherList.getChildAt(voucherList.getChildCount() - 1);
            voucherView.setTextColor(Color.parseColor("#000000"));
            voucherView.setText(voucher);
        }

        // set order price
        err = findViewById(R.id.price);
        err.setText(price);
    }

}
