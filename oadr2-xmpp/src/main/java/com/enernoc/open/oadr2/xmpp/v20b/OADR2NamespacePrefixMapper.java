package com.enernoc.open.oadr2.xmpp.v20b;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps OpenADR 2.0b namespaces to standard prefixes.  Note this is
 * a feature specific to certain XML parser implementations and may
 * not work in all cases.
 * 
 * @see XMLNS
 * @see com.enernoc.open.oadr2.xmpp.OADR2NamespacePrefixMapper
 * @see com.sun.xml.bind.marshaller.NamespacePrefixMapper
 * @author tnichols
 */
public class OADR2NamespacePrefixMapper extends com.enernoc.open.oadr2.xmpp.OADR2NamespacePrefixMapper {
	
    static final String[] allURIs;
    
    static {
        List<String> uris = new ArrayList<String>();
        for ( XMLNS ns : XMLNS.values() ) uris.add( ns.getNamespaceURI() );
        allURIs = uris.toArray( new String[uris.size()] );
    }

    /**
     * @see com.enernoc.open.oadr2.xmpp.OADR2NamespacePrefixMapper
     */
    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
//    	System.out.println("----------------- Asked for namespace for " + namespaceUri);
        try {
            XMLNS ns = XMLNS.fromURI( namespaceUri );
            if ( ns == XMLNS.OADR2 && ! requirePrefix ) return "";
            return ns.getPrefix();
        }
        catch ( IllegalArgumentException ex ) {
            return suggestion;
        }
    }
    
    /**
     * @see com.enernoc.open.oadr2.xmpp.OADR2NamespacePrefixMapper
     */
    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return allURIs;
    }
}
