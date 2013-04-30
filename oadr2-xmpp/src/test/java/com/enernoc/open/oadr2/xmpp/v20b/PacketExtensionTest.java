package com.enernoc.open.oadr2.xmpp.v20b;


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

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.enernoc.open.oadr2.model.v20b.OadrDistributeEvent;
import com.enernoc.open.oadr2.model.v20b.OadrDistributeEvent.OadrEvent;
import com.enernoc.open.oadr2.model.v20b.OadrSignedObject;
import com.enernoc.open.oadr2.model.v20b.ResponseRequiredType;
import com.enernoc.open.oadr2.model.v20b.ei.CurrentValue;
import com.enernoc.open.oadr2.model.v20b.ei.EiActivePeriod;
import com.enernoc.open.oadr2.model.v20b.ei.EiEvent;
import com.enernoc.open.oadr2.model.v20b.ei.EiEventSignal;
import com.enernoc.open.oadr2.model.v20b.ei.EiEventSignals;
import com.enernoc.open.oadr2.model.v20b.ei.EiTarget;
import com.enernoc.open.oadr2.model.v20b.ei.EventDescriptor;
import com.enernoc.open.oadr2.model.v20b.ei.EventDescriptor.EiMarketContext;
import com.enernoc.open.oadr2.model.v20b.ei.EventStatusEnumeratedType;
import com.enernoc.open.oadr2.model.v20b.ei.Interval;
import com.enernoc.open.oadr2.model.v20b.ei.ObjectFactory;
import com.enernoc.open.oadr2.model.v20b.ei.PayloadFloatType;
import com.enernoc.open.oadr2.model.v20b.ei.SignalPayload;
import com.enernoc.open.oadr2.model.v20b.ei.SignalTypeEnumeratedType;
import com.enernoc.open.oadr2.model.v20b.emix.MarketContext;
import com.enernoc.open.oadr2.model.v20b.strm.Intervals;
import com.enernoc.open.oadr2.model.v20b.xcal.DateTime;
import com.enernoc.open.oadr2.model.v20b.xcal.Dtstart;
import com.enernoc.open.oadr2.model.v20b.xcal.DurationPropType;
import com.enernoc.open.oadr2.model.v20b.xcal.DurationValue;
import com.enernoc.open.oadr2.model.v20b.xcal.Properties;
import com.enernoc.open.oadr2.model.v20b.xcal.Properties.Tolerance;
import com.enernoc.open.oadr2.model.v20b.xcal.Properties.Tolerance.Tolerate;
import com.enernoc.open.oadr2.xmpp.OADR2IQ;
import com.enernoc.open.oadr2.xmpp.OADR2PacketExtension;

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
//        Presence presence = new Presence(Presence.Type.available);
//        c.sendPacket(presence);
        
        return c;
    }
    
	@Before public void setUp() throws Exception {
		if (username == null || passwd == null )
			throw new Exception("XMPP Username or password are null! Set the system properties 'xmpp-username' and 'xmpp-pass'");
			
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
	    venConnection.addPacketListener(packetCollector, new OADR2PacketFilter());
	    
	    IQ iq = new OADR2IQ(new OADR2PacketExtension(createEventPayload(), this.marshaller));
	    iq.setTo(venConnection.getUser());
	    iq.setType(IQ.Type.SET);
	    
	    vtnConnection.sendPacket(iq);
	    
	    Packet packet = packetCollector.getQueue().poll(5,TimeUnit.SECONDS);
	    
//	    Thread.sleep(1000000);
	    
	    assertNotNull( "Received packet should not be null", packet);
	    assertTrue( "Packet should be an instance of OADRIQ", packet instanceof OADR2IQ );
	    assertNotNull( "OADR payload should not be null", ((OADR2IQ)packet).getOADRPayload() );
	    
	    OADR2PacketExtension extension = (OADR2PacketExtension)packet.getExtension(XMLNS.OADR2.getNamespaceURI());
	    assertEquals( "Packet extension should be an oadrSignedObject element", 
	            "oadrSignedObject", extension.getElementName());
	    assertEquals( "Extension should have the OADR2 namespace", 
	            XMLNS.OADR2.getNamespaceURI(), extension.getNamespace());
	    Object pObj = extension.getPayload(); 
	    assertNotNull( "Extension payload should not be null", pObj );
	    assertTrue( "Extension payload should be an OadrSignedObject", pObj instanceof OadrSignedObject );
	    OadrSignedObject payload = (OadrSignedObject)pObj;
	    assertEquals( "Request ID should match what was sent", 
	            "test 1234", payload.getOadrDistributeEvent().getRequestID() );
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
	
	@SuppressWarnings("unchecked")
    protected OadrSignedObject createEventPayload() {
		final XMLGregorianCalendar startDttm = xmlDataTypeFac.newXMLGregorianCalendar("2012-01-01T00:00:00Z");
		final ObjectFactory eiObjectFactory = new ObjectFactory();
		
		return new OadrSignedObject()
		    .withOadrDistributeEvent( new OadrDistributeEvent()
		        .withRequestID( "test 1234" )
		        .withVtnID( "hi" )
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
    										.withCurrentValue(new CurrentValue(new PayloadFloatType(1.0f)))
    										.withSignalName("simple")
    										.withSignalType(SignalTypeEnumeratedType.LEVEL)
    										.withIntervals( new Intervals()
    												.withIntervals( new Interval()
    													.withStreamPayloadBases( eiObjectFactory.createSignalPayload(
    													        new SignalPayload( eiObjectFactory.createPayloadFloat( 
    													                new PayloadFloatType( 1.0f) ) ) ) )
    													.withDuration( new DurationPropType( new DurationValue( "PT1M" )) )
    													.withDtstart( new Dtstart(new DateTime( startDttm )) )
    												)
    										)
    								)
    						)
    				)
    				.withOadrResponseRequired(ResponseRequiredType.ALWAYS)
    			)
    		);
		
	}
}
