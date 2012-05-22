package org.enernoc.open.oadr2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.enernoc.open.oadr2.model.EiEvent.EiActivePeriod;
import org.enernoc.open.oadr2.model.EiEvent.EiEventSignals;
import org.enernoc.open.oadr2.model.EiEvent.EiEventSignals.EiEventSignal;
import org.enernoc.open.oadr2.model.EiEvent.EiEventSignals.EiEventSignal.CurrentValue;
import org.enernoc.open.oadr2.model.EiEvent.EiTarget;
import org.enernoc.open.oadr2.model.EiEvent.EventDescriptor;
import org.enernoc.open.oadr2.model.EiEvent.EventDescriptor.EiMarketContext;
import org.enernoc.open.oadr2.model.Interval.SignalPayload;
import org.enernoc.open.oadr2.model.OadrDistributeEvent.OadrEvent;
import org.enernoc.open.oadr2.model.Properties.Dtstart;
import org.enernoc.open.oadr2.model.Properties.Tolerance;
import org.enernoc.open.oadr2.model.Properties.Tolerance.Tolerate;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Some sanity checking for our JAXB-generated models
 * 
 * @author <a href='mailto:tnichols@enernoc.com'>Tom Nichols</a>
 *
 */
public class OadrDistributeEventTest {

	JAXBContext jaxbContext;
	Marshaller marshaller;
	DatatypeFactory xmlDataTypeFac;
	
	SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
	Schema schema;
	Validator validator;
	
	@Before public void setup() throws Exception {
		this.jaxbContext = JAXBContext.newInstance("org.enernoc.open.oadr2.model");
		this.marshaller = jaxbContext.createMarshaller();
		xmlDataTypeFac = DatatypeFactory.newInstance();

		schema = sf.newSchema( getClass().getResource("/schema/oadr_20a_032212.xsd") );
		this.validator = schema.newValidator();
	}
	
	@Test public void testSerialize() throws Exception {
		final XMLGregorianCalendar startDttm = xmlDataTypeFac.newXMLGregorianCalendar("2012-01-01T00:00:00Z");
		OadrDistributeEvent distribEventPayload = new OadrDistributeEvent()
			.withRequestID("test-123")
			.withVtnID("vtn-123")
			.withOadrEvent( new OadrEvent()
				.withEiEvent(
					new EiEvent()
						.withEiTarget( new EiTarget()
								.withVenID("ven-1234") )
						.withEventDescriptor(new EventDescriptor()
								.withEventID("event-1234")
								.withModificationNumber(0)
								.withEventStatus(EventStatusEnumeratedType.FAR)
								.withPriority(1L)
								.withEiMarketContext(new EiMarketContext("http://enernoc.com"))
								.withCreatedDateTime(startDttm))
						.withEiActivePeriod(new EiActivePeriod()
								.withProperties(new Properties()
										.withDtstart(new Dtstart(startDttm))
										.withDuration(new DurationPropType("PT1M"))
										.withTolerance( new Tolerance(new Tolerate(null,"PT5S")))
										.withXEiNotification(new DurationPropType("PT5S"))
								))
						.withEiEventSignals( new EiEventSignals()
								.withEiEventSignal( new EiEventSignal()
										.withSignalID("hi there")
										.withCurrentValue(new CurrentValue(new PayloadFloat(1.0f)))
										.withSignalName("simple")
										.withSignalType(SignalTypeEnumeratedType.LEVEL)
										.withIntervals( new Intervals()
												.withInterval( new Interval()
													.withSignalPayload( new SignalPayload(new PayloadFloat(1.0f)))
													.withDuration( new DurationPropType("PT1M"))
													.withUid(new Uid("abc"))
												)
										)
								)
						)
				)
				.withOadrResponseRequired(ResponseRequiredType.ALWAYS)
			);

		assertEquals("test-123", distribEventPayload.getRequestID());
		
		StringWriter out = new StringWriter();
		this.marshaller.marshal(distribEventPayload, out);
		assertNotNull(out.toString());
		assertTrue(out.toString().length() > 0);
		
		assertEquals(1.0f, distribEventPayload.getOadrEvent().get(0).getEiEvent()
				.getEiEventSignals().getEiEventSignal().get(0)
				.getIntervals().getInterval().get(0)
				.getSignalPayload().getPayloadFloat().getValue(), 0);
		
		assertEquals( 0, validate(out.toString()) );
	}


	protected int validate( String doc ) throws IOException, SAXException {
		ErrorCollector errorCollector = new ErrorCollector();
		validator.setErrorHandler( errorCollector );
		validator.validate( new StreamSource( new StringReader( doc ) ) );

		return errorCollector.errors.size();
	}
	
	class ErrorCollector extends DefaultHandler {
		List<SAXParseException> errors = new ArrayList<SAXParseException>();
		@Override public void error( SAXParseException e ) throws SAXException {
			System.out.println( "SAX Parse error (" + e.getLineNumber() + "): " + e.getMessage() );
			errors.add( e );
		}
	}
}
