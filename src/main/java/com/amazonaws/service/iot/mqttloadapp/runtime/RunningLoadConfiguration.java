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
import com.amazonaws.service.iot.mqttloadapp.SystemConfig;
import com.amazonaws.service.iot.mqttloadapp.model.LoadConfig;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author soderlun
 */
public class RunningLoadConfiguration implements MqttListener {

    private static final Logger LOG = Logger.getLogger(RunningLoadConfiguration.class.getName());

    FunctionConfiguration ms = null;
    LoadConfig config;
    LoadGeneratorThread thread;
    long start = 0;

    public RunningLoadConfiguration(LoadConfig cfg) {
        if (cfg == null) {
            throw new NullPointerException("Config should not be null");
        }
        config = cfg;
        thread = new LoadGeneratorThread(config, config.getMetricsSeries());
    }

    public boolean start() {
        LOG.log(Level.INFO, "Starting {0}", config.getId());
        MqttConnection con = MqttConnection.getInstance();
        if (!con.isConnected()) {
            if (con.connect(SystemConfig.getMqttConfigProperties())) {
                LOG.info("Connected successfully");
            } else {
                LOG.warning("Could not connect to MQTT-broker");
                // TBD - Disabled while offline
                // throw new InternalServerErrorException("Could not connect to MQTT-broker");
                return false;
            }
        } else {
            LOG.info("Connection already established, reusing");
        }

        // Start metrics thread
        thread.start();
        start = System.currentTimeMillis();

        // Configure control messaging listener if appropriate
        if (shouldRegisterControlTopic(config.getControlTopic())) {
            String instantiatedTopic = config.getControlTopic().replace("$configid", config.getId());
            con.registerTopicListener(this, instantiatedTopic);
        }

        return true;
    }

    private boolean shouldRegisterControlTopic(String ctrlTopic) {
        return ctrlTopic != null && ctrlTopic.length() > 0;
    }

    public void stop(String cfgId) {
        LOG.log(Level.INFO, "Stopping {0}", config.getId());
        if (thread != null) {
            try {
                thread.shutdown();
                
                MqttConnection.getInstance().unregister(this, config.getControlTopic().replace("$configid", config.getId()));
                
                thread.join(5000);
                LOG.info("Running thread was terminated");
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, "Could not terminate thread", ex);
            }
            thread = null;
        }
    }

    public String getId() {
        return config.getId();
    }

    public long getStart() {
        return start;
    }

    public JsonObject toJsonObject() {
        return Json.createObjectBuilder()
                .add("id", getId())
                .add("start", getStart()).build();
    }

    @Override
    public void notify(MqttMessage message) {
        // We have received a control message
        byte[] msg = message.getPayload();
        try {
            String str = msg != null ? new String(msg, "UTF-8") : "";
            // Only one control message supported, 
            // either in text or in json with single "action" attribute
            if (str.equals("STOP") || isJsonStop(str)) {
                LoadConfigurationRuntimeRegistry.getInstance().stop(config.getId());
            } else {
                LOG.log(Level.INFO, "Incoming message not a supported action: {0}", str);
            }
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, "Could not decode control message: " + Arrays.toString(msg), ex);
        }
    }

    private boolean isJsonStop(String str) {
        JsonReader reader = Json.createReader(new StringReader(str));
        String action = reader.readObject().getString("action", "");
        return action.length() > 0 && action.equals("STOP");
    }
}
