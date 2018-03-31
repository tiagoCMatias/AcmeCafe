package com.example.tiagomatias.acme_client;

import android.net.http.HttpResponseCache;

import com.example.tiagomatias.acme_client.Models.Order;
import com.example.tiagomatias.acme_client.Models.OrderProduct;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
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

    public void run()
    {
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
            jOrderType.addProperty("user:",  order.getUserId() );
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
            url = new URL("http://192.168.1.132:3000/order/new" );
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

    public static HttpResponse makeRequest(String path, Map params) throws Exception
    {
        //instantiates httpclient to make request
        DefaultHttpClient httpclient = new DefaultHttpClient();

        //url with the post data
        HttpPost httpost = new HttpPost(path);

        //convert parameters into JSON object
        JSONObject holder = getJsonObjectFromMap(params);

        //passes the results to a string builder/entity
        StringEntity se = new StringEntity(holder.toString());

        //sets the post request as the resulting string
        httpost.setEntity(se);
        //sets a request header so the page receving the request
        //will know what to do with it
        httpost.setHeader("Accept", "application/json");
        httpost.setHeader("Content-type", "application/json");

        //Handles what is returned from the page
        //HttpResponseCache responseHandler = new HttpResponseCache();

        return httpclient.execute(httpost);
    }


    private static JSONObject getJsonObjectFromMap(Map params) throws JSONException {

        //all the passed parameters from the post request
        //iterator used to loop through all the parameters
        //passed in the post request
        Iterator iter = params.entrySet().iterator();

        //Stores JSON
        JSONObject holder = new JSONObject();

        //using the earlier example your first entry would get email
        //and the inner while would get the value which would be 'foo@bar.com'
        //{ fan: { email : 'foo@bar.com' } }

        //While there is another entry
        while (iter.hasNext())
        {
            //gets an entry in the params
            Map.Entry pairs = (Map.Entry)iter.next();

            //creates a key for Map
            String key = (String)pairs.getKey();

            //Create a new map
            Map m = (Map)pairs.getValue();

            //object for storing Json
            JSONObject data = new JSONObject();

            //gets the value
            Iterator iter2 = m.entrySet().iterator();
            while (iter2.hasNext())
            {
                Map.Entry pairs2 = (Map.Entry)iter2.next();
                data.put((String)pairs2.getKey(), (String)pairs2.getValue());
            }

            //puts email and 'foo@bar.com'  together in map
            holder.put(key, data);
        }
        return holder;
    }
}


