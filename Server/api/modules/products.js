'use strict';

/**
 * Module dependencies.
 */
const mongoose = require('mongoose');
const Schema = mongoose.Schema;
const autoIncrement = require('mongoose-auto-increment');

const productSchema = Schema({
    name: { type: String, require },
    tag_number: { type: Number  },
    price: { type: Number },
    qt: { type: Number, default: 1 },
    createdAt: { type: Date, default: Date.now },
    updatedAt: { type: Date, default: Date.now }        
});

module.exports = mongoose.model('Product', productSchema);