package com.enernoc.open.oadr2.xmpp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * XML Namespace and prefix definitions for all URIs used in OpenADR 
 * profile 2.0a.  Note that payloads received are not guaranteed to use
 * the prefixes defined here, you <em>must</em> use a namespace-aware 
 * XML parser.  However these prefixes are handy if you want sent 
 * payloads to use the standard prefixes.  
 * 
 * @author tnichols
 */
public enum XMLNS {

    XSI( "http://www.w3.org/2001/XMLSchema-instance","xsi" ),
    OADR2( "http://openadr.org/oadr-2.0a/2012/07", "oadr2" ),
    EI( "http://docs.oasis-open.org/ns/energyinterop/201110","ei" ),
    PYLD( "http://docs.oasis-open.org/ns/energyinterop/201110/payloads", "pyld" ),
    EMIX( "http://docs.oasis-open.org/ns/emix/2011/06", "emix" ),
    ICAL( "urn:ietf:params:xml:ns:icalendar-2.0", "ical" ),
    STRM( "urn:ietf:params:xml:ns:icalendar-2.0:stream","strm" );
    
    private static final Map<String,XMLNS> NS_MAP;
    
    static {
        Map<String,XMLNS> temp = new HashMap<String,XMLNS>();
        for ( XMLNS val : XMLNS.values() ) 
            temp.put( val.uri, val );
        NS_MAP = Collections.unmodifiableMap( temp );
    }
    
    private final String uri;
    private final String prefix;
    
    XMLNS( final String uri, final String prefix) {
        this.uri = uri;
        this.prefix = prefix;
    }

    public String getNamespaceURI() {
        return this.uri;
    }
    
    public String getPrefix() {
        return this.prefix;
    }
    
    /**
     * Lookup the correct namespace from a given URI.
     * @param uri
     * @return
     */
    public static XMLNS fromURI( final String uri ) {
        if ( ! NS_MAP.containsKey( uri ) )
                throw new IllegalArgumentException( "URI " + uri + " is not a known OpenADR 2.0a XMLNS" );
        return NS_MAP.get(uri);
    }
}
