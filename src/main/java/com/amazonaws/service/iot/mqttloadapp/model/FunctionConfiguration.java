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
package com.amazonaws.service.iot.mqttloadapp.model;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

/**
 *
 * @author soderlun
 */
public class FunctionConfiguration {

    static FunctionConfiguration parse(JsonObject metric) {
        String func = metric.getString("function");
        String funcvar = metric.getString("variable");
        JsonArray params = metric.getJsonArray("parameters");

        FunctionType ft = FunctionType.valueOf(func);
        Function f = Function.newInstance(ft, funcvar, params);
        
        FunctionConfiguration ms = new FunctionConfiguration(f);
        return ms;       
    }

    private Function function;

    public FunctionConfiguration(Function f) {
        function = f;

    }

    public Function getFunction() {
        return function;
    }

    void toJson(JsonArrayBuilder metricsBuilder) {
        JsonArrayBuilder paramsBuilder = Json.createArrayBuilder();
        getFunction().fillParameters(paramsBuilder);
        metricsBuilder
                .add(Json.createObjectBuilder()
                        .add("function", getFunction().getFunctionType().name())
                        .add("variable", getFunction().getVariable())
                        .add("parameters", paramsBuilder));
    }

}
