package com.example.tiagomatias.acme_nfc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.tiagomatias.acme_nfc.Models.Product;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NFCMain extends AppCompatActivity {
    NFCApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (NFCApp)getApplication();
        setContentView(R.layout.activity_nfc_main);
        GetProducts productsRun = new GetProducts("/product/all");
        Thread productsThr = new Thread(productsRun);
        productsThr.start();

        try {
            productsThr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String response = productsRun.response;
        createProductObject(response);
    }

    private class GetProducts implements Runnable {
        String address;
        String response;

        GetProducts(String addr) {
            address = addr;
        }

        @Override
        public void run() {
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(GlobalVariables.url + address);

                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setUseCaches(false);

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == 201) {
                    InputStream response = urlConnection.getInputStream();
                    this.response = readStream(response);
                } else {
                    System.out.println("Code: " + responseCode);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
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
                e.printStackTrace();
            }
            finally {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return response.toString();
        }
    }

    /**
     * Created by Henrique.
     */
    public void createProductObject(String response){

        try {
            JSONArray json = new JSONArray(response);
            for (int i = 0; i< json.length(); i++){
                String id = (String) json.getJSONObject(i).get("_id");
                String name = (String) json.getJSONObject(i).get("name");
                Double price = (Double) json.getJSONObject(i).get("price");
                Integer tag_number = (Integer)json.getJSONObject(i).get("tag_number");

                Product p = new Product(id, name, price, tag_number);

                app.products.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
