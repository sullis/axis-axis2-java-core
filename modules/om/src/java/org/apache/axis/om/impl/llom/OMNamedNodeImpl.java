/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
 
package org.apache.axis.om.impl.llom;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNamedNode;
import org.apache.axis.om.OMNamespace;

import javax.xml.namespace.QName;

public class OMNamedNodeImpl extends OMNodeImpl implements OMNamedNode {
    protected OMNamespace ns;
    protected String localName;

    public OMNamedNodeImpl(OMElement parent) {
        super(parent);
    }

    public OMNamedNodeImpl(String localName, OMNamespace ns, OMElement parent) {
        super(parent);
        this.localName = localName;
        this.ns = ns;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public OMNamespace getNamespace() throws OMException {
        if (ns == null && parent != null) {
            ns = parent.getNamespace();
        }
        if (ns == null)
            throw new OMException("all elements in a soap message must be namespace qualified");
        return ns;
    }

    public String getNamespaceName() {
        if (ns != null) {
            return ns.getName();
        }
        return null;
    }

    /**
     * @param namespace
     */
    public void setNamespace(OMNamespace namespace) {
        this.ns = namespace;
    }

    public QName getQName() {
        QName qName = new QName(ns.getName(), localName, ns.getPrefix());
        return qName;
    }
}
