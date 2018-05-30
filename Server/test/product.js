process.env.NODE_ENV = 'test';
process.env.MONGO_ATLAS_PW = 'Acmecafe2018*';

const mongoose = require("mongoose");
const Product = require('../api/modules/products');
//Require the dev-dependencies
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../server');
const should = chai.should();
const request = require('request');
const expect  = require("chai").expect;

chai.use(chaiHttp);


describe('Product', () => {

  before(function () {
    server.listen(3000);
  });

  after(function () {
    server.close();
  });
    
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