/*
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.service.iot.mqttloadapp;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author soderlun
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.amazonaws.service.iot.mqttloadapp.rest.ConfigResource.class);
        resources.add(com.amazonaws.service.iot.mqttloadapp.rest.ConfigsResource.class);
        resources.add(com.amazonaws.service.iot.mqttloadapp.rest.MetricsSeriesCollectionResource.class);
        resources.add(com.amazonaws.service.iot.mqttloadapp.rest.MetricsSeriesResource.class);
        resources.add(com.amazonaws.service.iot.mqttloadapp.rest.TemplateResource.class);
        resources.add(com.amazonaws.service.iot.mqttloadapp.rest.TemplatesResource.class);
    }
    
}
