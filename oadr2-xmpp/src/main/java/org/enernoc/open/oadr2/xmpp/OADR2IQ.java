package org.enernoc.open.oadr2.xmpp;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;

public class OADR2IQ extends IQ {
	
	PacketExtension payload;
	
	public OADR2IQ(PacketExtension payload) {
		this.payload = payload;
		this.addExtension(payload);
		this.setType(IQ.Type.SET);
	}

	@Override public String getChildElementXML() {
		return payload.toXML();
	}
}
