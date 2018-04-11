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
const voucherDiscount = "Discount 5%";


router.get('/', (req, res, next) => {
    res.status(200).json({
        message: 'Order - GET'
    });
});

router.post("/teste", (req, res, next) => {

    const user = req.body.user;
    const price = req.body.price;

    const signature = req.body.assinatura;

    var verification = false;
    getUserData(user)
        .then(userData => {
            var publicKey = userData.publicKey;

            var bb = ByteBuffer.fromHex(publicKey);

            bb = bb.toBase64().toString();

            var exponent = "AQAB";

            var pem = getPem(bb, exponent);

            var message = user;

            var verify = crypto.createVerify("sha1WithRSAEncryption"); //sha1WithRSAEncryption
                
            verify.update(message);

            console.log("\n>>> Message:\n\n" + message);
            
            verification = verify.verify(pem, signature, "hex");

            console.log("\n>>> Verify: " + verification);

            res.status(200).json({
                message: "Verify Signature",
                status: verification
            });
        })
        .catch(err => { 
            console.log("err: " + err); 
            res.status(200).json({
                message: "Verify Signature",
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

    var coffee_order = 0;

    var coffeeInCurrentOrder = 0;
    var coffeeVoucherInOrder = 0;
    var discountVoucherInOrder = 0;
    const coffeeID = "5aba9f42f8869a2848577fc7";

    var totalCoffeeVoucher = 0;
    var total_money_spent = 0;

    console.log("User: " + user);

    checkEncription(user, signature, user)
        .then(response => { 
            if(response == false) {
                res.status(403).json({
                    message: "Encryption Failed",
                });
            }
        })
        .catch(err => {
            res.status(403).json({
                message: "Encryption Failed",
                err: err
            });
        });

    products.forEach(element => {
        if(element._id == coffeeID)
        {
            coffeeInCurrentOrder = Number(element.qt);
        }
    });

    vouchers.forEach(element => {
        if(element.type == Voucher_Coffe)
        {
            coffeeVoucherInOrder++;
            updateVoucherState(element._id).then(res => { /*console.log("updating voucher state");*/ });
        }
        if(element.type == voucherDiscount)
        {
            discountVoucherInOrder++;
            updateVoucherState(element._id).then(res => { /*console.log("updating voucher state");*/ });
        }
    });

    
    getTotalMoneySpent(user)
        .then(discountDoc => { 
            total_money_spent = (discountDoc+price); 
            total_money_spent = (total_money_spent/100);
        });
    getUserData(user).then(doc => { totalCoffeeVoucher = doc.coffe; }).catch();

    getTotalCoffesOrdered(user, coffeeID)
        .then(totalCoffees => { 
            total_cafe = totalCoffees;
        getUserVoucher(user)
            .then(userVouchers => { 
                console.log(userVouchers);
                var voucherCoffeeUtil = userVouchers.coffe; 
                var voucherDiscount = userVouchers.money;
                var coffeePaid = (total_cafe - voucherCoffeeUtil);

                coffeePaid = coffeePaid + (coffeeInCurrentOrder - coffeeVoucherInOrder);
                
                console.log("Vouchers gerados:" + totalCoffeeVoucher);
                console.log("Total Cafes Pedidos:" + coffee_order);
                console.log("Vouchers Usados:" + voucherCoffeeUtil);
                console.log("Cafes Pagos:" + coffeePaid);

                var gerar = Math.floor(coffeePaid/3);

                if(totalCoffeeVoucher < gerar){
                    var qt = gerar - totalCoffeeVoucher;
                    for(var i = 0 ; i < qt; i++)
                    {
                        addUserNewCoffeVoucher(user, gerar).then(res => { /*console.log("adding voucher");*/ });
                    }  
                    console.log("Voucher Coffee Created: " + gerar);
                }

                if( voucherDiscount < total_money_spent ){
                    const new_qt = Math.floor(total_money_spent) - voucherDiscount;
                    
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

            var message = user;

            var verify = crypto.createVerify("sha1WithRSAEncryption"); //sha1WithRSAEncryption
                
            verify.update(message);

            //console.log('\n>>> Message:\n\n' + message);
            
            var verification = verify.verify(pem, signature, 'hex');

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
                createNewVoucher(user, voucherDiscount)
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
                    var coffe = 0;
                    var price = 0;
                    doc.forEach(element => {
                        if(element.type == Voucher_Coffe)
                            coffe = coffe +1 ;
                        if(element.type == voucherDiscount)
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

function getTotalCoffesOrdered(user, coffeeID){
    return new Promise(function (resolve, reject) {
        resolve(
            Order
            .find()
            .where('user_id').equals(user)
            .where('products._id').equals(coffeeID)
            .exec()
            .then(doc => {
                var max_coffe_qt = 0;
                doc.forEach(element => {
                    element.products.forEach(element => {
                        if(element._id == coffeeID)
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
            var max_price = 0;
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
    
    var verification = verify.verify(publicKey, signature, SIGNATURE_FORMAT);

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