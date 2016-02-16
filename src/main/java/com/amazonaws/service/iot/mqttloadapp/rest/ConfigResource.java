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

import com.amazonaws.service.iot.mqttloadapp.model.LoadConfigsRegistry;
import com.amazonaws.service.iot.mqttloadapp.model.FunctionConfiguration;
import com.amazonaws.service.iot.mqttloadapp.runtime.LoadConfigurationRuntimeRegistry;
import com.amazonaws.service.iot.mqttloadapp.model.LoadConfig;
import com.amazonaws.service.iot.mqttloadapp.model.LoadConfigException;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * REST Web Service
 *
 * @author soderlun
 */
public class ConfigResource {

    private static final Logger LOG = Logger.getLogger(ConfigResource.class.getName());

    static ConfigResource getInstance(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private UriInfo context;

    private String id;

    /**
     * Creates a new instance of ConfigResource
     */
    private ConfigResource(String id, UriInfo ctx) {
        this.id = id;
        context = ctx;
    }

    /**
     * Get instance of the ConfigResource
     */
    public static ConfigResource getInstance(String id, UriInfo context) {
        // The user may use some kind of persistence mechanism
        // to store and restore instances of ConfigResource class.
        return new ConfigResource(id, context);
    }

    /**
     * Retrieves representation of an instance of
     * com.amazonaws.soderlun.iot.mqttloadapp.ConfigResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getRepresentation() {

        LoadConfig cfg = LoadConfigsRegistry.getConfig(id);

        if (cfg == null) {
            throw new NotFoundException();
        }

        return cfg.toJson();
    }

    /**
     * PUT method for updating or creating an instance of ConfigResource
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/json")
    public void updateConfig(String c) {
        JsonObject content = Json.createReader(new StringReader(c))
                .readObject();

        LoadConfig cfg = LoadConfig.newInstance(content);
        cfg.setId(id);
        LoadConfigsRegistry.updateConfig(id, cfg);
//        Response resp = Response.created(context.getAbsolutePathBuilder().path(cfg.getId()).build()) .build();              
//        return resp;        
    }

    @POST
    @Consumes("application/json")
    public Response startRun(String content, @Context UriInfo uriInfo) {
        JsonReader reader = Json.createReader(new StringReader(content));
        JsonObject obj = reader.readObject();

        LoadConfig cfg = LoadConfigsRegistry.getConfig(id);

        List<FunctionConfiguration> ms = cfg.getMetricsSeries();
        if (ms.size() > 0) {

            try {
                LoadConfigurationRuntimeRegistry rt = LoadConfigurationRuntimeRegistry.getInstance();
                rt.start(cfg.getId(), obj);
                UriBuilder builder = UriBuilder.fromResource(MetricsSeriesCollectionResource.class);

                UriBuilder baseBuild = uriInfo.getBaseUriBuilder();

                // return Response.created( context.getAbsolutePathBuilder().path(cfg.getId()).build()).build();
                return Response.created(baseBuild.path(builder.build().toString()).path(cfg.getId()).build()).build();

            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "could not start", ex);
                return Response.serverError().build();
            }
        } else {
            LOG.severe("No metrics series defined for config " + cfg.getId());
            return Response.serverError().build();
        }
    }

    /**
     * DELETE method for resource ConfigResource
     */
    @DELETE
    public void delete() {
        try {
            LoadConfigsRegistry.deleteConfig(id);
        } catch (LoadConfigException ex) {
            Logger.getLogger(ConfigResource.class.getName()).log(Level.SEVERE, "Could not find config id" + id, ex);
            throw new javax.ws.rs.NotFoundException();
        }
    }
}
