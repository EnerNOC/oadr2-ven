package com.enernoc.open.oadr2.xmpp;

import javax.xml.bind.Marshaller;

import org.jivesoftware.smack.packet.IQ;

public class OADR2IQ extends IQ {
	
	OADR2PacketExtension extension;
	
	public OADR2IQ(OADR2PacketExtension extension) {
	    this.init( extension );
	}
	
	public OADR2IQ(Object payload, Marshaller marshaller) {
        OADR2PacketExtension extension = new OADR2PacketExtension( payload, marshaller );
	    this.init( extension );
	}
	
	protected void init( OADR2PacketExtension extension ) {
	    this.setType(IQ.Type.SET);
	    this.extension = extension;
	    this.addExtension(extension);
	}
	
	/**
	 * Convenience method for calling
	 * <code>
	 * ((OADR2PacketExtension)iq.getExtension(XMLNS.OADR2.getNamespaceURI())).getPayload()
	 * </code>
	 * @return
	 */
	public Object getOADRPayload() {
	    return this.extension.getPayload();
	}

	/**
	 * Called by the Smack API to serialize the payload to XML
	 * @see IQ#getChildElementXML()
	 */
	@Override public String getChildElementXML() {
		return extension.toXML();
	}
}
