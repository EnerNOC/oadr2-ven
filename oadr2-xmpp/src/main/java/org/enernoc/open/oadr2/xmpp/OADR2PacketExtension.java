package org.enernoc.open.oadr2.xmpp;

import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.jivesoftware.smack.packet.PacketExtension;

class OADR2PacketExtension implements PacketExtension {
	
	// JAXB object don't inherit a base class, so you'll need to cast to 
	// the correct type, which you can infer based on the root element name
	// or just do `instanceof` checks
	Object payload; 
	 
	JAXBManager jaxb;
	Marshaller marshaller;
	
	public OADR2PacketExtension(Object e) {
		this.payload = e;
	}
	
	public OADR2PacketExtension(Object e, JAXBManager jaxb) {
		this.payload = e;
		this.jaxb = jaxb;
	}
	
	public OADR2PacketExtension(Object e, Marshaller marshaller) {
		this.payload = e;
		this.marshaller = marshaller;
	}
	
	public Object getPayload() {
		return this.payload;
	}
	
	@Override public String getElementName() {
//		return this.payload.getName().getLocalPart();
		return this.payload.getClass().getAnnotation(XmlRootElement.class).name();
	}

	@Override public String getNamespace() {
//		return this.payload.getName().getNamespaceURI();
		return this.payload.getClass().getAnnotation(XmlRootElement.class).namespace();
	}

	@Override public String toXML() {
		try {
			if ( this.marshaller == null ) // TODO synchronize 
				this.marshaller = this.jaxb.createMarshaller();
			
			StringWriter sw = new StringWriter();
			this.marshaller.marshal(this.payload, sw);
			return sw.toString();
		}
		catch ( JAXBException ex ) {
			throw new RuntimeException("JAXB error marshalling XML to string", ex);
		}
	}	
}
