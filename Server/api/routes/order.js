const express = require('express');
const Promise = require('bluebird');
const mongoose = Promise.promisifyAll(require('mongoose'));
const router = express.Router();
const getPem = require('rsa-pem-from-mod-exp');

var crypto = require('crypto');
var fs = require('fs');
var ByteBuffer = require("bytebuffer");
var SIGNATURE_FORMAT = "base64"; // Accepted: hex, latin1, base64

const Order = require('../modules/order');
const User = require('../modules/user');
const Product = require('../modules/products');
const Voucher = require('../modules/voucher');


const Voucher_Coffe = "Free Coffee";
const Voucher_Discount = "Discount 5%";


router.get('/', (req, res, next) => {
    res.status(200).json({
        message: 'Order - GET'
    });
});

router.post('/teste', (req, res, next) => {

    const user = req.body.user;
    const price = req.body.price;

    const signature = req.body.assinatura;

    let verification = false;
    getUserData(user)
        .then(userData => {
            var public_key = userData.publicKey;

            var bb = ByteBuffer.fromHex(public_key);

            bb = bb.toBase64().toString();

            var exponent = "AQAB";

            var pem = getPem(bb, exponent);

            let message = user;

            var verify = crypto.createVerify("sha1WithRSAEncryption"); //sha1WithRSAEncryption
                
            verify.update(message);

            console.log('\n>>> Message:\n\n' + message);
            
            verification = verify.verify(pem, signature, 'hex');

            console.log('\n>>> Verify: ' + verification);

            res.status(200).json({
                message: 'Verify Signature',
                status: verification
            });
        })
        .catch(err=> { 
            console.log("err: " + err); 
            res.status(200).json({
                message: 'Verify Signature',
                status: verification
            });
        });
    
});

router.post('/new', (req, res, next) => {
    
    const user = req.body.user;
    const products = req.body.products;
    const vouchers = req.body.vouchers;
    const price = req.body.price;
    const signature = req.body.assinatura;

    let coffee_order = 0;

    let coffee_in_current_order = 0;
    let coffee_voucher_in_order = 0;
    let discount_voucher_in_order = 0;
    const coffee_id = "5ac399be111e652110ee1c51";

    let total_vouchers = 0;
    let total_money_spent = 0;

    console.log("User: " + user);

    if(!checkEncription(user, signature, user))
    {
        res.status(403).json({
            message: "Encryption Failed",
        });
    }

    products.forEach(element => {
        if(element._id == coffee_id)
        {
            coffee_in_current_order = Number(element.qt);
            console.log("coffee_in_current_order: " + element.qt);
        }
    });

    vouchers.forEach(element => {
        if(element.type == Voucher_Coffe)
        {
            coffee_voucher_in_order++;
            updateVoucherState(element._id).then(res => { /*console.log("updating voucher state");*/ });
        }
        if(element.type == Voucher_Discount)
        {
            discount_voucher_in_order++;
            updateVoucherState(element._id).then(res => { /*console.log("updating voucher state");*/ });
        }
    });

    
    getTotalMoneySpent(user)
        .then(discountDoc => { 
            total_money_spent = (discountDoc+price); 
            total_money_spent = (total_money_spent/100);
        });
    getUserData(user).then(doc => { total_vouchers = doc.coffe; }).catch();

    getTotalCoffesOrdered(user, coffee_id)
        .then(totalCoffees => { 
            total_cafe = totalCoffees;
        getUserVoucher(user)
            .then(userVouchers => { 
                console.log(userVouchers);
                let voucher_cafe_util = userVouchers.coffe; 
                let voucher_discount = userVouchers.money;
                let coffee_pagos = (total_cafe - voucher_cafe_util);

                coffee_pagos = coffee_pagos + (coffee_in_current_order - coffee_voucher_in_order);
                
                console.log("Vouchers gerados:" + total_vouchers);
                console.log("Total Cafes Pedidos:" + coffee_order);
                console.log("Vouchers Usados:" + voucher_cafe_util);
                console.log("Cafes Pagos:" + coffee_pagos);

                let gerar = Math.floor(coffee_pagos/3);

                if(total_vouchers < gerar){
                    let qt = gerar - total_vouchers;
                    for(var i = 0 ; i < qt; i++)
                    {
                        addUserNewCoffeVoucher(user, gerar).then(res => { /*console.log("adding voucher");*/ });
                    }  
                    console.log("Voucher Coffee Created: " + gerar);
                }

                if( voucher_discount < total_money_spent ){
                    const new_qt = Math.floor(total_money_spent) - voucher_discount;
                    
                    for(var i = 0 ; i < new_qt; i++)
                    {
                        addUserNewMoneyVoucher(user, new_qt).then(res => {  });
                    } 
                    console.log("Voucher Discount Created: " + new_qt);
                }

                const order = new Order({
                    products: products,
                    user_id:  user,
                    price: price,
                    voucher: vouchers
                });
            
                registerOrder(order).then(doc => {
                    res.status(201).json({
                        message: "New order added",
                        order: order
                    });
                });
            });
    });
});


function checkEncription(user, signature, message){
    return new Promise(function (resolve, reject) {
        resolve(checkPublicKeyEncription(user, signature, message));
    });
}

function checkPublicKeyEncription(user, signature, message){
    getUserData(user)
        .then(userData => {
            var public_key = userData.publicKey;

            var bb = ByteBuffer.fromHex(public_key);

            bb = bb.toBase64().toString();

            var exponent = "AQAB";

            var pem = getPem(bb, exponent);

            let message = user;

            var verify = crypto.createVerify("sha1WithRSAEncryption"); //sha1WithRSAEncryption
                
            verify.update(message);

            //console.log('\n>>> Message:\n\n' + message);
            
            let verification = verify.verify(pem, signature, 'hex');

            console.log('\n>>> Verify: ' + verification);

            if(verification)
                return true;
            else
                return false;

        })
        .catch(err=> { console.log("err: " + err); return false;});
}

function registerOrder(order){
    return new Promise(function (resolve, reject) {
        resolve(registerNewOrder(order));
    });
}


function registerNewOrder(order){
    order
    .save()
    .then(result => {
        return true;
    })
    .catch(error => {
        console.log(error);
        return false;
    });
}

function createNewVoucher(user, voucher_type){
    const voucher = new Voucher({
        user_id: user,
        type: voucher_type,
        state: false,
    });

    voucher
    .save()
    .then(result => {
        return true;
    })
    .catch(error => {
        console.log("Error: " + error);
    });
}

function addUserNewCoffeVoucher(user, voucher_qt){
    
    return new Promise(function (resolve, reject) {
        resolve(
            User.update({ _id: user}, { $set: { private_coffe_voucher_qt: voucher_qt }})
            .exec()
            .then(doc => {
                createNewVoucher(user, Voucher_Coffe)
            })
            .catch(err => {
                console.log("Error :" + err);
            })
        );
    });
}

function updateVoucherState(voucher_id){
    return new Promise(function (resolve, reject) {
        resolve(
            Voucher.update({ _id: voucher_id}, { $set: { state: true }})
            .exec()
            .then(doc => {
                return true;
            })
            .catch(err => {
                return false;
                console.log("Error :" + err);
            })
        );
    });
}


function addUserNewMoneyVoucher(user, voucher_qt){
    
    return new Promise(function (resolve, reject) {
        resolve(
            User.update({ _id: user}, { $set: { private_price_voucher_qt: voucher_qt }})
            .exec()
            .then(doc => {
                createNewVoucher(user, Voucher_Discount)
            })
            .catch(err => {
                return false;
                console.log("Error :" + err);
            })
        );
    });
}

function getUserVoucher(user){
    return new Promise(function (resolve, reject) {
        resolve(
            Voucher
            .find()
            .where('user_id').equals(user)
            .where('state').equals(true)
            .exec()
            .then(doc => {
                if(doc.length > 0)
                {
                    let coffe = 0;
                    let price = 0;
                    doc.forEach(element => {
                        if(element.type == Voucher_Coffe)
                            coffe = coffe +1 ;
                        if(element.type == Voucher_Discount)
                            price = price + 1;
                    });
                    
                    return {
                        coffe: coffe,
                        money: price,
                    };  
                }
                else
                {
                    return {
                        coffe: 0,
                        money: 0,
                    };
                }                
            })
            .catch(err => {
                console.log(err);
            })
        );
    });
}

function getUserData(user){
    return new Promise(function (resolve, reject) {
        resolve(
            User
            .findById(user)
            .exec()
            .then(doc => {
                if(doc)
                return {
                    coffe: doc.private_coffe_voucher_qt,
                    money: doc.private_price_voucher_qt,
                    publicKey: doc.public_key
                };                
            })
            .catch(err => {
                console.log(err);
            })
        );
    });
}

function getTotalCoffesOrdered(user, coffee_id){
    return new Promise(function (resolve, reject) {
        resolve(
            Order
            .find()
            .where('user_id').equals(user)
            .where('products._id').equals(coffee_id)
            .exec()
            .then(doc => {
                let max_coffe_qt = 0;
                doc.forEach(element => {
                    element.products.forEach(element => {
                        if(element._id == coffee_id)
                        {
                            //console.log("Confirmed ID - " + element.qt);
                            max_coffe_qt += element.qt;
                        }
                    });                    
                });
                return max_coffe_qt;
            })
            .catch(err => {
                console.log(err);
            })
        );
    });
}

function getTotalMoneySpent(user){
    return new Promise(function (resolve, reject) {
        resolve(
        Order
        .find()
        .where('user_id').equals(user)
        .select('price')
        .exec()
        .then(doc => {
            let max_price = 0;
            if(doc.length > 0){
                doc.forEach(element => {
                    //console.log("Money: "+ element.price);
                   max_price += element.price; 
                });
            }
            return max_price;            
        })
        .catch(err => {
            console.log("err: "+err);
        }));
    });
}



function validateMessage(publicKey, signature, mensagem) { 

    var verify = crypto.createVerify("sha1WithRSAEncryption"); //sha1WithRSAEncryption
    
    //publicKey = convertCertificate(publicKey);
    
    verify.update(mensagem);

    console.log('\n>>> Message:\n\n' + mensagem);
    
    let verification = verify.verify(publicKey, signature, SIGNATURE_FORMAT);

    console.log('\n>>> Verify: ' + verification);

    return verification;
};

function convertCertificate (cert) {
    //Certificate must be in this specific format or else the function won't accept it
    var beginCert = "-----BEGIN PUBLIC KEY-----";
    var endCert = "-----END PUBLIC KEY-----";

    cert = cert.replace("\n", "");
    cert = cert.replace(beginCert, "");
    cert = cert.replace(endCert, "");

    var result = beginCert;
    while (cert.length > 0) {

        if (cert.length > 64) {
            result += "\n" + cert.substring(0, 64);
            cert = cert.substring(64, cert.length);
        }
        else {
            result += "\n" + cert;
            cert = "";
        }
    }

    if (result[result.length ] != "\n")
        result += "\n";
    result += endCert + "\n";
    return result;
}

module.exports = router;