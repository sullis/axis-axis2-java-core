package org.apache.axis2.deployment;

import org.apache.axis2.engine.AxisObserver;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.description.Parameter;

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
 *
 * 
 */

/**
 * Author : Deepal Jayasinghe
 * Date: Jul 19, 2005
 * Time: 10:55:51 AM
 */
public class AxisObserverImpl implements AxisObserver{

    //The initilization code will go here
    public void init() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void update(AxisEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    //there can be parameters for the Observers
    public void addParameter(Parameter parameter) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Parameter getParameter(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
