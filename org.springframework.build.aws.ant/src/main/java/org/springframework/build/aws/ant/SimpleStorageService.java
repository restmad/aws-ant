/*
 * Copyright 2010 SpringSource
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.build.aws.ant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3Service;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;

/**
 * An ANT task for dealing with the Amazon S3 service. Requires properties to be set for an <code>accessKey</code> and a
 * <code>secretKey</code>. S3 operations are listed as elements contained in the s3 tag.
 * 
 * <pre>
 * &lt;aws:s3 accessKey=&quot;${s3.accessKey}&quot; secretKey=&quot;${s3.secretKey}&quot;&gt;
 *         &lt;upload bucketName=&quot;static.springframework.org&quot;
 *                 file=&quot;${target.release.dir}/${release-with-dependencies.zip}&quot;
 *                 toFile=&quot;SPR/spring-framework-${spring-version}-with-dependencies-${tstamp}-${build.number}.zip&quot;
 *                 publicRead=&quot;true&quot;/&gt;
 *         &lt;upload bucketName=&quot;static.springframework.org&quot;
 *                 file=&quot;${target.release.dir}/${release.zip}&quot;
 *                 toFile=&quot;SPR/spring-framework-${spring-version}-${tstamp}-${build.number}.zip&quot;
 *                 publicRead=&quot;true&quot;/&gt;
 * &lt;/aws:s3&gt;
 * </pre>
 * 
 * @author Ben Hale
 */
public class SimpleStorageService {

    private String accessKey;

    private String secretKey;

    private Project project;

    private final List<S3Operation> operations = new ArrayList<S3Operation>();

    /**
     * Required parameter that corresponds to the S3 Access Key
     * 
     * @param accessKey The S3 Access Key
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * Required parameter that corresponds to the S3 Secret Key
     * 
     * @param secretKey The S3 Secret Key
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Infrastructure element
     * 
     * @param project The project this task is running in
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Add any upload operations
     * 
     * @param upload The upload operation metadata
     */
    public void addConfiguredUpload(Upload upload) {
        this.operations.add(upload);
    }

    /**
     * Add any download operations
     * 
     * @param download The download operation metadata
     */
    public void addConfiguredDownload(Download download) {
        this.operations.add(download);
    }

    /**
     * Add any downloadLatest operations
     * 
     * @param download The downloadLatest operation metadata
     */
    public void addConfiguredDownloadLatest(DownloadLatest downloadLatest) {
        this.operations.add(downloadLatest);
    }

    /**
     * Add any delete operations
     * 
     * @param delete The delete operation metadata
     */
    public void addConfiguredDelete(Delete delete) {
        this.operations.add(delete);
    }

    /**
     * Run all S3 operations configured as part of this task
     */
    public void execute() {
        try {
            AWSCredentials credentials = new AWSCredentials(this.accessKey, this.secretKey);
            S3Service service = new RestS3Service(credentials, "ants3task", null, getJetS3tProperties());

            for (S3Operation operation : this.operations) {
                operation.execute(service);
            }
        } catch (ServiceException e) {
            throw new BuildException(e);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Jets3tProperties getJetS3tProperties() {
        Properties p = new Properties();
        p.putAll(this.project.getProperties());

        Jets3tProperties jets3tProperties = new Jets3tProperties();
        jets3tProperties.loadAndReplaceProperties(p, "ANT Properties");

        return jets3tProperties;
    }

}
