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

import com.amazonaws.service.iot.mqttloadapp.model.Template;
import com.amazonaws.service.iot.mqttloadapp.model.TemplateRepository;
import java.io.FileNotFoundException;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author soderlun
 */
public class TemplateResource {

    private String id;

    /**
     * Creates a new instance of TemplateResource
     */
    private TemplateResource(String id) {
        this.id = id;
    }

    /**
     * Get instance of the TemplateResource
     */
    public static TemplateResource getInstance(String id) {
        // The user may use some kind of persistence mechanism
        // to store and restore instances of TemplateResource class.
        return new TemplateResource(id);
    }

    /**
     * Retrieves representation of an instance of
     * com.amazonaws.soderlun.iot.mqttloadapp.rest.TemplateResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson() {
        Template t = TemplateRepository.getInstance().getTemplate(id);

        return t.toJson();
    }


    /**
     * DELETE method for resource TemplateResource
     */
    @DELETE
    public Response delete() {
        try {
            TemplateRepository.getInstance().delete(id);
        } catch (FileNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception ex) {
            return Response.serverError().build();
        }
        return Response.ok().build();
    }
}
