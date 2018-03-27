const express = require('express');
const router = express.Router();

const Product = require('../modules/products');

router.get('/', (req, res, next) => {
    res.status(200).json({
        message: 'Product - GET'
    });
});


router.get('/all', (req, res,next) => {
    Product.find()
        .exec()
        .then(doc => {
            if(doc){
                res.status(201).json(doc);
            }
        })
        .catch(error => {
            res.status(401).json({ message: "error fecthing data" });
            //console.log("erro");
        });
});

router.post('/new', (req, res, next) => {

    const product = new Product({
        name: req.body.name,
        price: req.body.price,
        tag_number: req.body.tag
    });

    product
    .save()
    .then(result => {
        res.status(201).json({
            message: "New product added",
            product: product
        });
    })
    .catch(error => {
        res.status(201).json({
            message: "Error found",
            error: error
        });
    });

})

router.delete('/:id' , (req, res, next) => {
    Product.findByIdAndRemove(req.params.id, function (err, user) {
        if (err)
            throw err; 
    });
    res.status(201).json({
        message: "Product deleted",
    });
});

module.exports = router;