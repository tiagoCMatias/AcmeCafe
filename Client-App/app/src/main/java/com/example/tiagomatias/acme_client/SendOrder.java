package com.example.tiagomatias.acme_client;


import com.example.tiagomatias.acme_client.Models.Order;
import com.example.tiagomatias.acme_client.Models.OrderProduct;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SendOrder implements Runnable {

    Order order;

    public SendOrder(Order order){
        this.order = order;
    }

    @Override
    public void run()
    {
        System.out.println("RUNNABLLEMELMELMe");
        Map<String, String> data = new HashMap<>();
        JsonObject jOrderType = new JsonObject();
        try
        {
            JsonArray jProductData = new JsonArray ();
            for (OrderProduct product : order.products)  {
                JsonObject obj = new JsonObject();
                //System.out.print("\nProduct_Id: "+ product.getId());
                obj.addProperty("_id", product.id);
                //System.out.print("\nProduct_Name: "+ product.getName());
                obj.addProperty("name", product.productName);
                //System.out.print("\nProduct_tag: "+ product.getTag_number());
                obj.addProperty("tag_number", String.valueOf(product.tag_number));
                //System.out.print("\nProduct_Qt: "+ product.getQuantity());
                obj.addProperty("qt", String.valueOf(product.quantity));
                jProductData.add( obj );
            }
            jOrderType.addProperty("user",  order.getUserId() );
            jOrderType.addProperty("price",  order.getPrice() );
            jOrderType.add("products",  jProductData );

        }
        catch (Exception e) {
            System.out.println(e.toString());
        }

        URL url;
        HttpURLConnection urlConnection = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = null;

        json = gson.toJson(jOrderType);
        System.out.println(json);


        try {
            url = new URL(GlobalVariables.url + "/order/new" );
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
                String response = readStream(urlConnection.getInputStream());
                System.out.println(response);
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
    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));

            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        catch (IOException e) {
            return e.getMessage();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    return e.getMessage();
                }
            }
        }

        return response.toString();
    }
}



