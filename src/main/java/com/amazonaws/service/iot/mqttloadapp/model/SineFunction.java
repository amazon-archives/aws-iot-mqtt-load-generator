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

import java.util.Properties;

/**
 *
 * @author soderlun
 */
public class SineFunction extends Function {

    int cycleTimeSeconds = 60;
    int magnitude = 1;
    private final int scaleFactorElapsed;

    SineFunction(FunctionType type, String variable, Properties props) {
        super(type, variable, props);

        cycleTimeSeconds = Integer.parseInt(props.getProperty("cycleTime", "60"));
        magnitude = Integer.parseInt(props.getProperty("magnitude", "1"));
        scaleFactorElapsed = Integer.parseInt(props.getProperty("elapsedscalefactor", "1"));
    }

    @Override
    public void value(StringBuilder buff, long millisElapsed, int ticks) {
        int period = cycleTimeSeconds * 1000;

        long scaledElapsed = millisElapsed / scaleFactorElapsed;

        double value = Math.sin((double) (scaledElapsed % period) / (double) period);

        double scaledValue = value * magnitude;

        buff.append(scaledValue);
    }
}
