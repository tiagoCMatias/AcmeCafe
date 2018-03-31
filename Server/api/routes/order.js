const express = require('express');
const router = express.Router();

const Order = require('../modules/order');

router.get('/', (req, res, next) => {
    res.status(200).json({
        message: 'Order - GET'
    });
});

router.post('/new', (req, res, next) => {
    /*
    const product = JSON.stringify(req.body.products);
    
    const order = new Order({
        products: [req.body.products],
        user:  req.body.user,
        voucher: [req.body.voucher]
    });
    */
    //console.log(JSON.parse(product));

    const products = req.body.products;
    const user = req.body.user;
    console.log(products);

    const order = new Order({
        products: req.body.products,
        user:  req.body.user,
    });

    order
    .save()
    .then(result => {
        res.status(201).json({
            message: "New order added",
            order: result
        });
    })
    .catch(error => {
        console.log(error);
        res.status(401).json({
            message: "Error found",
            error: error
        });
    });

});

module.exports = router;