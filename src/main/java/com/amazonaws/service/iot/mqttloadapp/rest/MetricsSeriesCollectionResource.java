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
package com.amazonaws.service.iot.mqttloadapp.rest;

import com.amazonaws.service.iot.mqttloadapp.runtime.LoadConfigurationRuntimeRegistry;
import com.amazonaws.service.iot.mqttloadapp.runtime.RunningLoadConfiguration;
import java.io.StringWriter;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonWriter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

/**
 * REST Web Service
 *
 * @author soderlun
 */
@Path("/series")
public class MetricsSeriesCollectionResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of MetricsSeriesCollectionResource
     */
    public MetricsSeriesCollectionResource() {
    }

    /**
     * Retrieves representation of an instance of com.amazonaws.soderlun.iot.mqttloadapp.MetricsSeriesCollectionResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson() {
        List<RunningLoadConfiguration> configs = LoadConfigurationRuntimeRegistry.getInstance().getAllRunning();

        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (RunningLoadConfiguration rms : configs) {
            builder.add(rms.toJsonObject());
        }
        
        StringWriter writer = new StringWriter();

        try (JsonWriter jw = Json.createWriter(writer)) {
            jw.writeArray(builder.build());
        }

        return writer.toString();        
    }



    /**
     * Sub-resource locator method for {id}
     */
    @Path("{id}")
    public MetricsSeriesResource getMetricsSeriesResource(@PathParam("id") String id) {
        return MetricsSeriesResource.getInstance(id);
    }
}
