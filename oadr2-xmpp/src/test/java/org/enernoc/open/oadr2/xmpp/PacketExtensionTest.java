package org.enernoc.open.oadr2.xmpp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.enernoc.open.oadr2.model.CurrentValue;
import org.enernoc.open.oadr2.model.DateTime;
import org.enernoc.open.oadr2.model.Dtstart;
import org.enernoc.open.oadr2.model.DurationPropType;
import org.enernoc.open.oadr2.model.DurationValue;
import org.enernoc.open.oadr2.model.EiActivePeriod;
import org.enernoc.open.oadr2.model.EiEvent;
import org.enernoc.open.oadr2.model.EiEventSignal;
import org.enernoc.open.oadr2.model.EiEventSignals;
import org.enernoc.open.oadr2.model.EiTarget;
import org.enernoc.open.oadr2.model.EventDescriptor;
import org.enernoc.open.oadr2.model.EventDescriptor.EiMarketContext;
import org.enernoc.open.oadr2.model.EventStatusEnumeratedType;
import org.enernoc.open.oadr2.model.Interval;
import org.enernoc.open.oadr2.model.Intervals;
import org.enernoc.open.oadr2.model.MarketContext;
import org.enernoc.open.oadr2.model.OadrDistributeEvent;
import org.enernoc.open.oadr2.model.OadrDistributeEvent.OadrEvent;
import org.enernoc.open.oadr2.model.ObjectFactory;
import org.enernoc.open.oadr2.model.PayloadFloat;
import org.enernoc.open.oadr2.model.Properties;
import org.enernoc.open.oadr2.model.Properties.Tolerance;
import org.enernoc.open.oadr2.model.Properties.Tolerance.Tolerate;
import org.enernoc.open.oadr2.model.ResponseRequiredType;
import org.enernoc.open.oadr2.model.SignalPayload;
import org.enernoc.open.oadr2.model.SignalTypeEnumeratedType;
import org.enernoc.open.oadr2.model.Uid;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Well, this is a functional test, not a unit test, but it proves we can 
 * send an OADR payload over XMPP with Smack.
 * 
 * Use system properties to pass your Google Talk username and password like so:
 *  -Dxmpp-username='tmnichols@gmail.com' -Dxmpp-pass='blah'
 *  
 * @author tnichols
 *
 */
public class PacketExtensionTest {

	static final String OADR2_XMLNS = "http://openadr.org/oadr-2.0a/2012/07";
	
	static final String username = System.getProperty("xmpp-username");
	static final String passwd = System.getProperty("xmpp-pass");

    ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
    XMPPConnection vtnConnection;
    XMPPConnection venConnection;
    
    Marshaller marshaller;
	DatatypeFactory xmlDataTypeFac;
    
    protected XMPPConnection connect(String resource) throws XMPPException {
    	XMPPConnection c = new XMPPConnection(connConfig);
	    
        c.connect();        
        c.login(username, passwd, resource);        
        Presence presence = new Presence(Presence.Type.available);
        c.sendPacket(presence);
        
        return c;
    }
    
	@Before public void setUp() throws Exception {
		JAXBManager jaxb = new JAXBManager();
		this.marshaller = jaxb.createMarshaller();
		xmlDataTypeFac = DatatypeFactory.newInstance();

	    this.venConnection = connect("ven"); 		
	    this.vtnConnection = connect("vtn"); 		
	}
	
	@After public void tearDown() throws Exception {
		if ( this.venConnection != null && venConnection.isConnected() ) venConnection.disconnect();
		if ( this.vtnConnection != null && vtnConnection.isConnected() ) vtnConnection.disconnect();
	}
	
	//@Test 
	public void testNamespaces() throws Exception {
		StringWriter out = new StringWriter();
		marshaller.marshal(createEventPayload(), out);
		System.out.println(out.toString());
	}
	
	@Test public void testPacketExtension() throws Exception {
		
		OADRPacketCollector packetCollector = new OADRPacketCollector();
	    venConnection.addPacketListener(packetCollector, new OADRPacketFilter());
	    
	    IQ iq = new OADR2IQ(new OADR2PacketExtension(createEventPayload(), this.marshaller));
	    iq.setTo(venConnection.getUser());
	    iq.setType(IQ.Type.SET);
	    
	    vtnConnection.sendPacket(iq);
	    
	    Packet packet = packetCollector.getQueue().poll(5,TimeUnit.SECONDS);
	    
//	    Thread.sleep(1000000);
	    
	    assertNotNull(packet);
	    OADR2PacketExtension extension = (OADR2PacketExtension)packet.getExtension(OADR2_XMLNS);
	    assertEquals("oadrDistributeEvent", extension.getElementName());
	    assertEquals(OADR2_XMLNS, extension.getNamespace());
	    Object pObj = extension.getPayload(); 
	    assertNotNull( pObj );
	    assertTrue( pObj instanceof OadrDistributeEvent );
	    OadrDistributeEvent payload = (OadrDistributeEvent)pObj;
	    assertEquals("test-123", payload.getRequestID());
	}
	
	class OADRPacketCollector implements PacketListener {
		BlockingQueue<Packet> packets = new ArrayBlockingQueue<Packet>(10);
		@Override public void processPacket(Packet packet) {
			// Assuming there is an OADRPacketFilter, only packets with an 
			// OadrDistributeEventPacket extension will be passed here.
			this.packets.offer(packet);
		}
		
		BlockingQueue<Packet> getQueue() { return this.packets; }
	}
	
	class OADRPacketFilter implements PacketFilter {
		@Override public boolean accept(Packet packet) {
			// This namespace should match the filter used in smack.providers to 
			// register the packet extension:
//			System.out.println(packet.getClass().toString());
			return packet.getExtension(OADR2_XMLNS) != null;
		}
	}
	
	protected OadrDistributeEvent createEventPayload() {
		final XMLGregorianCalendar startDttm = xmlDataTypeFac.newXMLGregorianCalendar("2012-01-01T00:00:00Z");
		final ObjectFactory oadrTypes = new ObjectFactory();
		
		return new OadrDistributeEvent()
			.withRequestID("test-123")
			.withVtnID("vtn-123")
			.withOadrEvents( new OadrEvent()
				.withEiEvent(
					new EiEvent()
						.withEiTarget( new EiTarget()
								.withVenIDs("ven-1234") )
						.withEventDescriptor(new EventDescriptor()
								.withEventID("event-1234")
								.withModificationNumber(0)
								.withEventStatus(EventStatusEnumeratedType.FAR)
								.withPriority(1L)
								.withEiMarketContext(new EiMarketContext(
										new MarketContext("http://enernoc.com")))
								.withCreatedDateTime(new DateTime(startDttm)))
						.withEiActivePeriod(new EiActivePeriod()
								.withProperties(new Properties()
										.withDtstart(new Dtstart(new DateTime(startDttm)))
										.withDuration(new DurationPropType(new DurationValue("PT1M")))
										.withTolerance( new Tolerance(new Tolerate(new DurationValue("PT5S"))))
										.withXEiNotification(new DurationPropType(new DurationValue("PT5S")))
								))
						.withEiEventSignals( new EiEventSignals()
								.withEiEventSignals( new EiEventSignal()
										.withSignalID("hi there")
										.withCurrentValue(new CurrentValue(new PayloadFloat(1.0f)))
										.withSignalName("simple")
										.withSignalType(SignalTypeEnumeratedType.LEVEL)
										.withIntervals( new Intervals()
												.withIntervals( new Interval()
													.withStreamPayloadBase( oadrTypes.createSignalPayload(
															new SignalPayload(new PayloadFloat(1.0f))))
													.withDuration( new DurationPropType(new DurationValue("PT1M")))
													.withUid(new Uid("abc"))
												)
										)
								)
						)
				)
				.withOadrResponseRequired(ResponseRequiredType.ALWAYS)
			);
		
	}
}
