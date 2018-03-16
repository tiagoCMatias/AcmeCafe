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

  before(function () {
    server.listen(3000);
    
  });
  beforeEach((done) => { //Before each test we empty the database
    User.remove({}, (err) => { 
        done();         
    });     
  });

  after(function (done) {
    server.close(function () {
      mongoose.connection.close(done)
    });
  });
  
/*
  describe('/Post user', () => {
    var url = "http://localhost:3000/user";
    it("should NOT add a USER without public_key", function (done) {
      let user = {
        name: "Teste",
        nif: "123412"
      };
      chai.request(url)
        .post('/new')
        .set('content-type', 'application/x-www-form-urlencoded')
        .send(user)
        .end((err, res) => {
          res.should.have.status(202);
          res.body.should.be.a('object');
          res.body.should.have.property('error');
        });
    });
  });*/

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
