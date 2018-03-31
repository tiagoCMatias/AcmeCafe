package com.example.tiagomatias.acme_client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tiagomatias.acme_client.Adapters.ConfirmOrderProductsListAdapter;
import com.example.tiagomatias.acme_client.Adapters.ConfirmOrderVouchersListAdapter;
import com.example.tiagomatias.acme_client.Models.Order;
import com.example.tiagomatias.acme_client.Models.OrderProduct;
import com.example.tiagomatias.acme_client.Models.Voucher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        //showVouchers();

        Button confirm = findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                order = makeOrder();
/*
                System.out.print("Making Order\n");
                order.getProducts();
                System.out.println("\nUserId: " + order.getUserId());
                System.out.println("\nVouchers: " + order.getVouchers());
                System.out.println("\nPrice: " + order.getPrice());*/
                //nfcCall();
                //sendOrder(order);

                SendOrder sendOrder = new SendOrder(order);
                Thread thr = new Thread(sendOrder);
                thr.start();
                try {
                    thr.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void sendOrder(Order order)
    {
        Map<String, String> data = new HashMap<>();

        JSONObject jOrderType = new JSONObject();
        try
        {
            JSONArray jProductData = new JSONArray ();
            for (OrderProduct product : order.products)  {
                JSONObject obj = new JSONObject();
                //System.out.print("\nProduct_Id: "+ product.getId());
                obj.put("product_id", product.id.toString());
                //System.out.print("\nProduct_Name: "+ product.getName());
                obj.put("product_name", product.productName.toString());
                //System.out.print("\nProduct_tag: "+ product.getTag_number());
                obj.put("product_tag", product.tag_number);
                //System.out.print("\nProduct_Qt: "+ product.getQuantity());
                obj.put("product_qt", product.quantity);
                jProductData.put( obj.toString() );
            }
            jOrderType.put("products", jProductData.toString());


        }
        catch (Exception e) {
            System.out.println(e.toString());
        }

        URL url;
        HttpURLConnection urlConnection = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String json = gson.toJson(jOrderType.toString());
        System.out.println("Printing json");
        System.out.println(json);

        try {
            url = new URL("http://172.30.25.111:3000/order/new" );
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setUseCaches (false);

            DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
            outputStream.writeBytes(json);
            outputStream.flush();
            outputStream.close();

            // get response
            int responseCode = urlConnection.getResponseCode();
            System.out.println("ADDINg");
            if(responseCode == 201) {
                System.out.println("Code: " + responseCode);
            }
            else {
                System.out.println("Code: " + responseCode);
            }
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        finally {
            if(urlConnection != null)
                urlConnection.disconnect();
        }
    }

    public void showProducts(ArrayList<OrderProduct> productsList){
        final ListView products = findViewById(R.id.order_products);

        adapterProducts =  new ConfirmOrderProductsListAdapter(this,
                R.layout.confirm_order_products_item, productsList);

        products.setAdapter(adapterProducts);
    }

    public void showVouchers(){
        System.out.println("Trying to get Vouchers");
        if(getVouchers()) {

            final ListView vouchers = findViewById(R.id.order_vouchers);

            adapterVouchers = new ConfirmOrderVouchersListAdapter(this,
                    R.layout.confirm_order_vouchers_item, this.vouchers);

            vouchers.setAdapter(adapterVouchers);
        }
    }

    public boolean getVouchers(){

        SharedPreferences settings = getSharedPreferences("user_info", MODE_PRIVATE);
        String userId = settings.getString("userId", "not found");

        GetVoucher getVoucher = new GetVoucher("/vouchers/"+userId);
        Thread thr = new Thread(getVoucher);
        thr.start();
        try {
            thr.join();
            String response = getVoucher.response;
            System.out.println("VOUCHERS: "+response);
            if(response != null)
            createVoucherObject(response);
            return true;
        } catch (InterruptedException e) {
            System.out.println("Failed to get Vouchers");
            e.printStackTrace();
            return false;
        }

    }

    public void createVoucherObject(String response){

        try {
            JSONObject vouchers = new JSONObject(response);
            JSONArray json = vouchers.getJSONArray("voucher");
            for (int i = 0; i< json.length(); i++){
                //Integer id = (Integer) json.getJSONObject(i).get("_id");
                String type = (String) json.getJSONObject(i).get("_id");

                Voucher v = new Voucher(type);

                this.vouchers.add(v);
            }
        } catch (JSONException e) {
            System.out.println("Failed to Create Vouchers");
            e.printStackTrace();
        }
    }

    public Order makeOrder(){

        Order o = new Order(this.userId, this.productsSelected, this.vouchers, this.price);

        return o;
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


