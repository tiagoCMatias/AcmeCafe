package com.example.tiagomatias.acme_client;


import com.example.tiagomatias.acme_client.Models.Order;
import com.example.tiagomatias.acme_client.Models.OrderProduct;
import com.example.tiagomatias.acme_client.Models.Voucher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class SendOrder implements Runnable {

    Order order;

    //ASSINATURA
    PrivateKey privateKey;
    Signature sg;
    byte[] assinatura;


    public SendOrder(Order order){
        this.order = order;
    }

    @Override
    public void run()
    {

        makeBytesArray(this.order);

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

            JsonArray jVoucherData = new JsonArray ();
            for (Voucher voucher : order.vouchers)  {
                System.out.println("IDV:" + voucher.getId());
                JsonObject obj = new JsonObject();
                //System.out.print("\nProduct_Id: "+ product.getId());
                obj.addProperty("_id", String.valueOf(voucher.getId()));
                //System.out.print("\nProduct_Name: "+ product.getName());
                obj.addProperty("type", voucher.getType());
                jVoucherData.add(obj);
            }
            jOrderType.addProperty("user",  order.getUserId() );
            jOrderType.addProperty("price",  order.getPrice() );



            //ASSINATURA
            jOrderType.addProperty("assinatura", byteArrayToHex(assinatura));


            jOrderType.add("products",  jProductData );
            jOrderType.add("vouchers",  jVoucherData );

            System.out.println(jOrderType);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }

        URL url;
        HttpURLConnection urlConnection = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = null;

        json = gson.toJson(jOrderType);
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+jOrderType.toString());


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

        System.out.println(response.toString());

        return response.toString();
    }


    // ASSINATURA (PARA APAGAR)

    public void makeBytesArray(Order order){

        // UUID tem 16 bytes
        //INT tem 4bytes
        // assinatura tem 48 bytes
        // usar 1 byte para o numero de produtos

        byte[] userIdByte = order.getUserId().getBytes();
        byte sizeOfUserIdByte = (byte) userIdByte.length;

        int numberOfProducts = order.getProducts().size();
        byte numberOfProductsByte = (byte) numberOfProducts;
        int numberOfVouchers = order.getVouchers().size();
        byte numberOfVouchersByte = (byte) numberOfVouchers;



        //ESTRUTURA: tamanho do ID (1 byte), ID, tamanho dos produtos(1 byte), produtos(tamanho * 25 pq é 1 byte para a quantidade e 24byte para o nome), tamanho dos vouchers(1 byte), vouchers (tamanho * 24)
        Integer sizeOfByteBuffer = 1 + sizeOfUserIdByte + 1 + numberOfProducts * 2 + 1 + numberOfVouchers * 12 + 64;
        int sizeUsed = 0;

        ByteBuffer bb = ByteBuffer.allocate(sizeOfByteBuffer);

        bb.put(sizeOfUserIdByte);
        sizeUsed += 1;
        bb.put(userIdByte);
        sizeUsed += sizeOfUserIdByte;
        bb.put((byte) (numberOfProductsByte * 2));
        sizeUsed += 1;
        //FOREACH products
        for (OrderProduct p: order.getProducts()) {
            bb.put((byte) p.getQuantity());
            bb.put((byte) p.getTag_number());
            sizeUsed+= 2;
        }

        bb.put((byte) (numberOfVouchersByte));
        sizeUsed += 1;
        //FOREACH vouchers
        for (Voucher v: order.getVouchers()) {
            bb.put(v.getId().getBytes());
            sizeUsed+= 12;
        }

        //Assinatura
        try {
            KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
            keystore.load(null);

            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keystore.getEntry("keys", null);
            privateKey = privateKeyEntry.getPrivateKey();

            sg = Signature.getInstance("SHA1WithRSA");                    // for signing with the stated algorithm (Signing and verifying object)
            sg.initSign(privateKey);                                             // supply the private key
            sg.update(userIdByte);                                             // define the data to sign
            assinatura = sg.sign();
            sizeUsed+= assinatura.length;
            System.out.println("Sign SiZE: " + assinatura.length);
            System.out.println("USED: " + sizeUsed);
            System.out.println(bb.array().length);
            System.out.println("ASS: " + assinatura.toString());
            //bb.put(assinatura);//BufferOverflowException

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


        System.out.println("ID BYTE :" + userIdByte);
        System.out.println("SIZE ID:" + sizeOfUserIdByte);
        System.out.println("SIZE P BYte:" + numberOfProductsByte);
        System.out.println("SIZE V Byte:" + numberOfVouchersByte);

        System.out.println("SIZE BB: " + bb.array().length);
        System.out.println("BB STRING:");

        for (byte b: bb.array()) {
            System.out.println(b);
        }

    }

    String byteArrayToHex(byte[] ba) {
        StringBuilder sb = new StringBuilder(ba.length * 2);
        for(byte b: ba)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}



