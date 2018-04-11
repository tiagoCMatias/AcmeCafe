var crypto = require('crypto');
var fs = require('fs');
var ALGORITHM = "sha384"; // Accepted: any result of crypto.getHashes(), check doc dor other options
var SIGNATURE_FORMAT = "hex"; // Accepted: hex, latin1, base64

function getPublicKeySomehow() {

    var pubKey = fs.readFileSync('RSA_2048_Public_Key.pem', 'utf8');
    console.log("\n>>> Public key: \n\n" + pubKey);

    return pubKey;
}

function getPrivateKeySomehow() {

    var privKey = fs.readFileSync('RSA_2048_Private_Key.pem', 'utf8');
    console.log(">>> Private key: \n\n" + privKey);

    return privKey;
}

function getSignatureToVerify(data) {

    var privateKey = getPrivateKeySomehow();
    var sign = crypto.createSign(ALGORITHM);
    sign.update(data);
    var signature = sign.sign(privateKey, SIGNATURE_FORMAT);

    console.log(">>> Signature:\n\n" + signature);

    return signature;
}

module.exports = function() { 

    this.tryCrypto = function() { 
        var hashes = crypto.getHashes();
        console.log(hashes); 
        var ciphers = crypto.getCiphers();
        console.log(ciphers);     
    };
    this.checkValidation = function(publicKey, data) { 
        //var publicKey = getPublicKeySomehow();
        var verify = crypto.createVerify(ALGORITHM);
        //var data = "This message will be signed with a RSA private key in PEM format and then verified with a RSA public key in PEM format.";
        var signature = getSignatureToVerify(data);
        
        console.log('\n>>> Message:\n\n' + data);
        
        verify.update(data);
        
        return verification = verify.verify(publicKey, signature, SIGNATURE_FORMAT);
    };
    //etc
}