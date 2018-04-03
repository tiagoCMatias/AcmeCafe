const express = require('express');
const Promise = require('bluebird');
const mongoose = Promise.promisifyAll(require('mongoose'));
const router = express.Router();

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

router.get('/test', (req, res, next) => {
    
    const user_id = "5ac35ddd3c27772e24f5813f";

    Order
        .find()
        .populate('user products voucher')
        .where()
        .where('user_id').equals(user_id)
        .exec()
        .then(doc => {
            //console.log(doc);
            res.status(200).json({
                message: 'Order - GET',
                order: doc
            });
        })
        .catch(err => {
            console.log(err);
            res.status(200).json({
                message: 'Order - GET',
                err: err
            });
        })
    
});

router.post('/new', (req, res, next) => {

    const user = req.body.user;
    const products = req.body.products;
    const vouchers = req.body.vouchers;
    
    const order = new Order({
        products: products,
        user_id:  user,
        price: req.body.price,
        voucher: vouchers
    });

    const coffe_id = "5abfce2a57e4b63528582f55";
    let maxMoney = 0;
    let maxCoffe = 0;

    products.forEach(element => {
        if(element._id == coffe_id)
        {
            console.log("qt: " + element.qt);
        }
    });
    
    console.log("P:"+req.body.price);
    //console.log(vouchers);

    vouchers.forEach(element => {
        updateVoucherState(element._id).then(res => { /*console.log("updating voucher state");*/ });
    });

    getTotalMoneySpent(user).then(discountDoc => { maxMoney = discountDoc});
    getTotalCoffesOrdered(user, coffe_id).then(coffeeDoc => { maxCoffe = coffeeDoc});
    getUserVouchers(user).then(voucherDoc => { 
        let voucher_coffe = voucherDoc.coffe;
        let voucher_money = voucherDoc.money;
        if(voucher_coffe < maxCoffe)
        {
            console.log("MaxCof: " + maxCoffe);
            console.log("VoucherCof: " + voucher_coffe);
            const new_qt = (maxCoffe - voucher_coffe);
            for(var i = 0 ; i < new_qt; i++)
            {
                addUserNewCoffeVoucher(user, maxCoffe).then(res => { /*console.log("adding voucher");*/ });
            }  
            console.log("You should add more coffe:" + new_qt);
        }
        registerOrder(order).then(doc => {
            res.status(201).json({
                message: "New order added",
                order: order
            });
        });
    });

});

/*
getTotalCoffesOrdered = (user, coffe_id) => {
    Order
        .find()
        .where('user').equals(user)
        .where('products._id').equals(coffe_id)
        .exec()
        .then(doc => {
            let len = doc.length;
            let max_coffe_qt = 0;
            doc.forEach(element => {
                element.products.forEach(element => {
                    if(element._id == coffe_id)
                    {
                        console.log("Confirmed ID");
                        max_coffe_qt += element.qt;
                    }
                });                    
            });
            return max_coffe_qt;
            //console.log("Qt Coffe: "+ Math.floor(max_coffe_qt/3));
            //res.status(201).json(doc);
        })
        .catch(err => {
            console.log(err);
        });
}

getTotalMoneySpent = (user) => {
    Order
        .find()
        .where('user').equals(user)
        .select('price')
        .exec()
        .then(doc => {
            let max_price = 0;
            doc.forEach(element => {
               //console.log("Price: "+ element.price);
               max_price += element.price; 
            });
            //console.log("TotalPrice: "+ max_price);
            return max_price;
        })
        .catch(err => {
            console.log(err);
        });
        if(voucher_type == "Coffe")
        {
            User.update({ _id: user}, { $set: { private_coffe_voucher_qt: new_qt }})
                .exec()
                .then()
                .catch();
        }
        if(voucher_type == "Money")
        {
            User.update({ _id: user}, { $set: { private_price_voucher_qt: new_qt }})
                .exec()
                .then(doc => {
                    console.log("User updated");
                })
                .catch(err => {
                    console.log("Error :" + err);
                });
        }
}*/

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
            User.update({ _id: user}, { $set: { private_coffe_voucher_qt: voucher_qt }})
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

function getUserVouchers(user){
    return new Promise(function (resolve, reject) {
        resolve(
            User
            .findById(user)
            .exec()
            .then(doc => {
                return {
                    coffe: doc.private_coffe_voucher_qt,
                    money: doc.private_price_voucher_qt
                };                
            })
            .catch(err => {
                console.log(err);
            })
        );
    });
}

function getTotalCoffesOrdered(user, coffe_id){
    return new Promise(function (resolve, reject) {
        resolve(
            Order
            .find()
            .where('user_id').equals(user)
            .where('products._id').equals(coffe_id)
            .exec()
            .then(doc => {
                let max_coffe_qt = 0;
                doc.forEach(element => {
                    element.products.forEach(element => {
                        if(element._id == coffe_id)
                        {
                            //console.log("Confirmed ID - " + element.qt);
                            max_coffe_qt += element.qt;
                        }
                    });                    
                });
                return Math.floor(max_coffe_qt/3);
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
module.exports = router;