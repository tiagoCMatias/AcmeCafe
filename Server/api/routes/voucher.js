const express = require('express');
const router = express.Router();

const Voucher = require('../modules/voucher');

router.get('/', (req, res, next) => {
    res.status(200).json({
        message: 'Vouchers - GET'
    });
});

router.post('/new', (req, res, next) => {
    const voucher = new Voucher({
        user_id: req.body.user_id,
        type: req.body.voucher_type,
        state: false,
    });

    voucher
    .save()
    .then(result => {
        res.status(201).json({
            message: "New voucher added",
            //user: user
        });
    })
    .catch(error => {
        res.status(202).json({
            message: "Error found",
            error: error
        });
    });
})


router.get('/:id', (req, res, next) => {
    const request_id = req.params.id;
    Voucher.find({'user_id' : request_id})
        .exec()
        .then(doc => {
            res.status(200).json({
                voucher: doc
            });
        })
        .catch(error => {
            res.status(400).json({
                message: 'error fectchin vouchers',
                error: error
            });
        });
});

module.exports = router;