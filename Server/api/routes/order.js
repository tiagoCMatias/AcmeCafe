const express = require('express');
const router = express.Router();

const Order = require('../modules/order');

router.get('/', (req, res, next) => {
    res.status(200).json({
        message: 'Order - GET'
    });
});

router.post('/', (req, res, next) => {
    
    const product = JSON.stringify(req.body.products);
    
    const order = new Order({
        products: [req.body.products],
        user:  req.body.user,
        voucher: [req.body.voucher]
    });

    console.log(JSON.parse(product));

    res.status(201).json({
        message: "New Order",
        order: order
    });

});

module.exports = router;