
const mongoose = require("mongoose");
const Product = require('../api/modules/products');
const chai = require('chai');
const http = require('chai-http');
//const server = require('../server');
const should = chai.should();
const request = require("request");
const expect  = require("chai").expect;

chai.use(http);



describe('Product', () => {
  
  describe('/GET product', () => {

    var url = "http://localhost:3000/product/all";

    it("should return list of all existing products", function(done) {
      request(url, function(error, response, body) {
        expect(response.statusCode).to.equal(201);
        done();
      });
    });
  });
});
