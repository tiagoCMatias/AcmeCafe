const express = require('express');
const router = express.Router();
const NodeRSA = require('node-rsa');


const User = require('../modules/user');


router.get("/all", (req, res, next) => {
    User.find()
        .exec()
        .then(doc => {
            if(doc.length > 0){
                res.status(201).json(doc);
            }
            else{
                res.status(201).json({message: 'No users Found'});    
            }
        })
        .catch(error => {
            res.status(401).json({ message: "error fecthing data" });
        });
});

router.get('/:id', (req, res, next) => {
    const request_id = req.params.id;
    User.findById(request_id)
        .exec()
        .then(doc => {
            if(doc)
            {
                res.status(200).json({
                    message: 'User Found',
                });
            }
            else {
                res.status(205).json({
                    message: 'User Not Found',
                });
            }
        })
        .catch(error => {
            res.status(404).json({
                message: 'User Not Found',
                error: error
            });
        });
});


router.post("/new", (req, res, next) => {
    
    const username = req.body.username;
    const nif = req.body.nif;
    const public_key = req.body.public_key

    const user = new User({
        name: username,
        nif: nif,
        public_key: public_key
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
        res.status(403).json({
            message: "Error found",
            error: error
        });
    });
    
});


router.delete("/:id" , (req, res, next) => {
    User.findByIdAndRemove(req.params.id, function (err, user) {
        if (err)
            throw err; 
    });
});


function isInt(value) {
    return !isNaN(value) && (function(x) { return (x | 0) === x; })(parseFloat(value))
}


module.exports = router;