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
 
package org.apache.axis.util;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Ajith
 * Date: Feb 17, 2005
 * Time: 5:57:47 PM
 */
public class ServiceListBean {
    private ArrayList serviceList;

    public ServiceListBean() {
        this.serviceList = new ArrayList();
    }

    public void addService(ServiceItemBean service){
        this.serviceList.add(serviceList);
    }

}

