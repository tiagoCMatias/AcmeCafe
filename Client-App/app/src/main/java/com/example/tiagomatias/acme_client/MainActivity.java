package com.example.tiagomatias.acme_client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class MainActivity extends AppCompatActivity {

    KeyStore keyStore;
    String alias = "keys", username, userId;
    PrivateKey privateKey;
    byte[] encryptedBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            verifyRegistration();
        } catch (CertificateException e) {

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


    }


    public void verifyRegistration() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, UnrecoverableEntryException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException {


        SharedPreferences settings = getSharedPreferences("user_info", MODE_PRIVATE);

        if (!settings.contains("username")) {

            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        } else{
            logIn();
        }
    }

    public void logIn() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, UnrecoverableEntryException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, BadPaddingException {

        SharedPreferences settings = getSharedPreferences("user_info", MODE_PRIVATE);
        username = settings.getString("username", "USERNAME NOT FOUND");
        userId = settings.getString("userId", "ID NOT FOUND");

        VerifyUser verify = new VerifyUser("/user/"+ userId);
        Thread thr = new Thread(verify);
        thr.start();

        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
        PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey();
        privateKey = privateKeyEntry.getPrivateKey();
        BigInteger mod, exp;

        mod = ((RSAPublicKey)publicKey).getModulus();                       // get the value for the private key in a BigInteger (the modulus)
        exp = ((RSAPublicKey)publicKey).getPublicExponent();


        KeyFactory f = KeyFactory.getInstance("RSA");

        RSAPublicKeySpec spec = new RSAPublicKeySpec(mod, exp);

        try {
            publicKey = f.generatePublic(spec);
            byte[] data = publicKey.getEncoded();
            String base64encoded = new String(Base64.encode(data, Base64.DEFAULT));

            System.out.println("INCOMING");
            System.out.println(base64encoded);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        try {
            thr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        String message = "123456789qweqweqwe";
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);
        try {
            signature.update(message.getBytes());
            byte[] signatureValue = signature.sign();


            signature.initVerify(publicKey);
            signature.update(message.getBytes());
            boolean ok = signature.verify(signatureValue);

            System.out.println("VERIFY: " + ok);
        } catch (SignatureException e) {
            e.printStackTrace();
        }





        if (verify.responseCode == 200){
            System.out.println("Existo");
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        }else {
            System.out.println("EU Não");
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        }


//        getKeys();
//        encrypt();

    }

    public void encrypt() throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IOException, CertificateException, KeyStoreException, UnrecoverableEntryException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
        privateKey = privateKeyEntry.getPrivateKey();

        Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        inCipher.init(Cipher.ENCRYPT_MODE, privateKey);
        encryptedBytes = inCipher.doFinal(username.getBytes());

        System.out.println("PRIVATE: " + privateKey.toString());
        System.out.println("USERNAME: " + username);
        System.out.println("ENCRYPTT: " + encryptedBytes.toString());
    }

    public void getKeys() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, UnrecoverableEntryException {

        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);


        // Retrieve the keys
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
        privateKey = privateKeyEntry.getPrivateKey();

        System.out.println("private LI key = " + privateKey.toString());
    }
}
