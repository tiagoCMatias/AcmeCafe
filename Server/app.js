const express = require('express');
const app = express();
const morgan = require('morgan');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');

//connect to mongo
const url = 'mongodb://Acme:'+ process.env.MONGO_ATLAS_PW +'@acmecafe-shard-00-00-y4uxw.mongodb.net:27017,acmecafe-shard-00-01-y4uxw.mongodb.net:27017,acmecafe-shard-00-02-y4uxw.mongodb.net:27017/test?ssl=true&replicaSet=AcmeCafe-shard-0&authSource=admin'
//const url = 'mongodb://127.0.0.1:27017/acme';

console.log(url);
mongoose.connect(url);

//routes
const userRoute = require('./api/routes/user');
const productRoute = require('./api/routes/product');
const orderRoute = require('./api/routes/order');
const voucherRoute = require('./api/routes/voucher');
const deleteRoute = require('./api/routes/delete');


app.use(morgan('dev'));
app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());

// Add headers
app.use(function (req, res, next) {

    // Website you to allow to connect
    res.setHeader('Access-Control-Allow-Origin', '*');

    // Request headers to allow
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, cache-control, pragma, Accept, Authorization');

    if(req.method === "OPTIONS")
    {
        res.header('Access-Control-Allow-Methods', 'GET, HEAD, POST, OPTIONS, PUT, PATCH, DELETE');
        return res.status(200).json({});
    }
    // Pass to next layer of middleware
    next();
});

/** Handling routes */
app.use('/order', orderRoute);
app.use('/product', productRoute);
app.use('/user', userRoute);
app.use('/voucher', voucherRoute);
app.use('/delete', deleteRoute);

/** Error Handling */
app.use( (req, res, next) => {
    const error = new Error('Not Found');
    error.status = 404;
    next(error);
});

app.use( (error, req, res, next) => {
    res.status(error.status || 500);
    console.log(error);
    res.json({
        error: {
            message: 'error'
        }
    });
});

module.exports = app;