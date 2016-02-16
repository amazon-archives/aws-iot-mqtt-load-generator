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
package com.amazonaws.service.iot.mqttloadapp.runtime;

import com.amazonaws.service.iot.mqttloadapp.model.FunctionConfiguration;
import com.amazonaws.service.iot.mqttloadapp.model.Function;
import com.amazonaws.service.iot.mqttloadapp.model.TemplateRepository;
import com.amazonaws.service.iot.mqttloadapp.model.Template;
import com.amazonaws.service.iot.mqttloadapp.model.LoadConfig;
import com.google.common.util.concurrent.RateLimiter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 * @author soderlun
 */
public class LoadGeneratorThread extends Thread {

    private static final Logger LOG = Logger.getLogger(LoadGeneratorThread.class.getName());
    private static final int MIN_TICK_TIME_MS = 10;

    private boolean running = false;
    private List<FunctionConfiguration> functions = new ArrayList<>();
    private final LoadConfig config;
    private long started;
    private int numberTicks = 0;

    public LoadGeneratorThread(LoadConfig cfg, List<FunctionConfiguration> ms) {
        functions = ms;
        config = cfg;
    }

    @Override
    public void run() {
        MqttConnection connection = MqttConnection.getInstance();
        running = true;

        started = System.currentTimeMillis();

        // TODO simplification with only one metrics series supported
        int ratePerMinute = config.getRate();

        RateLimiter limiter = com.google.common.util.concurrent.RateLimiter.create(ratePerMinute / (double) 60.0);

        while (running && connection.isConnected()) {
            long start = System.currentTimeMillis();
            numberTicks++;

            // Wait for the configured amount of time to maintain the rate
            limiter.acquire();

            // int numberOfPayloads = calculatePayloadsPerTick(ratePerMinute, timeForLastTick, MIN_TICK_TIME_MS);
            byte[] payload = formatPayload(config.getTemplateId(), functions);

            try {
                connection.publish(config.getTopic(), 0, payload, config.getId());
            } catch (MqttException ex) {
                LOG.log(Level.SEVERE, "Could not publish the MQTT-message", ex);
            }

            long timeForLastTick = System.currentTimeMillis() - start;
            LOG.log(Level.INFO, "Took {0}ms to execute tick #: {1}", new Object[]{timeForLastTick, numberTicks});
        }
        
        LOG.info("Stopped metrics thread");
    }

    public void shutdown() {
        running = false;
        started = -1;
        this.interrupt();
    }

    private byte[] formatPayload(String templateId, List<FunctionConfiguration> metricsseries) {

        long elapsedTime = System.currentTimeMillis() - started;

        Map<String, String> variableValues = new HashMap<>();
        for (FunctionConfiguration ms : metricsseries) {
            Function func = ms.getFunction();

            StringBuilder builder = new StringBuilder();
            func.value(builder, elapsedTime, numberTicks);
            String value = builder.toString();

            variableValues.put(func.getVariable(), value);
        }

        variableValues.put("timestamp", Long.toString(System.currentTimeMillis()));

        variableValues.put("elapsedtime", Long.toString(elapsedTime));

        variableValues.put("tick", Integer.toString(numberTicks));
        
        variableValues.put("seriesid", config.getId());
        
        variableValues.put("configid", config.getId());
        
        variableValues.put("clientid", MqttConnection.getInstance().getClientId());

        Template template = TemplateRepository.getInstance().getTemplate(templateId);
        String content = template.getContent();

        String payload = instantiateTemplate(variableValues, content);

        try {
            LOG.log(Level.INFO, "Instantiated template: {0}", payload);
            return payload.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, "Could not UTF-8 format payload", ex);
            throw new RuntimeException("Could not UTF-8 format payload");
        }
    }


    private String instantiateTemplate(Map<String, String> variableValues, String content) {
        
        String template = content;
        for(String variable : variableValues.keySet()) {
           template = template.replace("$" + variable, variableValues.get(variable));
        }        
        return template;
    }

}
