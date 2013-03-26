package com.enernoc.open.oadr2.xmpp;

import javax.xml.bind.annotation.XmlRootElement;

import com.enernoc.open.oadr2.model.OadrDistributeEvent;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

/**
 * Convenience XMPP filter to catch all OpenADR messages. Attach this filter 
 * to your XMPP client connection like so:
 * <code>
 * xmppConnection.addPacketListener(myPacketCollector, new OADRPacketFilter());
 * </code>
 */
class OADR2PacketFilter implements PacketFilter {
	
	static final String OADR_XMLNS = OadrDistributeEvent.class.getAnnotation(XmlRootElement.class).namespace();
	
	@Override public boolean accept(Packet packet) {
		// This namespace should match the filter used in smack.providers to 
		// register the packet extension:
//		System.out.println(packet.getClass().toString());
		return packet.getExtension(OADR_XMLNS) != null;
	}
}

