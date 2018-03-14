process.env.NODE_ENV = 'test';
process.env.MONGO_ATLAS_PW = 'Acmecafe2018*';

const mongoose = require("mongoose");
const User = require('../api/modules/user');
const chai = require('chai');
const http = require('chai-http');
const server = require('../server');
const request = require("request");
const expect  = require("chai").expect;
let should = chai.should();

chai.use(http);


describe('User', () => {

  after(function (done) {
    server.close(function () {
      mongoose.connection.close(done)
    });
  });
  describe('/GET user', () => {

    var url = "http://localhost:3000/user";

    it("returns status 201", function(done) {
      chai.request(server)
            .get('/user/all')
            .end((err, res) => {
                res.should.have.status(201);
                res.body.should.be.a('array');
                res.body.length.should.be.eql(0);
              done();
            });
    });
  });
});
