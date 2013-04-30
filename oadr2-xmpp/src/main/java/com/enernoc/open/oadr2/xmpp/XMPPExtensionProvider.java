package com.enernoc.open.oadr2.xmpp;

import javax.xml.bind.JAXBException;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 * This is an implementation of the Smack {@link IQProvider} to 
 * provide automatic de-serialization of OpenADR payloads in XMPP.
 * IQs that contain an OpenADR payload will automatically be parsed
 * as {@link OADR2IQ} instances. 
 * @author tnichols
 */
public class XMPPExtensionProvider implements PacketExtensionProvider, IQProvider {

	protected JAXBManager jaxb;
	protected PullUnmarshaller unmarshaller;

	public XMPPExtensionProvider() {
		try { 
			this.jaxb = new JAXBManager();
			this.unmarshaller = new PullUnmarshaller(jaxb.getContext());
		}
		catch ( JAXBException ex ) {
			throw new RuntimeException("Error initializing JAXB context",ex);
		}
	}
	
	public XMPPExtensionProvider(JAXBManager jaxb) {
		try {
			this.jaxb = jaxb;
			this.unmarshaller = new PullUnmarshaller(jaxb.getContext());
		}
		catch ( JAXBException ex ) {
			throw new RuntimeException("Error initializing JAXB context",ex);
		}
//		System.out.println("++++++++++++++++++++ LOADED OADR Packet Extension Provider");
	}
	
	@Override
	public OADR2PacketExtension parseExtension(XmlPullParser pullParser) throws Exception {
//		System.out.println( "++++++++++++++++++++++++++ Parsing Extension!!!" );
		return new OADR2PacketExtension( unmarshaller.unmarshalSubTree(pullParser), this.jaxb );		
	}

	@Override
	public IQ parseIQ(XmlPullParser parser) throws Exception {
//		System.out.println( "++++++++++++++++++++++++++ Parsing IQ!!!" );
		return new OADR2IQ( parseExtension(parser) );
	}
}
