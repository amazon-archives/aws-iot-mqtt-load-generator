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

import com.amazonaws.service.iot.mqttloadapp.model.RandomFunction;
import com.amazonaws.service.iot.mqttloadapp.model.SineFunction;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 *
 * @author soderlun
 */
public abstract class Function {

    public static Function newInstance(FunctionType type, String var, JsonArray params) {

        Properties props = new Properties();
        if (params != null) {

            Iterator<JsonValue> iter = params.iterator();
            while (iter.hasNext()) {
                JsonValue val = iter.next();
                JsonObject p = (JsonObject) val;
                Set<Entry<String, JsonValue>> keys = p.entrySet();

                for (Entry<String, JsonValue> ev : keys) {
                    switch (ev.getValue().getValueType()) {
                        case NUMBER:
                            props.setProperty(ev.getKey(), Integer.toString(((JsonNumber) ev.getValue()).intValue()));
                            break;
                        case STRING:
                            props.setProperty(ev.getKey(), ((JsonString) ev.getValue()).getString());
                            break;
                        default:
                            throw new RuntimeException("Unknown parameter type");
                    }
                }
            }
        }

        switch (type) {
            case SINE:
                return new SineFunction(type, var, props);
            case RANDOM:
                return new RandomFunction(type, var, props);
            case EXPR:
                return new ExpressionFunction(type, var, props);
            case TEXT:
                return new TextFunction(type, var, props);
            default:
                throw new RuntimeException("Unsupported function type: " + type.name());
        }
    }

    FunctionType type;
    Properties parameters;
    String variable;

    protected Function(FunctionType t, String v, Properties props) {
        type = t;
        parameters = props;
        variable = v;
    }

    public abstract void value(StringBuilder buff, long millisElapsed, int ticks);

    public FunctionType getFunctionType() {
        return type;
    }

    public String getVariable() {
        return variable;
    }

    public void fillParameters(JsonArrayBuilder paramsBuilder) {
        Enumeration<String> names = (Enumeration<String>) parameters.propertyNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            paramsBuilder.add(Json.createObjectBuilder()
                    .add(name, parameters.getProperty(name)));
        }
    }
}
