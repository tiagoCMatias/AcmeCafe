
const mongoose = require("mongoose");
const User = require('../api/modules/user');
const chai = require('chai');
const http = require('chai-http');
//const server = require('../server');
const request = require("request");
const expect  = require("chai").expect;

chai.use(http);


describe('User', () => {
  
  describe('/GET user', () => {

    var url = "http://localhost:3000/user";

    it("returns status 200", function(done) {
      request(url, function(error, response, body) {
        expect(response.statusCode).to.equal(200);
        done();
      });
    });
  });
});
