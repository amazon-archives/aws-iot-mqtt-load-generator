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
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 *
 * @author soderlun
 */
public class ExpressionFunction extends Function {

    ExpressionBuilder expressionTemplate;
    private final int scaleFactorElapsed;
    private final int scaleFactorTick;
    private final int tickOffset;
    private final int elapsedOffset;

    public ExpressionFunction(FunctionType t, String v, Properties props) {
        super(t, v, props);

        expressionTemplate = new ExpressionBuilder(props.getProperty("expression", "tick"))
                .function(new net.objecthunter.exp4j.function.Function("max", 2) {
                    @Override
                    public double apply(double... args) {
                        return Math.max(args[0], args[1]);
                    }
                })
                .function(new net.objecthunter.exp4j.function.Function("min", 2) {
                    @Override
                    public double apply(double... args) {
                        return Math.min(args[0], args[1]);
                    }
                })              
                .function(new net.objecthunter.exp4j.function.Function("random") {
                    @Override
                    public double apply(double... args) {
                        return Math.random();
                    }
                })
                .variables("tick", "elapsed");
        scaleFactorElapsed = Integer.parseInt(props.getProperty("elapsedscalefactor", "1"));
        scaleFactorTick = Integer.parseInt(props.getProperty("tickscalefactor", "1"));
        tickOffset = Integer.parseInt(props.getProperty("tickoffset", "0"));
        elapsedOffset = Integer.parseInt(props.getProperty("elapsedoffset", "0"));
    }

    @Override
    public void value(StringBuilder buff, long millisElapsed, int ticks) {
        Expression expr = expressionTemplate.build();

        int adjustedTicks = ticks + tickOffset;
        double scaledTick = adjustedTicks / (double) scaleFactorTick;
        expr.setVariable("tick", scaledTick);

        long adjustedElapsed = millisElapsed + elapsedOffset;
        double scaledElapsed = adjustedElapsed / (double) scaleFactorElapsed;

        expr.setVariable("elapsed", scaledElapsed);

        buff.append(expr.evaluate());
    }

}
