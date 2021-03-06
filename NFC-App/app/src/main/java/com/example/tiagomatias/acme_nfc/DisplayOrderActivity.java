package com.example.tiagomatias.acme_nfc;

import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.example.tiagomatias.acme_nfc.Models.OrderProduct;
import com.example.tiagomatias.acme_nfc.Models.Product;
import com.example.tiagomatias.acme_nfc.Models.Voucher;

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
import java.util.ArrayList;
import java.util.Arrays;

/*
 * Mensagem de order:
 * user_size: 1byte + id_user + nr_items: 1byte + item: 2bytes*nr + nr_vouchers: 1byte + voucher: 12byte*nr + assinatura
 */

public class DisplayOrderActivity extends AppCompatActivity {
    NFCApp app;
    ArrayList<String> order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (NFCApp) getApplication();
        setContentView(R.layout.activity_display_order);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            try {
                processIntent(getIntent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    void processIntent(Intent intent) throws IOException {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        byte[] order = msg.getRecords()[0].getPayload();
        Thread pOrder = new Thread(new ProcessOrder(order));
        pOrder.start();
    }

    private void presentOrder(int orderID, ArrayList<OrderProduct> products, ArrayList<Voucher> vouchers, double price) {
        // set order number
        TextView err = findViewById(R.id.orderNumber);
        err.setText(orderID);

        // set order products
        LinearLayout productList = findViewById(R.id.productsList);
        for (OrderProduct product : products) {
            productList.addView(new TextView(this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            TextView productView = (TextView) productList.getChildAt(productList.getChildCount() - 1);
            productView.setTextColor(Color.parseColor("#000000"));
            productView.setText(product.getName());
        }

        // set order vouchers
        LinearLayout voucherList = findViewById(R.id.vouchersList);
        for (Voucher voucher : vouchers) {
            voucherList.addView(new TextView(this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            TextView voucherView = (TextView) voucherList.getChildAt(voucherList.getChildCount() - 1);
            voucherView.setTextColor(Color.parseColor("#000000"));
            voucherView.setText(voucher.getType());
        }

        // set order price
        err = findViewById(R.id.price);
        err.setText(price+"€");
    }

    private class ProcessOrder implements Runnable {
        byte[] order;
        String response;

        ProcessOrder(byte[] order) {
            this.order = order;
        }

        @Override
        public void run() {

            System.out.println("PAYLOAD: " + Arrays.toString(order));

            int currPosition = 0;
            int sizeUserID = order[currPosition];
            currPosition++;

            ArrayList<Byte> userID = new ArrayList<>();
            while (currPosition <= sizeUserID) {
                userID.add(order[currPosition]);
                currPosition++;
            }

            System.out.println("userID:" + userID);

            int numProducts = order[currPosition] + currPosition;
            currPosition++;
            ArrayList<OrderProduct> products = new ArrayList<>();
            double price = 0;
            while (currPosition <= numProducts) {
                for (Product product : app.products) {
                    if (order[currPosition + 1] == product.getTag_number()) {
                        products.add(new OrderProduct(product.getId(), product.getName(), product.getPrice(), product.getTag_number()));
                        products.get(products.size() - 1).setQuantity((int) order[currPosition]);
                        price = price + product.getPrice() * order[currPosition];

                        System.out.println(products.get(products.size()-1).getName());
                        break;
                    }
                }
                currPosition = currPosition + 2;
            }

            int numVouchers = order[currPosition] * 12 + currPosition;
            currPosition++;
            ArrayList<byte[]> voucherList = new ArrayList<>();
            ArrayList<Voucher> vouchers = new ArrayList<>();
            while (currPosition < numVouchers) {
                byte[] voucher = new byte[12];
                System.arraycopy(order, currPosition, voucher, 0, 12);
                voucherList.add(voucher);
                currPosition = currPosition + 1;
            }

            URL url;
            HttpURLConnection urlConnection;

            for (byte[] voucher : voucherList) {
                try {
                    url = new URL(GlobalVariables.url + "/voucher/" + new String(voucher, "ISO-8859-1"));


                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setUseCaches(false);

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == 201) {
                        String response = readStream(urlConnection.getInputStream());
                        try {
                            JSONArray json = new JSONArray(response);
                            String type = (String) json.getJSONObject(0).get("type");

                            Voucher v = new Voucher(new String(voucher, "ISO-8859-1"), type);

                            vouchers.add(v);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("Code: " + responseCode);
                    }

                    urlConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            byte[] sig = new byte[order.length - currPosition];
            System.arraycopy(order, currPosition, sig, 0, sig.length);


            try {
                JSONObject jOrderType = new JSONObject();

                byte[] byteID = new byte[userID.size()];
                for (int pos = 0; pos < byteID.length; pos++) {
                    byteID[pos] = userID.get(pos);
                }

                String uID = new String(byteID, "ISO-8859-1");

                jOrderType.put("user",  uID);
                jOrderType.put("price",  price );

                JSONArray jsonProducts = new JSONArray ();
                System.out.println("NUM PRODUCTS "+products.size());
                for (OrderProduct product : products)  {
                    JSONObject obj = new JSONObject();
                    obj.put("_id", String.valueOf(product.getId()));
                    obj.put("name", String.valueOf(product.getName()));
                    obj.put("tag_number", String.valueOf(product.getTag_number()));
                    obj.put("qt", String.valueOf(product.quantity));
                    jsonProducts.put(obj);
                }

                JSONArray jsonVouchers = new JSONArray ();
                for (Voucher voucher : vouchers)  {
                    JSONObject obj = new JSONObject();
                    obj.put("_id", String.valueOf(voucher.getId()));
                    obj.put("type", voucher.getType());
                    jsonVouchers.put(obj);
                }

                jOrderType.put("assinatura", byteArrayToHex(sig));


                jOrderType.put("products",  jsonProducts );
                jOrderType.put("vouchers",  jsonVouchers );

                System.out.println(jOrderType.toString());


                url = new URL(GlobalVariables.url + "/order/new" );
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setUseCaches (false);

                DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
                outputStream.writeBytes(jOrderType.toString());
                outputStream.flush();
                outputStream.close();

                // get response
                int responseCode = urlConnection.getResponseCode();
                System.out.println("Code: " + responseCode);
                if(responseCode == 201) {
                    JSONArray json = new JSONArray(readStream(urlConnection.getInputStream()));
                    int orderID = Integer.parseInt((String)json.getJSONObject(0).get("_id"));
                    presentOrder(orderID, products, vouchers, price);
                }

            } catch(IOException | JSONException e) {

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
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return response.toString();
        }

        String byteArrayToHex(byte[] ba) {
            StringBuilder sb = new StringBuilder(ba.length * 2);
            for(byte b: ba)
                sb.append(String.format("%02x", b));
            return sb.toString();
        }
    }

}
