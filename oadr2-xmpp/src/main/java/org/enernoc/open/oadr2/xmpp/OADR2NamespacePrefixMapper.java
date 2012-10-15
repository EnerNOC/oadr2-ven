package org.enernoc.open.oadr2.xmpp;

import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

class OADR2NamespacePrefixMapper extends NamespacePrefixMapper {
	
	public OADR2NamespacePrefixMapper() {}
	
	public OADR2NamespacePrefixMapper(Marshaller marshaller) throws PropertyException {
		this.addTo(marshaller);
	}
	
	public void addTo( Marshaller marshaller ) throws PropertyException {
//		System.out.println("--------------- Marshaller class: " + marshaller.getClass().toString());
		// TODO this probably only works for com.sun.xml.bind.v2.runtime.MarshallerImpl
		try {
			marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", this); 
		}
		catch ( Exception ex ) {
			marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", this);
		}		
	}

    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
//    	System.out.println("----------------- Asked for namespace for " + namespaceUri);
        if( "http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri) )
            return "xsi";
        if( "http://openadr.org/oadr-2.0a/2012/07".equals(namespaceUri) )
        	return requirePrefix ? "oadr2" : "";
        if( "http://docs.oasis-open.org/ns/energyinterop/201110".equals(namespaceUri) )
        	return "ei";
        if( "http://docs.oasis-open.org/ns/energyinterop/201110/payloads".equals(namespaceUri) )
        	return "pyld";
        if( "http://docs.oasis-open.org/ns/emix/2011/06".equals(namespaceUri) )
        	return "emix";
        if( "urn:ietf:params:xml:ns:icalendar-2.0".equals(namespaceUri) )
        	return "ical";
        if( "urn:ietf:params:xml:ns:icalendar-2.0:stream".equals(namespaceUri) )
        	return "strm";
        return suggestion;
    }
        
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { "http://openadr.org/oadr-2.0a/2012/07", 
        		"http://docs.oasis-open.org/ns/energyinterop/201110",
        		"http://docs.oasis-open.org/ns/energyinterop/201110/payloads", 
        		"http://docs.oasis-open.org/ns/emix/2011/06",
        		"urn:ietf:params:xml:ns:icalendar-2.0",
        		"urn:ietf:params:xml:ns:icalendar-2.0:stream",
        		"http://www.w3.org/2001/XMLSchema-instance",
        	};
    }
}
