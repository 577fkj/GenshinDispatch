const crypto = require('crypto')
const fs = require('fs')
const protobuf = require("protobufjs");

const KEY_SIZE = 256

// keyId = 2 国服
// keyId = 3 海外服
const pubKey = fs.readFileSync('./OS.pem')
const privKey = fs.readFileSync('../keys/OSCB.pem')

let rawData = fs.readFileSync('./data.json')
let parsedData = JSON.parse(rawData)

let content = Buffer.from(parsedData.content, 'base64')
let sign = Buffer.from(parsedData.sign, 'base64')

let res = []

while (content.length > 0) {
    let data = content.slice(0, KEY_SIZE)
    content = content.slice(data.length)

    res.push(crypto.privateDecrypt({
        key: privKey,
        padding: crypto.constants.RSA_PKCS1_PADDING
    }, Buffer.from(data)))
}

let result = Buffer.concat(res)
let verified = crypto.createVerify('RSA-SHA256')
    .update(result)
    .verify(pubKey, sign)

console.log('Verified signature =>', verified)

protobuf.load('schema.proto', function (err, root) {
    if (err)
        throw err;

    let QueryCurrRegionHttpRsp = root.lookupType('QueryCurrRegionHttpRsp')

    let message = QueryCurrRegionHttpRsp.decode(result)
    console.log(message)
    // console.log(JSON.stringify(message))
})