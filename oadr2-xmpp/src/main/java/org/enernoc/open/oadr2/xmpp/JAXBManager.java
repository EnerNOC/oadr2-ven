package org.enernoc.open.oadr2.xmpp;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class JAXBManager {

	public static final String DEFAULT_JAXB_CONTEXT_PATH = "org.enernoc.open.oadr2.model";
	
	JAXBContext jaxbContext;  // thread-safe
	OADR2NamespacePrefixMapper nsMapper; // thread-safe
	
	public JAXBManager() throws JAXBException {
		this(DEFAULT_JAXB_CONTEXT_PATH);
	}
	public JAXBManager(final String jaxbContextPath) throws JAXBException {
		this.jaxbContext = JAXBContext.newInstance(jaxbContextPath);
		this.nsMapper = new OADR2NamespacePrefixMapper();
	}
	
	public JAXBContext getContext() {
		return this.jaxbContext;
	}

	Marshaller createMarshaller() throws JAXBException {
		Marshaller marshaller = this.jaxbContext.createMarshaller();
		this.nsMapper.addTo(marshaller);
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		return marshaller;
	}
}
