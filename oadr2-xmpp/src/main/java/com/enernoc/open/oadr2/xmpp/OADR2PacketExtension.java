package com.enernoc.open.oadr2.xmpp;

import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;

import org.jivesoftware.smack.packet.PacketExtension;

import com.enernoc.open.oadr2.model.OadrCreatedEvent;
import com.enernoc.open.oadr2.model.OadrDistributeEvent;
import com.enernoc.open.oadr2.model.OadrRequestEvent;
import com.enernoc.open.oadr2.model.OadrResponse;

/**
 * Packet extensions are created by XMPP extension implementations 
 * that understand a particular namespace that appears in the body
 * of an XMPP packet (message or IQ.)  In this case, this extension
 * will be created whenever the OpenADR 2.0 namespace 
 * (http://openadr.org/oadr-2.0[a or b]/2012/07) is encountered.  It's also
 * used to serialize (send) a packet that contains an OpenADR payload.
 * 
 * @see PacketExtension
 * @author tnichols
 */
public class OADR2PacketExtension implements PacketExtension {
	
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
	
	/**
	 * This will return the parsed OpenADR model object which will be 
	 * one of:
	 * {@link OadrDistributeEvent}, {@link OadrRequestEvent}, 
	 * {@link OadrCreatedEvent} or {@link OadrResponse}.
	 * @return the JAXB-parsed payload object.
	 */
	public Object getPayload() {
		return this.payload;
	}
	
	@Override public String getElementName() {
//		return this.payload.getName().getLocalPart();
		return this.payload.getClass().getAnnotation(XmlRootElement.class).name();
	}

	@Override public String getNamespace() {
//		return this.payload.getName().getNamespaceURI();
	    String namespace = this.payload.getClass().getAnnotation(XmlRootElement.class).namespace();
	    if ( "##default".equals( namespace ) )
	        namespace = this.payload.getClass().getPackage().getAnnotation( XmlSchema.class ).namespace();
		return namespace;
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
