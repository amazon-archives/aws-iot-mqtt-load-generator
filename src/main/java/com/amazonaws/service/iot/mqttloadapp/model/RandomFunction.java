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
import java.util.Random;

/**
 *
 * @author soderlun
 */
public class RandomFunction extends Function {

    private enum RandomType {

        INT, FLOAT
    };
    RandomType valueType;
    int maxvalue;
    private final int minvalue;
    Random rnd = new Random();

    RandomFunction(FunctionType type, String variable, Properties props) {
        super(type, variable, props);

        valueType = RandomType.valueOf(props.getProperty("type", "INT"));

        maxvalue = Integer.parseInt(props.getProperty("maxvalue", "100"));
        minvalue = Integer.parseInt(props.getProperty("minvalue", "0"));
    }

    @Override
    public void value(StringBuilder buff, long millisElapsed, int ticks) {
        switch (valueType) {
            case INT:
                buff.append(rnd.nextInt(maxvalue - minvalue) + minvalue);
                break;
            case FLOAT:
                buff.append((double) (rnd.nextDouble() * (maxvalue - minvalue)) + minvalue);
            default:
                throw new RuntimeException("Unknown type");
        }
    }
}
