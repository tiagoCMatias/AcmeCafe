package com.example.tiagomatias.acme_nfc;

import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.example.tiagomatias.acme_nfc.Models.OrderProduct;
import com.example.tiagomatias.acme_nfc.Models.Product;
import com.example.tiagomatias.acme_nfc.Models.Voucher;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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
        app = (NFCApp)getApplication();
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
        int err = this.processOrder(msg.getRecords()[0].getPayload());
    }

    private int processOrder(byte[] order) throws IOException {
        int currPosition = 0;
        int sizeUserID = order[currPosition];
        currPosition++;

        ArrayList<Byte> userID = new ArrayList<>();
        while (currPosition <= sizeUserID) {
            userID.add(order[currPosition]);
            currPosition++;
        }

        int numProducts = order[currPosition];
        currPosition++;
        ArrayList<OrderProduct> products = new ArrayList<>();
        double price = 0;
        while (currPosition <= numProducts*2) {
            for (Product product : app.products) {
                if (order[currPosition + 1] == product.getTag_number()) {
                    products.add(new OrderProduct(product.getId(), product.getName(), product.getPrice(), product.getTag_number()));
                    products.get(products.size() - 1).setQuantity((int) order[currPosition]);
                    price = price + product.getPrice()*order[currPosition];
                    break;
                }
            }
            currPosition = currPosition+2;
        }

        int numVouchers = order[currPosition];
        currPosition++;
        ArrayList<Byte[]> voucherList = new ArrayList<>();
        ArrayList<Voucher> vouchers = new ArrayList<>();
        while (currPosition < numVouchers*12) {
            Byte[] voucher = new Byte[12];
            System.arraycopy(order, currPosition, voucher,0, 12);
            voucherList.add(voucher);
            currPosition = currPosition+12;
        }

        URL url;
        HttpURLConnection urlConnection;

        for (Byte[] voucher : voucherList) {
            url = new URL(GlobalVariables.url + "/voucher/" + voucher.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setUseCaches(false);

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 201) {
                String response = readStream(urlConnection.getInputStream());
                try {
                    JSONArray json = new JSONArray(response);
                    for (int i = 0; i< json.length(); i++){
                        String type = (String) json.getJSONObject(i).get("type");

                        Voucher v = new Voucher(voucher[1].toString(), type);

                        vouchers.add(v);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Code: " + responseCode);
            }
        }

        byte[] sig = new byte[order.length - currPosition];
        System.arraycopy(order, currPosition, sig, 0, sig.length);


        int orderID = 00000;
        presentOrder(orderID, products, vouchers, price);
        return 0;
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

    private void presentOrder(int orderID, ArrayList<OrderProduct> products, ArrayList<Voucher> vouchers, double price) {
        // set order number
        TextView err = findViewById(R.id.orderNumber);
        err.setText(orderID);

        // set order products
        LinearLayout productList = findViewById(R.id.productsList);
        for (OrderProduct product : products) {
            productList.addView(new TextView(this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            TextView productView = (TextView)productList.getChildAt(productList.getChildCount() - 1);
            productView.setTextColor(Color.parseColor("#000000"));
            productView.setText(product.getName());
        }

        // set order vouchers
        LinearLayout voucherList = findViewById(R.id.vouchersList);
        for (Voucher voucher : vouchers) {
            voucherList.addView(new TextView(this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            TextView voucherView = (TextView)voucherList.getChildAt(voucherList.getChildCount() - 1);
            voucherView.setTextColor(Color.parseColor("#000000"));
            voucherView.setText(voucher.getType());
        }

        // set order price
        err = findViewById(R.id.price);
        err.setText(Double.toString(price));
    }

}
