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
package com.amazonaws.service.iot.mqttloadapp;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author soderlun
 */
public class SystemConfig {

    private static final Logger LOG = Logger.getLogger(SystemConfig.class.getName());

    
    public static AWSCredentials getCredentials() {
        return new MyCredentialsProvider().getCredentials();
    }

    public static String getTemplateRoot() {
        return System.getProperty("template_root", System.getenv("template_root") != null ? System.getenv("template_root") : getConfigRoot() + "/template_root");
    }

    public static String getConfigRoot() {
        String ret = System.getProperty("config_root", System.getenv("config_root"));
        return ret;
    }

    public static String getLoadConfigRoot() {
        return System.getProperty("loadconfig_root", System.getenv("loadconfig_root") != null ? System.getenv("loadconfig_root") : getConfigRoot() + "/loadconfig_root");
    }

    public static Properties getMqttConfigProperties() {
        Properties props = new Properties();

        String root = getConfigRoot();
        if (isS3(root)) {
            S3Info inf = getS3Info(SystemConfig.getConfigRoot());

            AmazonS3Client api = new AmazonS3Client(getCredentials());
            S3Object prop = api.getObject(inf.bucket, inf.prefixPath + "/mqtt.properties");
            try (InputStream fis = prop.getObjectContent()) {
                props.load(fis);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Could not open mqtt.properties", ex);
            }
        } else {
            File f = new File(root, "mqtt.properties");
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    props.load(fis);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Could not open mqtt.properties", ex);
                }
            } else {
                LOG.warning("Could not load config.properties");
            }
        }
        return props;
    }

    public static boolean isS3(String path) {
        return path.startsWith("s3://");
    }

    static boolean isLocalFileSystem(String file) {
        return new File(file).isDirectory() || new File(file).isFile();
    }

    public static class S3Info {

        public String bucket;
        public String prefixPath;
    }

    public static S3Info getS3Info(String root) {
        Pattern p = Pattern.compile("s3:\\/\\/([\\w\\.]*)\\/(.*)");
        Matcher m = p.matcher(root);

        S3Info inf = new S3Info();

        if (m.matches()) {
            inf.bucket = m.group(1);
            inf.prefixPath = m.group(2);
            return inf;
        } else {
            LOG.log(Level.SEVERE, "Illegal format of s3 root config {0}", root);
            throw new RuntimeException("Illegal format of s3 root config: " + root);
        }
    }
}
