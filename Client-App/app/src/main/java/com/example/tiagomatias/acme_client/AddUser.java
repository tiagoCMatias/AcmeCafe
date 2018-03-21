package com.example.tiagomatias.acme_client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;

/**
 * Created by Henrique on 14/03/2018.
 */

public class AddUser implements Runnable {
    String encrypted_name;
    String nif;
    String public_key;
    String address;
    String response;
    int responseCode;


    AddUser(String baseAddress, String encrypted_name, String nif, String pk) {
        address = baseAddress;
        this.encrypted_name = encrypted_name;
        this.nif = nif;
        public_key = pk;
    }

    @Override
    public void run() {
        URL url;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL("http://10.0.2.2:3000" + address);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setUseCaches (false);

            DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
            String payload = "{\"username\" : \""+ encrypted_name + "\", \"nif\" : "+ nif +", \"public_key\" : \""+public_key +"\"}";
            System.out.println("payload: " + payload);
            outputStream.writeBytes(payload);
            outputStream.flush();
            outputStream.close();

            // get response
            int responseCode = urlConnection.getResponseCode();
            if(responseCode == 201) {
                this.responseCode = responseCode;
                String response = readStream(urlConnection.getInputStream());
                this.response = response;
            }
            else {
                this.responseCode = responseCode;
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
