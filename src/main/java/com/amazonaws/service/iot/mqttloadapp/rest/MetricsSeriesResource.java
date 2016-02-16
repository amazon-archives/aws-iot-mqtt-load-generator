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

import com.amazonaws.service.iot.mqttloadapp.model.LoadConfig;
import com.amazonaws.service.iot.mqttloadapp.model.LoadConfigsRegistry;
import com.amazonaws.service.iot.mqttloadapp.model.FunctionConfiguration;
import com.amazonaws.service.iot.mqttloadapp.runtime.LoadConfigurationRuntimeRegistry;
import com.amazonaws.service.iot.mqttloadapp.runtime.RunningLoadConfiguration;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;

/**
 * REST Web Service
 *
 * @author soderlun
 */
public class MetricsSeriesResource {

    private String id;
    private FunctionConfiguration metricsSeries;
    private boolean running = false;

    /**
     * Creates a new instance of MetricsSeriesResource
     */
    private MetricsSeriesResource(String id) {
        this.id = id;
        LoadConfig cfg = LoadConfigsRegistry.getConfig(id);
        if (cfg.getMetricsSeries().size() > 0) {
            metricsSeries = cfg.getMetricsSeries().get(0);
        } else {
            throw new RuntimeException("No metrics series defined");
        }
    }

    /**
     * Get instance of the MetricsSeriesResource
     *
     * @param id
     * @return
     */
    public static MetricsSeriesResource getInstance(String id) {
        // The user may use some kind of persistence mechanism
        // to store and restore instances of MetricsSeriesResource class.
        return new MetricsSeriesResource(id);
    }

    /**
     * Retrieves representation of an instance of
     * com.amazonaws.soderlun.iot.mqttloadapp.MetricsSeriesResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson() {
        RunningLoadConfiguration rms = LoadConfigurationRuntimeRegistry.getInstance().get(id);

        if (rms != null) {
            return rms.toJsonObject().toString();
        }
        else {
            throw new NotFoundException();
        }
    }

    /**
     * PUT method for updating or creating an instance of MetricsSeriesResource
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/json")
    public void putJson(String content) {
    }

    /**
     * DELETE method for resource MetricsSeriesResource
     */
    @DELETE
    public void delete() {
        stop();
    }

    private void stop() {
        LoadConfigurationRuntimeRegistry.getInstance().stop(id);
        running = false;
    }
}
