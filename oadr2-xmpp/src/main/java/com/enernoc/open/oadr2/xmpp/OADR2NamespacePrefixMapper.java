package com.enernoc.open.oadr2.xmpp;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class OADR2NamespacePrefixMapper extends NamespacePrefixMapper {
	
    static final String[] allURIs;
    
    static {
        List<String> uris = new ArrayList<String>();
        for ( XMLNS ns : XMLNS.values() ) uris.add( ns.getNamespaceURI() );
        allURIs = uris.toArray( new String[uris.size()] );
    }

	public OADR2NamespacePrefixMapper() {}
	
	public OADR2NamespacePrefixMapper(Marshaller marshaller) throws PropertyException {
		this.addTo(marshaller);
	}
	
	/**
	 * This attempts to add the namespace prefix mapper to the JAXB marshaller, so that
	 * serialized payloads will have 'standard' XML namespace prefixes.  Note this is 
	 * only likely to work if you're using the Sun XML APIs.
	 * @param marshaller
	 * @throws PropertyException
	 */
	public void addTo( Marshaller marshaller ) throws PropertyException {
//		System.out.println("--------------- Marshaller class: " + marshaller.getClass().toString());
		// TODO this probably only works for com.sun.xml.bind.v2.runtime.MarshallerImpl
		try {
			marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", this); 
		}
		catch ( Exception ex ) {
		    try {
		        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", this);
		    }
		    catch ( Exception e2 ) {
		        System.err.println( "Couldn't enable the Namespace Prefix mapper, we're probably not"
		                + " using the Sun XML impl. " + e2.getMessage() );
		    }
		}		
	}

	/**
	 * Return the 'preferred' namespace prefix for the given URI.  This is called
	 * automatically if you're using the Sun Java XML APIs if .
	 * @see NamespacePrefixMapper#getPreferredPrefix(String, String, boolean)
	 */
	@Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
//      System.out.println("----------------- Asked for namespace for " + namespaceUri);
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
	 * @see NamespacePrefixMapper#getPreDeclaredNamespaceUris()
	 */
	@Override
    public String[] getPreDeclaredNamespaceUris() {
        return allURIs;
    }
}
