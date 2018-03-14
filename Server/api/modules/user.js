const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const userSchema = Schema({
    name: { type: String, require },
    nif: { type: Number },
    public_key: { type: String, require },
    createdAt: { type: Date, default: Date.now },
    updatedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', userSchema);