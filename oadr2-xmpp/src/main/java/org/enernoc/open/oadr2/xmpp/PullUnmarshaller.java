package org.enernoc.open.oadr2.xmpp;
// Original source can be found at:
// http://java.net/projects/jaxb/sources/version1/content/trunk/jaxb-ri/samples/samples-src/pull-parser/src/PullUnmarshaller.java?rev=203

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * Use is subject to the license terms.
 */
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/**
 * Wraps {@link Unmarshaller} and provide a functionality to unmarshal
 * from a {@link XmlPullParser}.
 * 
 * <p>
 * This is particularly useful to unmarshal just a part of a document
 * into a JAXB object.
 * 
 * <p>
 * It is often the case that you don't need complete random access to
 * the entire document. Instead, you usually just need random access
 * to a limited region of XML.
 * 
 * In this scenario, it is typically a waste of resources to bind the
 * entire document into a single JAXB object tree. It will consume
 * more memory, it will cause a greater latency.
 * 
 * <p>
 * In some cases, your interesting XML document is contained inside
 * an "envelope", which you are not so interested in.
 * 
 * In this scenario, this class can be used to just bind the real meat
 * to a JAXB object. In this way, you don't need to compile a schema
 * that corresponds to the envelope.
 *  
 * <h2>Example</h2>
 * <p>
 * Consider the following address book XML:
 * <pre><xmp>
 * <addressBook>
 *   <contact>
 *     <name>John Doe</name>
 *     <email>john@acme.com</email>
 *   </contact>
 *   <contact>
 *     <name>Jane Smith</name>
 *     <email>jane@acme.org</email>
 *   </contact>
 *   ...
 * </addressBook>
 * </xmp></pre>
 * 
 * <p>
 * Suppose the address book is very long and your program is just looking
 * for the record of "Jane Smith". Unmarshalling the whole document
 * is easy, but it  forces the entire document to be loaded, which
 * consumes more memory.
 * 
 * <p>
 * This document can be processed in the following way:
 * 
 * <pre><xmp>
 * // set up a parser
 * XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 * factory.setNamespaceAware(true);
 * XmlPullParser xpp = factory.newPullParser();
 * 
 * // create an unmarshaller
 * PullUnmarshaller u = new PullUnmarshaller(jaxbContext);
 * 
 * xpp.setInput( new FileInputStream("contact.xml"), null );
 * xpp.nextTag();   // move to the root element
 * xpp.require(XmlPullParser.START_TAG,"","addressBook"); // check the root tag name
 * xpp.nextTag();   // move to the first <contact> element.
 * 
 * while(xpp.getEventType()==XmlPullParser.START_TAG) {
 *    // unmarshall one <contact> element into a JAXB Contact object
 *    Contact contact = (Contact)u.unmarshalSubTree(xpp);
 *    processContact(contact);
 *  
 *    if( xpp.getEventType()==XmlPullParser.TEXT)
 *       xpp.next();    // skip the whitespace between <contact>s.
 * }
 * </xmp></pre>  
 * 
 * <p>
 * Note that the unmarshaller will think that the &lt;contact> element
 * is the root element of a document, so it is important for the
 * &lt;contact> element to be declared as a global element.
 * 
 * <p>
 * It unmarshals one &lt;contact> at a time, then process it.
 * The added benefit is that you can stop the processing as soon
 * as you find the info you are looking for. This makes the program
 * a lot (average of roughly 2x) faster.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class PullUnmarshaller {
    
    private final Unmarshaller unmarshaller;

    /**
     * Constructs a {@link PullUnmarshaller} from a {@link JAXBContext}.
     * <p>
     * This is the equivalent of
     * <code>new PullUnmarshaller(context.createUnmarshaller());</code>
     */
    public PullUnmarshaller(JAXBContext context) throws JAXBException {
        this(context.createUnmarshaller());
    }
    
    /**
     * Constrcuts a {@link PullUnmarshaller} by wrapping an
     * Unmarshaller instance.
     * <p>
     * All the real work of unmarshalling is done through
     * the specified unmarshaller. So configure it appropriately
     * and don't reuse this unmarshaller instance while
     * this {@link PullUnmarshaller} is in use.
     */
    public PullUnmarshaller(Unmarshaller _unmarshaller) {
        if( _unmarshaller==null )
            throw new IllegalArgumentException();
        this.unmarshaller = _unmarshaller;
    }
    
    /**
     * Reads a XML sub-tree and unmarshals it into a JAXB object.
     * <p>
     * the parser must point to a start element. This method will read
     * the whole sub-tree, send it to the JAXB unmarshaller (which was
     * specfied to the constructor), then return the obtained object.
     * <p>
     * After the successful completion of this method, the parser will
     * point to the event next to the end element of the sub-tree.
     * <p>
     * If a processing fails, the parser will be left at somewhere
     * in the middle of the sub-tree.
     * 
     * 
     * @param parser
     *      A pull parser from which an object will be unmarshalled.
     *      The parser must be at the start element. IOW,
     *      <code>pp.getEventType() == XmlPullParser.START_TAG</code>
     *      has to be true.
     * 
     * @return
     *      Always return a non-null valid object.
     * 
     * @throws IllegalStateException
     *      If the parser is not set at a start element event,
     *      this exception will be thrown.
     * 
     * @throws JAXBException
     *      In case of any XML wellformedness error, IO error, and
     *      JAXB unmarshalling errors. Before this exception is thrown,
     *      it is reported to the {@link javax.xml.bind.ValidationEventHandler}
     *      of the unmarshaller first.
     */
    public Object unmarshalSubTree( XmlPullParser parser ) throws IllegalStateException, JAXBException {
        UnmarshallerHandler handler = unmarshaller.getUnmarshallerHandler();
        
        try {
            new Worker(parser,handler).parse();
        } catch( XmlPullParserException e ) {
            throw new JAXBException(e);
        } catch( SAXException e ) {
            // forward the error  TODO:report to the error handler
            throw new JAXBException(e);
        } catch( IOException e ) {
            // forward the error  TODO:report to the error handler
            throw new JAXBException(e);
        }
        
        return handler.getResult();
    }
    
    
    class Worker {
        private final XmlPullParser pp;
        private final ContentHandler handler;
        
        Worker(XmlPullParser _pp, ContentHandler _handler) {
            this.pp = _pp;
            this.handler = _handler;
        }
        
        public void parse() throws SAXException, XmlPullParserException, IOException {
            if( pp.getEventType() != XmlPullParser.START_TAG )
                throw new IllegalStateException("a parser has to be at the start element event");
            
            handler.setDocumentLocator(locator);
            handler.startDocument();
            
            int nsc = pp.getNamespaceCount(pp.getDepth()-1);
            
            declareNamespace(  0,nsc);
            parseElement();
            undeclareNamespace(0,nsc);
            
            // skips the last end element.
//            pp.next(); // Don't do this - it breaks smack's XPP traversal logic - TMN
            
            handler.endDocument();
        }

        /**
         * Declares namespace bindings between indices [i,j).
         */
        private void declareNamespace(int i, int j) throws SAXException, XmlPullParserException {
            for( ; i<j; i++ ) {
                String prefix = pp.getNamespacePrefix(i);
                if(prefix==null)    prefix="";
                handler.startPrefixMapping( prefix, pp.getNamespaceUri(i) );
            }
        }

        /**
         * Undeclares namespace bindings between indices [i,j).
         */
        private void undeclareNamespace(int i, int j) throws SAXException, XmlPullParserException {
            for( j--; j>=i; j-- ) {
                String prefix = pp.getNamespacePrefix(j);
                if(prefix==null)    prefix="";
                handler.endPrefixMapping( prefix );
            }
        }
        
        // used by the parseElement method as if it is a local variable.
        private final int[] startAndLen = new int[2];
        
        /**
         * Parses an element and its subordinates recursively.
         * 
         * A parser has to be at the START_TAG event, and when
         * this method leaves, the parser is at the corresponding END_TAG
         * event.
         */
        private void parseElement() throws XmlPullParserException, IOException, SAXException {
            
            int nss = pp.getNamespaceCount(pp.getDepth()-1);
            int nse = pp.getNamespaceCount(pp.getDepth()  );
            
            String qname;
            String prefix = pp.getPrefix();
            if(prefix==null)    qname = pp.getName();
            else                qname = prefix + ':' + pp.getName();
            
            declareNamespace(nss,nse);
            handler.startElement( pp.getNamespace(), pp.getName(), qname, attributes );
            
            outer:
            while(true) {
                switch(pp.next()) {
                case XmlPullParser.TEXT:
                    char[] buf = pp.getTextCharacters(startAndLen);
                    handler.characters(buf, startAndLen[0], startAndLen[1] );
                    break;
            
                case XmlPullParser.START_TAG:
                    parseElement();
                    break;
                    
                case XmlPullParser.END_TAG:
                    handler.endElement( pp.getNamespace(), pp.getName(), qname );
                    undeclareNamespace(nss,nse);
                    return;
                }
            }
        }
    

    
        /**
         * {@link Attributes} implementation that uses the
         * {@link XmlPullParser} as the real data storage.
         */        
        private final Attributes attributes = new Attributes() {
            
            public int getLength() {
                return pp.getAttributeCount();
            }
    
            public String getURI(int idx) {
                return pp.getAttributeNamespace(idx);
            }
    
            public String getLocalName(int idx) {
                return pp.getAttributeName(idx);
            }
    
            public String getQName(int idx) {
                String prefix = pp.getAttributePrefix(idx);
                if(prefix==null)    return pp.getAttributeName(idx);
                else    return prefix+':'+pp.getAttributeName(idx);
            }
    
            public String getType(int idx) {
                return pp.getAttributeType(idx);
            }
    
            public String getValue(int idx) {
                return pp.getAttributeValue(idx);
            }
    
            public int getIndex(String uri, String local) {
                for( int i=getLength()-1; i>=0; i-- ) {
                    if( getURI(i).equals(uri) && getLocalName(i).equals(local) )
                        return i;
                }
                return -1;
            }
    
            public int getIndex(String qname) {
                for( int i=getLength()-1; i>=0; i-- ) {
                    if( getQName(i).equals(qname) )
                        return i;
                }
                return -1;
            }
    
            public String getType(String uri, String local) {
                int idx = getIndex(uri,local);
                if(idx==-1) return null;
                else        return getType(idx);
            }
    
            public String getType(String qname) {
                int idx = getIndex(qname);
                if(idx==-1) return null;
                else        return getType(idx);
            }
    
            public String getValue(String uri, String local) {
                int idx = getIndex(uri,local);
                if(idx==-1) return null;
                else        return getValue(idx);
            }
    
            public String getValue(String qname) {
                int idx = getIndex(qname);
                if(idx==-1) return null;
                else        return getValue(idx);
            }
        };
        
        /**
         * {@link Locator} implementation that uses the
         * {@link XmlPullParser} as the real data storage.
         */        
        private final Locator locator = new Locator() {
            public String getPublicId() {
                return null;
            }
    
            public String getSystemId() {
                return null;
            }
    
            public int getLineNumber() {
                return pp.getLineNumber();
            }
    
            public int getColumnNumber() {
                return pp.getColumnNumber();
            }
        };
    }
}
