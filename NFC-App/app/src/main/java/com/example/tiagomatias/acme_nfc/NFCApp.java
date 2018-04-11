package com.example.tiagomatias.acme_nfc;

import android.app.Application;

import com.example.tiagomatias.acme_nfc.Models.Order;
import com.example.tiagomatias.acme_nfc.Models.Product;

import java.util.ArrayList;

/**
 * Created by andre on 05-04-2018.
 */

public class NFCApp extends Application {
    public Order request;
    public ArrayList<Product> products = new ArrayList<>();

}