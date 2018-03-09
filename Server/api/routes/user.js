const express = require('express');
const router = express.Router();

const User = require('../modules/user');

router.get('/', (req, res, next) => {
    res.status(200).json({
        message: 'USers - GET'
    });
});

router.post('/new', (req, res, next) => {
    const user = new User({
        name: req.body.username,
        nif: req.body.nif,
        public_key: req.body.public_key
    });

    user
        .save()
        .then(result => {
            console.log(Sucess);
        })
        .catch(error => {
            console.log(error);
        });
    res.status(201).json({
        user: user
    });

});



module.exports = router;