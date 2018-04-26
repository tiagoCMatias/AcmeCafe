package com.example.tiagomatias.acme_client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tiagomatias.acme_client.Adapters.ConfirmOrderProductsListAdapter;
import com.example.tiagomatias.acme_client.Adapters.ConfirmOrderVouchersListAdapter;
import com.example.tiagomatias.acme_client.Models.Order;
import com.example.tiagomatias.acme_client.Models.OrderProduct;
import com.example.tiagomatias.acme_client.Models.Voucher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OrderConfirmActivity extends AppCompatActivity {

    ConfirmOrderProductsListAdapter adapterProducts;
    ConfirmOrderVouchersListAdapter adapterVouchers;
    ArrayList<OrderProduct> productsSelected = new ArrayList<>();
    ArrayList<Voucher> vouchers = new ArrayList<>();
    Double price;
    String userId;

    Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        SharedPreferences settings = getSharedPreferences("user_info", MODE_PRIVATE);
        this.userId = settings.getString("userId", "not found");

        Bundle extras = getIntent().getExtras();

        productsSelected = (ArrayList<OrderProduct>) extras.getSerializable("productsSelected");
        price = extras.getDouble("price");
        Double priceRound = Math.round(price * 100.0)/100.0;

        TextView price = findViewById(R.id.price);
        price.setText(String.valueOf(priceRound) + " €");

        showProducts(productsSelected);
        showVouchers();

        Button confirm = findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                order = makeOrder();
                nfcCall();

                SendOrder send = new SendOrder(order);
                Thread thr = new Thread(send);
                thr.start();
                try {
                    thr.join();
                    Intent i = new Intent(OrderConfirmActivity.this, OrderFinalInfoActivity.class);
                    i.putExtra("price", order.getPrice());
                    startActivity(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        this.vouchers.clear();
        showVouchers();
    }


    public void showProducts(ArrayList<OrderProduct> productsList){
        final ListView products = findViewById(R.id.order_products);

        adapterProducts =  new ConfirmOrderProductsListAdapter(this,
                R.layout.confirm_order_products_item, productsList);

        products.setAdapter(adapterProducts);
    }

    public void showVouchers(){
        getVouchers();

        ArrayList<Voucher> vouchersAvailable = vouchersThatCanBeUsed();

        final ListView vouchers = findViewById(R.id.order_vouchers);

        adapterVouchers =  new ConfirmOrderVouchersListAdapter(this,
                R.layout.confirm_order_vouchers_item, vouchersAvailable);

        vouchers.setAdapter(adapterVouchers);
    }

    public ArrayList<Voucher> vouchersThatCanBeUsed(){

        ArrayList<Voucher> vouchers = new ArrayList<>();

        int coffeesOrdered = 0;

        for (OrderProduct o: this.productsSelected) {
            if (o.getTag_number() == 2 ){
                coffeesOrdered = o.getQuantity();
            }
        }

        for (Voucher v: this.vouchers) {
            if (v.getType().equals(GlobalVariables.voucherCoffee) && coffeesOrdered > 0){
                vouchers.add(v);
                coffeesOrdered -= 1;
            }else if (v.getType().equals(GlobalVariables.voucherDiscount)){
                vouchers.add(v);
            }
        }

        return vouchers;
    }

    public void getVouchers(){

        SharedPreferences settings = getSharedPreferences("user_info", MODE_PRIVATE);
        String userId = settings.getString("userId", "not found");

        GetVoucher getVoucher = new GetVoucher("/voucher/"+userId);
        Thread thr = new Thread(getVoucher);
        thr.start();
        try {
            thr.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (getVoucher.responseCode == 200){
            String response = getVoucher.response;
            System.out.println("VOUCHERS: "+response);
            createVoucherObject(response);
        }else {

        }

    }

    public void createVoucherObject(String response){

        try {
            JSONObject vouchers = new JSONObject(response);
            JSONArray json = vouchers.getJSONArray("voucher");

            for (int i = 0; i< json.length(); i++){
                String id = (String) json.getJSONObject(i).get("_id");
                String type = (String) json.getJSONObject(i).get("type");

                Voucher v = new Voucher(id, type);

                this.vouchers.add(v);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Order makeOrder(){

        ArrayList<Voucher> selectedVouchers = this.adapterVouchers.getSelectedVouchers();

        Double price = calculatePrice(selectedVouchers);

        Order o = new Order(this.userId, this.productsSelected, selectedVouchers, price);

        return o;
    }

    public Double calculatePrice(ArrayList<Voucher> selectedVouchers){

        Double price = this.price;

        for (Voucher v: selectedVouchers) {
            if(v.getType().equals(GlobalVariables.voucherCoffee)){
                price -= GlobalVariables.coffeePrice;
            }else if(v.getType().equals(GlobalVariables.voucherDiscount)){
                price -= price*GlobalVariables.discount;
            }
        }
        
        return price;
    }

    public void nfcCall(){
        Intent intent = new Intent(OrderConfirmActivity.this, NfcActivity.class);
        intent.putExtra("order", order);

        System.out.println("ORDER");
        System.out.println(order.getProducts());
        System.out.println(order.getUserId());
        System.out.println(order.getVouchers());
        System.out.println(order.getPrice());
        startActivity(intent);
    }
}
