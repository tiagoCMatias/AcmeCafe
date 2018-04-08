const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const userSchema = Schema({
    name: { type: String, require:true },
    nif: { type: Number },
    public_key: { type: String, require:true },
    private_coffe_voucher_qt : { type: Number, default: 0},
    private_price_voucher_qt : { type: Number, default: 0},
    createdAt: { type: Date, default: Date.now },
    updatedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', userSchema);