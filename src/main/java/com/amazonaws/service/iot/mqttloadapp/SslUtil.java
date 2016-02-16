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

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.service.iot.mqttloadapp.SystemConfig.S3Info;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.*;

import org.bouncycastle.jce.provider.*;
import org.bouncycastle.openssl.*;

public class SslUtil {

    public static SSLSocketFactory getSocketFactory(final String caCrtFile, final String crtFile, final String keyFile,
            final String password) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        PEMReader reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(getBytesFromFile(caCrtFile))));
        X509Certificate caCert = (X509Certificate) reader.readObject();
        reader.close();

        // load client certificate
        reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(getBytesFromFile(crtFile))));
        X509Certificate cert = (X509Certificate) reader.readObject();
        reader.close();

        // load client private key
        reader = new PEMReader(
                new InputStreamReader(new ByteArrayInputStream(getBytesFromFile(keyFile))),
                new PasswordFinder() {
                    @Override
                    public char[] getPassword() {
                        return password.toCharArray();
                    }
                }
        );
        KeyPair key = (KeyPair) reader.readObject();
        reader.close();

        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        // client key and certificates are sent to server so it can authenticate us
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{cert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance(System.getProperty("tlsversion", "TLSv1.2"));
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }

    private static byte[] getBytesFromFile(String file) {
        if (SystemConfig.isS3(file)) {
            AmazonS3Client api = new AmazonS3Client(SystemConfig.getCredentials());

            S3Info info = SystemConfig.getS3Info(file);

            S3Object obj = api.getObject(info.bucket, info.prefixPath);

            byte[] buff = new byte[10 * 1000];
            try {
                int read = obj.getObjectContent().read(buff);
                if (read > 0) {
                    return Arrays.copyOfRange(buff, 0, read);
                }
            } catch (IOException ex) {
                Logger.getLogger(SslUtil.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException("Could not read file: " + file);
            }
        } else if (SystemConfig.isLocalFileSystem(file)) {
            try {
                return Files.readAllBytes(Paths.get(file));
            } catch (IOException ex) {
                Logger.getLogger(SslUtil.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException("Could not read certificate from filesystem: " + file);
            }
        }

        throw new RuntimeException("Not a supported format of file: " + file);
    }
}
