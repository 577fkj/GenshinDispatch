const fs = require('fs')
const protobuf = require("protobufjs");

let result = Buffer.from(fs.readFileSync('./data.txt'), 'base64')

protobuf.load('schema.proto', function (err, root) {
    if (err)
        throw err;

    let QueryRegionListHttpRsp = root.lookupType('QueryRegionListHttpRsp')

    let message = QueryRegionListHttpRsp.decode(result)
    console.log(message)
    // console.log(JSON.stringify(message))
})