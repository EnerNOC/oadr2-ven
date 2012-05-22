package org.enernoc.open.oadr2.xmpp;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.enernoc.open.oadr2.model.OadrDistributeEvent;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class OADRDisitrbuteEventPacketExtensionProvider implements
		PacketExtensionProvider {

	PullUnmarshaller unmarshaller;
	Marshaller marshaller;
	
	public OADRDisitrbuteEventPacketExtensionProvider() {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance("org.enernoc.open.oadr2");
			this.marshaller = jaxbContext.createMarshaller();
			this.unmarshaller = new PullUnmarshaller(jaxbContext);
		}
		catch ( JAXBException ex ) {
			throw new RuntimeException("Error initializing JAXB context",ex);
		}
	}
	
	@Override
	public PacketExtension parseExtension(XmlPullParser pullParser) throws Exception {
		return new OadrDistributeEventPacket(
				(OadrDistributeEvent)unmarshaller.unmarshalSubTree(pullParser) );		
	}
	
	
	class OadrDistributeEventPacket implements PacketExtension {
		
		OadrDistributeEvent payload;
		
		public OadrDistributeEventPacket(OadrDistributeEvent e) {
			this.payload = e;
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
				marshaller.marshal(this, sw);
				return sw.toString();
			}
			catch ( JAXBException ex ) {
				throw new RuntimeException("JAXB error marshalling XML to string", ex);
			}
		}		
	}
}