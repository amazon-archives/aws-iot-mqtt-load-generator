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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author soderlun
 */
public class Template {

    private static Logger LOG = Logger.getLogger(Template.class.getName());

    static Template newInstance(File f) {
        Template t = new Template();
        try {
            if (f.getName().endsWith(".json")) {
                String name = f.getName().substring(0, f.getName().indexOf(".json"));
                return newInstance(new FileInputStream(f), name, TemplateFormat.JSON);
            } else if (f.getName().endsWith(".csv")) {
                String name = f.getName().substring(0, f.getName().indexOf(".csv"));
                return newInstance(new FileInputStream(f), name, TemplateFormat.CSV);
            }
        } catch (FileNotFoundException ex) {
            LOG.log(Level.SEVERE, "Could not find file " + f.getAbsolutePath(), ex);
            throw new RuntimeException("Could not find file");
        }

        return t;
    }

    static Template newInstance(InputStream is, String name, TemplateFormat tf) {
        Template t = new Template();
        String cont = readFileBuffer(is);
        t.setContent(cont);
        t.setFormat(tf);
        t.setName(name);
        return t;
    }

    public static Template newInstance(JsonObject obj) {
        String name = obj.getString("name");
        String content = obj.getString("content");
        String id = obj.getString("id");

        Template t = new Template();
        t.setContent(content);
        t.setName(name);
        t.setFormat(TemplateFormat.JSON);
        return t;
    }

    private static String readFileBuffer(InputStream is) {
        try {
            InputStreamReader bsr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(bsr);
            StringBuilder buff = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                buff.append(line);
            }
            return buff.toString();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Template.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Template.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public enum TemplateFormat {

        JSON, CSV
    };

    private String name;
    private String content;
    private TemplateFormat format;

    public String getName() {
        return name;
    }

    public TemplateFormat getFormat() {
        return format;
    }

    public void setFormat(TemplateFormat format) {
        this.format = format;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("id", getName())
                .add("name", getName())
                .add("content", getContent());
        return builder.build().toString();
    }

}
