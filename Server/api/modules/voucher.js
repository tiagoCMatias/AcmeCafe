const mongoose = require('mongoose');
const Schema = mongoose.Schema;
const autoIncrement = require('mongoose-auto-increment');
const User = require('./user');

const voucherSchema = Schema({
    user_id: { type: Schema.Types.ObjectId , ref: "User", require: true},
    type: { type: String, require:true },
    state: { type: Boolean, require:true },
    createdAt: { type: Date, default: Date.now },
    updatedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Voucher', voucherSchema);

voucherSchema.plugin(autoIncrement.plugin, {
    model: 'Voucher',
    field: '_id',
    startAt: 1,
    incrementBy: 1
});