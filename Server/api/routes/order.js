const express = require('express');
const Promise = require('bluebird');
const mongoose = Promise.promisifyAll(require('mongoose'));
const router = express.Router();
const getPem = require('rsa-pem-from-mod-exp');

var crypto = require('crypto');
var fs = require('fs');
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

router.post('/test', (req, res, next) => {
    
    const user = req.body.user;
    let signature = req.body.assinatura;

    getUserData(user).then(userDoc => { 
        let publicKey = userDoc.publicKey;

        let new_key = Buffer.from(publicKey, 'base64');

        let exponent = "65537";

        exponent = Buffer.from(exponent, 'base64');

        publicKey = getPem(new_key, exponent);

        //signature = Buffer.from(signature, 'base64');


        console.log(">>> A:" + signature);
        console.log(">>> E:" + exponent);
        console.log(">>> U:" + user);
        console.log(">>> P:" + publicKey);
        validateMessage(publicKey, signature, Buffer.from(user, 'base64'));

        res.status(201).json({
            message: "New order added",
        });

    });

});

router.post('/new', (req, res, next) => {

    const user = req.body.user;
    const products = req.body.products;
    const vouchers = req.body.vouchers;
    const price = req.body.price;

    const signature = req.body.assinatura;
/*
    getUserData(user).then(voucherDoc => { 
        let publicKey = voucherDoc.publicKey;
        publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALZy5szJUfnlxKxVOFkxmhyToln3NzrJENpvTrglP+zyyhnu3ZeBSpj6Fa03zV5FoVQ+BT+zxciBDm18KjFJUqMCAwEAAQ==";
        let verification = validateMessage(publicKey, signature);

    }).catch(err => {
        console.log("erro na signature");
    });
*/
    const order = new Order({
        products: products,
        user_id:  user,
        price: price,
        voucher: vouchers
    });

    const coffee_id = "5ac399be111e652110ee1c51";
    let maxMoney = 0;
    let maxCoffe = 0;
    let coffee_qt = 0;
    

    products.forEach(element => {
        if(element._id == coffee_id)
        {
            coffee_qt = element.qt
            //console.log("qt: " + element.qt);
        }
    });

    registerOrder(order).then(doc => {
        console.log("Order register");
    });

    let voucher_in_order_coffee = 0;

    vouchers.forEach(element => {
        if(element.type == Voucher_Coffe)
            voucher_in_order_coffee++;
        updateVoucherState(element._id).then(res => { /*console.log("updating voucher state");*/ });
    });

  

    getTotalMoneySpent(user)
        .then(discountDoc => { 
            maxMoney = (discountDoc+price); 
            maxMoney = (maxMoney/100);
        });

    getTotalCoffesOrdered(user, coffee_id)
        .then(coffeeDoc => {
            //console.log("pago: " + coffeeDoc);
            //console.log("pedido: " + coffee_qt);
            //let coffee = (Number(coffeeDoc) + Number(coffee_qt));
            //console.log("coffee: " + coffee);
            //maxCoffe = coffee/3;
            //console.log("maxCoffee: " + maxCoffe);
            //maxCoffe = Math.floor(maxCoffe);
            maxCoffe = (Number(coffeeDoc) + Number(coffee_qt));
            console.log("maxCoffe: " + maxCoffe);
        });
    let total_vouchers = 0;
    getUserData(user).then(data => { total_vouchers = data.coffe; console.log(data);});

    getUserVoucher(user).then(voucherDoc => { 
        let voucher_coffe = voucherDoc.coffe;
        let voucher_money = voucherDoc.money;
        //let gerar = (maxCoffe - voucher_coffe - voucher_in_order_coffee);
        let cafes_pagos = maxCoffe - voucher_coffe;
 
        let cafe_para_gerar_voucher = (cafes_pagos - (total_vouchers*3));

        let voucher_a_gerar = Math.floor(cafe_para_gerar_voucher/3);
        console.log("total_vouchers: " + total_vouchers);
        console.log("cafes_pagos: " + cafes_pagos);
        console.log("voucher_a_gerar: " + voucher_a_gerar);
        if(voucher_a_gerar)
        {
            const new_qt = voucher_a_gerar;
            for(var i = 0 ; i < new_qt; i++)
            {
                addUserNewCoffeVoucher(user, new_qt).then(res => { /*console.log("adding voucher");*/ });
            }  
            console.log("Voucher Coffee Created: " + new_qt);
        }

        if( voucher_money < maxMoney ){
            const new_qt = Math.floor(maxMoney) - voucher_money;
            
            for(var i = 0 ; i < new_qt; i++)
            {
                addUserNewMoneyVoucher(user, maxCoffe).then(res => {  });
            } 
            console.log("Voucher Discount Created: " + new_qt);
        }
        res.status(201).json({
            message: "New order added",
            order: order
        });
    });

});


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
        //console.log("Created new voucher");
        //res.status(201).json({ message: "All Done"});
    })
    .catch(error => {
        //console.log("Error creating voucher");
    });
}

function addUserNewCoffeVoucher(user, voucher_qt){
    
    return new Promise(function (resolve, reject) {
        resolve(
            User.update({ _id: user}, { $set: { private_coffe_voucher_qt: voucher_qt }})
            .exec()
            .then(doc => {
                createNewVoucher(user, Voucher_Coffe)
                //console.log("User updated");
            })
            .catch(err => {
                //console.log("Error :" + err);
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
                //console.log("User updated");
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
                //console.log("User updated");
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
            doc.forEach(element => {
                //console.log("Money: "+ element.price);
               max_price += element.price; 
            });
            return max_price;
            
        })
        .catch(err => {
            console.log(err);
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