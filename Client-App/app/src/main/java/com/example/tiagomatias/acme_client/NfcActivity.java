package com.example.tiagomatias.acme_client;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.tiagomatias.acme_client.Models.Order;
import com.example.tiagomatias.acme_client.Models.OrderProduct;
import com.example.tiagomatias.acme_client.Models.Voucher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;

public class NfcActivity extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback {

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

        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(getApplicationContext(), "NFC is not available on this device.", Toast.LENGTH_LONG).show();
            finish();
        }

        byte[] payload = new byte[0];
        try {
            payload = makeBytesArray(order);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("PAYLOAD: " + payload.length);
        NdefMessage msg = new NdefMessage(new NdefRecord[] { createMimeRecord("application/com.example.tiagomatias.acme_client.order", payload) });
        System.out.println(msg.getRecords()[0].getPayload().length);

        if (mNfcAdapter != null) {
            mNfcAdapter.setNdefPushMessage(msg, this);
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
        
    }

    public byte[] makeBytesArray(Order order) throws UnsupportedEncodingException {

        // UUID tem 16 bytes
        //INT tem 4bytes
        // assinatura tem 48 bytes
        // usar 1 byte para o numero de produtos

        byte[] userIdByte = order.getUserId().getBytes("ISO-8859-1");
        System.out.println("USERID: " + order.getUserId());
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
            bb.put(v.getId().getBytes("ISO-8859-1"));
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
            System.out.println(byteArrayToHex(assinatura));
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
        System.out.println("SIZE ID:" + Arrays.toString(userIdByte));
        System.out.println("SIZE P BYte:" + numberOfProductsByte);
        System.out.println("SIZE V Byte:" + numberOfVouchersByte);

        System.out.println("SIZE BB: " + bb.array().length);
        System.out.println("BB STRING:");

        for (byte b: bb.array()) {
            System.out.println(b);
        }

        byte[] message = bb.array();

        return message;

    }

    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("ISO-8859-1"));
        return new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Message sent.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    String byteArrayToHex(byte[] ba) {
        StringBuilder sb = new StringBuilder(ba.length * 2);
        for(byte b: ba)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
