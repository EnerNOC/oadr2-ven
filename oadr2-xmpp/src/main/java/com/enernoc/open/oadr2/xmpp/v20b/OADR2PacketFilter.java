package com.enernoc.open.oadr2.xmpp.v20b;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

/**
 * Convenience XMPP filter to catch all OpenADR 2.0b messages. 
 * Attach this filter to your XMPP client connection like so:
 * <code>
 * xmppConnection.addPacketListener(myPacketCollector, new OADRPacketFilter());
 * </code>
 * @see XMLNS#OADR2
 */
class OADR2PacketFilter implements PacketFilter {
	
	@Override public boolean accept(Packet packet) {
		// This namespace should match the filter used in smack.providers to 
		// register the packet extension:
		return packet.getExtension(XMLNS.OADR2.getNamespaceURI()) != null;
	}
}

