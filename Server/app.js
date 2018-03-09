const express = require('express');
const app = express();
const morgan = require('morgan');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');

//routes
const userRoute = require('./api/routes/user');
const productRoute = require('./api/routes/product');
const orderRoute = require('./api/routes/order');
const voucherRoute = require('./api/routes/voucher');


//connect to mongo
const url = 'mongodb+srv://' + process.env.MONGO_ATLAS_USER + ':' + process.env.MONGO_ATLAS_PW + '@acmecafe-y4uxw.mongodb.net/test'
//mongoose.connect(url);

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
app.use('/orders', orderRoute);
app.use('/product', productRoute);
app.use('/user', userRoute);
app.use('/vouchers', voucherRoute);

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