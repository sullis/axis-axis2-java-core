/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.jaxws.provider;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class SourceProviderTests extends ProviderTestCase {

    private String endpointUrl = "http://localhost:8080/axis2/services/SourceProviderService";
    private QName serviceName = new QName("http://ws.apache.org/axis2", "SourceProviderService");
    private String xmlDir = "xml";


    protected void setUp() throws Exception {
            super.setUp();
    }

    protected void tearDown() throws Exception {
            super.tearDown();
    }

    public SourceProviderTests(String name) {
        super(name);
    }
    
    public void testProviderSource(){
        try{
        	String resourceDir = new File(providerResourceDir, xmlDir).getAbsolutePath();
        	String fileName = resourceDir+File.separator+"web.xml";
        	
        	File file = new File(fileName);
        	InputStream inputStream = new FileInputStream(file);
        	StreamSource xmlStreamSource = new StreamSource(inputStream);
        	
        	Service svc = Service.create(serviceName);
        	svc.addPort(portName,null, null);
        	Dispatch<Source> dispatch = svc.createDispatch(portName, Source.class, null);
        	Map<String, Object> requestContext = dispatch.getRequestContext();
        	requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);
        	System.out.println(">> Invoking Source Provider Dispatch");
        	Source response = dispatch.invoke(xmlStreamSource);

        	System.out.println(">> Response [" + response.toString() + "]");
        	
        }catch(Exception e){
        	e.printStackTrace();
        }
        
    }
}