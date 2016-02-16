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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 *
 * @author soderlun
 */
public class LoadConfig {

    public static LoadConfig newInstance(JsonObject obj) {
        return parseJson(obj);
    }

    String id;
    List<FunctionConfiguration> metrics = new ArrayList<>();
    String templateId;
    String topic;
    private int rate;
    String controlTopic;

    public LoadConfig(String id) {
        this.id = id;
    }

    private LoadConfig(String tid, String t, List<FunctionConfiguration> series) {
        templateId = tid;
        id = UUID.randomUUID().toString();
        metrics = Collections.unmodifiableList(series);
        topic = t;
    }

    public String getId() {
        return id;
    }

    public List<FunctionConfiguration> getMetricsSeries() {
        return metrics;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getTopic() {
        return topic;
    }

    public String toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("id", id)
                .add("templateid", getTemplateId())
                .add("topic", getTopic())
                .add("rate", getRate())
                .add("controltopic", controlTopic);

        JsonArrayBuilder functionsBuilder = Json.createArrayBuilder();

        for (FunctionConfiguration ms : metrics) {
            ms.toJson(functionsBuilder);
        }

        builder.add("functions", functionsBuilder);
        return builder.build().toString();
    }

    private static LoadConfig parseJson(JsonObject obj) {

        // Parse LoadConfig attributes
        String topic = obj.getString("topic");
        String templateId = obj.getString("templateid");
        int rate = obj.getInt("rate");
        String id = obj.getString("id", null);
        String ctrlTopic = obj.getString("controltopic", "");

        // Parse Metrics Series
        JsonArray metrics = obj.getJsonArray("functions");
        List<FunctionConfiguration> series = new ArrayList<>();
        if (metrics != null) {
            for (JsonValue metric : metrics) {
                series.add(FunctionConfiguration.parse((JsonObject) metric));
            }
        }

        LoadConfig cfg = new LoadConfig(templateId, topic, series);
        cfg.setRate(rate);
        cfg.setControlTopic(ctrlTopic);
        if (id != null) {
            cfg.setId(id);
        }

        return cfg;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The number of samples to do per minute
     *
     * @return
     */
    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setControlTopic(String ctrlTopic) {
        controlTopic = ctrlTopic;
    }
    public String getControlTopic()
    {
        return controlTopic;
    }
}
