package org.enernoc.open.oadr2.xmpp;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.enernoc.open.oadr2.model.OadrDistributeEvent;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

class OadrDistributeEventPacket implements PacketExtension {
	
	OadrDistributeEvent payload;
	JAXBContext jaxb;
	Marshaller marshaller;
	
	public OadrDistributeEventPacket(OadrDistributeEvent e) {
		this.payload = e;
	}
	
	public OadrDistributeEventPacket(OadrDistributeEvent e, JAXBContext jaxb) {
		this.payload = e;
		this.jaxb = jaxb;
	}
	
	public OadrDistributeEventPacket(OadrDistributeEvent e, Marshaller marshaller) {
		this.payload = e;
		this.marshaller = marshaller;
	}
	
	public OadrDistributeEvent getPayload() {
		return payload;
	}
	
	@Override public String getElementName() {
		return "oadrDistributeEvent";
	}

	@Override public String getNamespace() {
		return "http://openadr.org/oadr-2.0a/2012/03";
	}

	@Override public String toXML() {
		StringWriter sw = new StringWriter();
		try {
			if ( this.marshaller == null ) {
				this.marshaller = this.jaxb.createMarshaller();
				OADR2NamespacePrefixMapper nsMapper = new OADR2NamespacePrefixMapper(this.marshaller);
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			}
			this.marshaller.marshal(this.payload, sw);
			return sw.toString();
		}
		catch ( JAXBException ex ) {
			throw new RuntimeException("JAXB error marshalling XML to string", ex);
		}
	}		

	public static class Provider implements PacketExtensionProvider, IQProvider {
	
		JAXBContext jaxbContext;
		PullUnmarshaller unmarshaller;
		
		public Provider() {
			try {
				this.jaxbContext = JAXBContext.newInstance("org.enernoc.open.oadr2.model");
				this.unmarshaller = new PullUnmarshaller(jaxbContext);
			}
			catch ( JAXBException ex ) {
				throw new RuntimeException("Error initializing JAXB context",ex);
			}
//			System.out.println("++++++++++++++++++++ LOADED OADR Packet Extension Provider");
		}
		
		@Override
		public PacketExtension parseExtension(XmlPullParser pullParser) throws Exception {
//			System.out.println( "++++++++++++++++++++++++++ Parsing Extension!!!" );
			return new OadrDistributeEventPacket( 
					(OadrDistributeEvent)unmarshaller.unmarshalSubTree(pullParser), 
					this.jaxbContext );		
		}

		@Override
		public IQ parseIQ(XmlPullParser parser) throws Exception {
//			System.out.println( "++++++++++++++++++++++++++ Parsing IQ!!!" );
			return new OADR2IQ( new OadrDistributeEventPacket( 
					(OadrDistributeEvent)unmarshaller.unmarshalSubTree(parser), 
		 			this.jaxbContext ) );
		}
	}
}