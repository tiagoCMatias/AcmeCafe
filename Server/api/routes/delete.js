const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');

const Order = require('../modules/order');
const User = require('../modules/user');
const Product = require('../modules/products');
const Voucher = require('../modules/voucher');


router.get('/softdelete', (req, res, next) => {

    Order.collection.drop();
    User.collection.drop();
    //Product.collection.drop();
    Voucher.collection.drop();
    /*mongoose.connection.db.dropCollection("identitycounters", function(err, result) {
         console.log("All is deleted");
    });*/

    res.status(200).json({
        message: 'Users, Orders and Vouchers deleted'
    });
});

router.get('/harddelete', (req, res, next) => {

    Order.collection.drop();
    User.collection.drop();
    //Product.collection.drop();
    Voucher.collection.drop();
    /*mongoose.connection.db.dropCollection("identitycounters", function(err, result) {
         console.log("All is deleted");
    });*/

    res.status(200).json({
        message: 'Users, Orders and Vouchers deleted'
    });
});



module.exports = router;