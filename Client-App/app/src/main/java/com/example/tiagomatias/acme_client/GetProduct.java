package com.example.tiagomatias.acme_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Henrique on 14/03/2018.
 */

public class GetProduct implements Runnable {

    String address = null;
    String response;

    GetProduct(String baseAddress) {
        address = baseAddress;
    }

    @Override
    public void run() {

        URL url;
        HttpURLConnection urlConnection = null;

        try {

            url = new URL(GlobalVariables.url + address);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoInput(true);

            urlConnection.setRequestProperty("Content-Type", "application/json");

            urlConnection.setUseCaches (false);

            int responseCode = urlConnection.getResponseCode();
            System.out.println("HEERE8");
            if(responseCode == 201) {
                String response = readStream(urlConnection.getInputStream());
                System.out.println(response);
                this.response = response;
            }
            else
                System.out.println("Code: " + responseCode);
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
