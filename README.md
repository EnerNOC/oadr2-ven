# EnerNOC OpenADR 2.0 Reference Implementation #

This is ENOC's open source reference implemntation for the OpenADR 2.0 spec.  

See: http://openadr.org/

## Modules ##

**oadr2-model** Simple JAXB Bindings

**oadr2-xmpp** XMPP client implmentation

**oadr2-http** HTTP client implementation (TODO)

**oadr2-server** Server implementation (TODO)


## Development, Build, Testing ##

Use Maven: http://maven.apache.org

Dependencies will be handled automatically.  

To run XMPP unit tests with your Google Talk account, do the following:

    mvn test -Dxmpp-username='enoc-open@gmail.com' -Dxmpp-pass='blah'

To see debug output from the XMPP stream, add the following flag:
`-Dsmack.debugEnabled=true`


## License ##

This code is released under the Apache 2.0 software license.  Please take it 
and use the code in your (commercial or open source) product.  Contributions 
back to this project are also welcome!
