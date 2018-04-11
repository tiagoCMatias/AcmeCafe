const express = require('express');
const router = express.Router();

const Voucher = require('../modules/voucher');

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
            voucher: voucher
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
    Voucher
        .find()
        .where('user_id').equals(request_id)
        .where('state').equals(false)
        .exec()
        .then(doc => {
            if(doc.length > 0)
            {
                res.status(200).json({
                    message: 'Voucher Found',
                    voucher: doc
                });
            }
            else {
                res.status(205).json({
                    message: 'Voucher Not Found',
                });
            }
            
        })
        .catch(error => {
            res.status(400).json({
                message: 'error fectchin vouchers',
                error: error
            });
        });
});

module.exports = router;