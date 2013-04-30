package com.enernoc.open.oadr2.xmpp.v20b;

import javax.xml.bind.JAXBException;

/**
 * Convenience class for using JAXB with OpenADR 2.0b profile 
 * generated classes.  See {@link com.enernoc.open.oadr2.xmpp.JAXBManager} 
 * @author tnichols
 */
public class JAXBManager extends com.enernoc.open.oadr2.xmpp.JAXBManager {

    public static final String DEFAULT_JAXB_CONTEXT_PATH = 
        "com.enernoc.open.oadr2.model.v20b" +
        ":com.enernoc.open.oadr2.model.v20b.atom" +
        ":com.enernoc.open.oadr2.model.v20b.currency" +
        ":com.enernoc.open.oadr2.model.v20b.ei" +
        ":com.enernoc.open.oadr2.model.v20b.emix" +
        ":com.enernoc.open.oadr2.model.v20b.gml" +
        ":com.enernoc.open.oadr2.model.v20b.greenbutton" +
        ":com.enernoc.open.oadr2.model.v20b.power" +
        ":com.enernoc.open.oadr2.model.v20b.pyld" +
        ":com.enernoc.open.oadr2.model.v20b.siscale" +
        ":com.enernoc.open.oadr2.model.v20b.strm" +
        ":com.enernoc.open.oadr2.model.v20b.xcal" +
        ":com.enernoc.open.oadr2.model.v20b.xmldsig" +
        ":com.enernoc.open.oadr2.model.v20b.xmldsig11";
    
    public JAXBManager() throws JAXBException {
        super(DEFAULT_JAXB_CONTEXT_PATH);
    }
    
    @Override
    protected OADR2NamespacePrefixMapper createPrefixMapper() {
        return new OADR2NamespacePrefixMapper();
    }
}
