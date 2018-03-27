const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const productSchema = Schema({
    name: { type: String, require },
    tag_number: { type: Number  },
    price: { type: Number },
    createdAt: { type: Date, default: Date.now },
    updatedAt: { type: Date, default: Date.now }        
});

module.exports = mongoose.model('Product', productSchema);