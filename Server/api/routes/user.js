const express = require('express');
const router = express.Router();
const NodeRSA = require('node-rsa');


const User = require('../modules/user');

router.get('/', (req, res, next) => {
    res.status(200).json({
        message: 'Users ACME - GET'
    });
});

router.get('/all', (req, res, next) => {
    User.find()
        .exec()
        .then(doc => {
            if(doc){
                res.status(201).json(doc);
            }
        })
        .catch(error => {
            res.status(401).json({ message: "error fecthing data" });
        });
});

router.post('/new', (req, res, next) => {
    
    if(!isInt(req.body.nif))
    {
        console.log("Nif not integer");
        res.status(201).json({
            message: "Please add a Valid NIF Number",
            //user: user
        });
    }
    
    const user = new User({
        name: req.body.username,
        nif: req.body.nif,
        public_key: req.body.public_key
    });

    user
    .save()
    .then(result => {
        res.status(201).json({
            message: "New user added",
            user: user
        });
    })
    .catch(error => {
        res.status(202).json({
            message: "Error found",
            error: error
        });
    });
    
});


router.delete('/delete/:id' , (req, res, next) => {
    User.findByIdAndRemove(req.params.id, function (err, user) {
        if (err)
            throw err; 
    });
});


function isInt(value) {
    return !isNaN(value) && (function(x) { return (x | 0) === x; })(parseFloat(value))
}


module.exports = router;