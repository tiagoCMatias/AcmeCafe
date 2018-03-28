package com.example.tiagomatias.acme_client;

import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.tiagomatias.acme_client.Models.Order;
import com.example.tiagomatias.acme_client.Models.OrderProduct;
import com.example.tiagomatias.acme_client.Models.Voucher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class NfcActivity extends AppCompatActivity {

    PrivateKey privateKey;
    Signature sg;
    byte[] assinatura;
    Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        Bundle extras = getIntent().getExtras();
        order = (Order) extras.getSerializable("order");


        checkForNfc();
        makeBytesArray( order);


    }

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
        Integer sizeOfByteBuffer = 1 + sizeOfUserIdByte + 1 + numberOfProducts * 2 + 1 + numberOfVouchers * 25 + 64;
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

        bb.put((byte) (numberOfVouchersByte * 24));
        sizeUsed += 1;
        //FOREACH vouchers
        for (Voucher v: order.getVouchers()) {
            bb.putInt(v.getId());
            sizeUsed+= 25;
        }

        //Assinatura
        try {
            KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
            keystore.load(null);

            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keystore.getEntry("keys", null);
            privateKey = privateKeyEntry.getPrivateKey();

            sg = Signature.getInstance("SHA1WithRSA");                    // for signing with the stated algorithm (Signing and verifying object)
            sg.initSign(privateKey);                                             // supply the private key
            sg.update(bb.array());                                             // define the data to sign
            assinatura = sg.sign();
            sizeUsed+= assinatura.length;
            System.out.println("Sign SiZE: " + assinatura.length);
            System.out.println("USED: " + sizeUsed);
            System.out.println(bb.array().length);
            System.out.println(assinatura);
            bb.put(assinatura);//BufferOverflowException

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



    public void checkForNfc(){
        // Check for available NFC Adapter
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(getApplicationContext(), "NFC is not available on this device.", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
