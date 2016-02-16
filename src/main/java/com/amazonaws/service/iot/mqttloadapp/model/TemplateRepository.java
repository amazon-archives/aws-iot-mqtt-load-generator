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

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.service.iot.mqttloadapp.SystemConfig;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author soderlun
 */
public class TemplateRepository {

    private static final Logger LOG = Logger.getLogger(TemplateRepository.class.getName());

    private static final long ONE_MINUTE_IN_MS = 1 * 60 * 1000;

    private static TemplateRepository instance;

    public static final TemplateRepository getInstance() {
        if( instance == null ){
            instance = new TemplateRepository();
        }
        return instance;
    }

    private Map<String, Template> cachedTemplates = new HashMap<>();
    private final Timer timer;

    TemplateRepository() {
        cacheTemplates();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                cacheTemplates();
            }
        }, new Date(), ONE_MINUTE_IN_MS);
    }

    public List<Template> getTemplates() {
        String root = SystemConfig.getTemplateRoot();
        List<Template> result = new ArrayList<>();

        if (isLocalFilesystem(root)) {
            File templateRoot = new File(root);

            File[] templates = templateRoot.listFiles();

            for (File t : templates) {
                Template temp = Template.newInstance(t);
                if (temp != null) {
                    result.add(temp);
                }
            }
            return result;
        } else if (SystemConfig.isS3(root)) {
            AmazonS3Client api = new AmazonS3Client(SystemConfig.getCredentials());
            SystemConfig.S3Info info = SystemConfig.getS3Info(SystemConfig.getTemplateRoot());

            ListObjectsRequest req = new ListObjectsRequest();
            req.setBucketName(info.bucket);
            req.setPrefix(info.prefixPath);

            ObjectListing resp = api.listObjects(req);
            List<S3ObjectSummary> configObjects = resp.getObjectSummaries();

            for (S3ObjectSummary c : configObjects) {
                if (c.getKey().endsWith(".json")) {

                    S3Object conf = api.getObject(info.bucket, c.getKey());
                    S3ObjectInputStream ois = conf.getObjectContent();

                    Pattern p = Pattern.compile(".*\\/(.*).json");
                    Matcher m = p.matcher(conf.getKey());
                    if (m.matches()) {
                        String name = m.group(1);
                        Template t = Template.newInstance(ois, name, Template.TemplateFormat.JSON);
                        result.add(t);
                    } else {
                        throw new RuntimeException("Could not extract name from key");
                    }
                } else if (c.getKey().endsWith(".csv")) {
                    S3Object conf = api.getObject(info.bucket, c.getKey());
                    S3ObjectInputStream ois = conf.getObjectContent();

                    Pattern p = Pattern.compile(".*\\/(.*).csv");
                    Matcher m = p.matcher(conf.getKey());
                    if (m.matches()) {
                        String name = m.group(1);
                        Template t = Template.newInstance(ois, name, Template.TemplateFormat.CSV);
                        result.add(t);
                    } else {
                        throw new RuntimeException("Could not extract name from key");
                    }
                } else {
                    LOG.info("Unknown S3-object key: " + c.getKey());
                }
            }
        }
        
        cachedTemplates.clear();
        for(Template t : result) {
            cachedTemplates.put(t.getName(), t);
        }

        return result;
    }

    private boolean isLocalFilesystem(String root) {
        File f = new File(root);
        return f.isAbsolute();
    }


    public Template getTemplate(String templateId) {
        return cachedTemplates.get(templateId);
    }

    private void cacheTemplates() {
        List<Template> templates = getTemplates();
        cachedTemplates.clear();
        for (Template t : templates) {
            cachedTemplates.put(t.getName(), t);
        }
    }

    public void delete(String id) throws FileNotFoundException {
        String templateRoot = SystemConfig.getTemplateRoot();
        if (isLocalFilesystem(templateRoot)) {
            File f = new File(templateRoot, id + ".json");
            if (f.exists()) {
                f.delete();
                return;
            }

            f = new File(templateRoot, id + ".csv");
            if (f.exists()) {
                f.delete();
            } else {
                throw new FileNotFoundException("Non existing template id");
            }
        } else if (SystemConfig.isS3(templateRoot)) {
            // TODO
        } else {
            throw new RuntimeException("Template configuration error, no valid template root");
        }
    }

    public void putTemplate(Template t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
