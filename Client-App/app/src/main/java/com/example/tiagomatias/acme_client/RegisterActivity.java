package com.example.tiagomatias.acme_client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.tiagomatias.acme_client.Models.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Savepoint;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

public class RegisterActivity extends AppCompatActivity {

    KeyPairGenerator kpg;
    KeyPair keyPair;
    KeyStore keyStore;
    PublicKey publicKey;
    PrivateKey privateKey;
    byte[] encryptedBytes,decryptedBytes;
    Cipher eCipher,dCipher;
    String encrypted,decrypted, username, nif, userId;
    String alias= "keys";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText name = findViewById(R.id.name);
        final EditText number = findViewById(R.id.nif);

        Button clickButton = (Button) findViewById(R.id.confirm);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                addUser(name, number);
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                startActivity(intent);

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void addUser(EditText et_name, EditText et_nif) {
        nif = et_nif.getText().toString();
        username = et_name.getText().toString();

        byte[] rsa = new byte[0];
        try {
            keyStore();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void keyStore() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException, CertificateException, KeyStoreException, UnrecoverableEntryException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {

        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);


        Calendar notBefore = Calendar.getInstance();
        Calendar notAfter = Calendar.getInstance();

        KeyPairGeneratorSpec spec =
                new KeyPairGeneratorSpec.Builder(this)
                        .setKeySize(512)
                        .setAlias(alias)
                        .setSubject(new X500Principal("CN=" + alias))
                        .setSerialNumber(BigInteger.valueOf(1337))
                        .setStartDate(notBefore.getTime())
                        .setEndDate(notAfter.getTime())
                        .build();

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        generator.initialize(spec);

        keyPair = generator.generateKeyPair();


        saveUserToDB();

    }


    public void saveUserToDB() throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnrecoverableEntryException, IllegalBlockSizeException, BadPaddingException, KeyStoreException, CertificateException {

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
        publicKey = privateKeyEntry.getCertificate().getPublicKey();

        String PubKeyString = byteArrayToHex(((RSAPublicKey)publicKey).getModulus().toByteArray());


        AddUser adduser = new AddUser("/user/new", username, nif, PubKeyString);
        Thread thr = new Thread(adduser);
        thr.start();

        try {
            thr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (adduser.responseCode == 201){

            String response = adduser.response;

            saveUserToSharedPreferences(response);
        }

    }

    public void saveUserToSharedPreferences(String response){

        //Get the UserId from the response
        try {
            JSONObject json = new JSONObject(response);
            JSONObject user = (JSONObject) json.get("user");
            String userId = (String) user.get("_id");
            this.userId = userId;
            System.out.println("UID; "+ userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // Writing username to SharedPreferences
        SharedPreferences settings = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", username);
        editor.putString("nif", nif);
        editor.putString("userId", userId);
        editor.commit();
    }

  /*  public void encrypt() throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IOException, CertificateException, KeyStoreException, UnrecoverableEntryException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
        privateKey = privateKeyEntry.getPrivateKey();

        Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        inCipher.init(Cipher.ENCRYPT_MODE, privateKey);
        encryptedBytes = inCipher.doFinal(username.getBytes());
        System.out.println("PK: " + privateKey.toString());
        System.out.println("ENCRYPTED: "+ encryptedBytes.toString());
    }

    public void decrypt() throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
        publicKey = privateKeyEntry.getCertificate().getPublicKey();

        Cipher outCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        outCipher.init(Cipher.DECRYPT_MODE, publicKey);
        decryptedBytes = outCipher.doFinal(encryptedBytes);

        System.out.println("PUBK: " + publicKey.toString());
        System.out.println("D-CR: " + decryptedBytes.toString());
    }*/


    String byteArrayToHex(byte[] ba) {
        StringBuilder sb = new StringBuilder(ba.length * 2);
        for(byte b: ba)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

}
