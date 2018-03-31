const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const Product = require('./products');
const User = require('./user');
const Voucher = require('./voucher');

const orderSchema = Schema({
    products: [{
        qt: Number,
        _id: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Product'
        }
    }],
    user:  {type: Schema.ObjectId, ref: 'User', require:true },
    voucher: [{
        type: String,
        _id: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'User'
        }
    }],
    createdAt: { type: Date, default: Date.now },
    updatedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Order', orderSchema);