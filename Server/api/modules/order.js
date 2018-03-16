const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const Product = require('./products');
const User = require('./user');
const Voucher = require('./voucher');


const orderSchema = Schema({
    products: [ {type: Schema.ObjectId, ref: 'Product', require:true }, ],
    user:  {type: Schema.ObjectId, ref: 'User', require:true },
    voucher: [ {type: Schema.ObjectId, ref: 'Voucher' }],
    createdAt: { type: Date, default: Date.now },
    updatedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Order', orderSchema);