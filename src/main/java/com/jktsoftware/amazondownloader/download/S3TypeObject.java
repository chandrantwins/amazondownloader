/*
 The MIT License (MIT)

 Copyright (c) 2015 JKTSoftware

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
package com.jktsoftware.amazondownloader.download;

import java.io.InputStream;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.jktsoftware.amazondownloader.download.interfaces.IObject;

/**
 *
 * @author jktdev
 */
public class S3TypeObject implements IObject {

    String key;
    long size;
    String storageclass;
    String contenttype;
    String repoid;
    AmazonS3 s3;

    public S3TypeObject() {

    }

    public S3TypeObject(
            String key,
            long size,
            String repoid,
            String storageclass,
            AmazonS3 s3) {
        this.key = key;
        this.size = size;
        this.storageclass = storageclass;
        this.repoid = repoid;
        this.s3 = s3;
    }

    public void setObjectKey(String key) {
        this.key = key;
    }

    public String getObjectKey() {
        return this.key;
    }

    public void setObjectSize(long size) {
        this.size = size;
    }

    public long getObjectSize() {
        return this.size;
    }

    public void setStorageClass(String storageclass) {
        this.storageclass = storageclass;
    }

    public String getStorageClass() {
        return this.storageclass;
    }

    public void setContentType(String contenttype) {
        this.contenttype = contenttype;
    }

    public String getContentType() {
        return this.contenttype;
    }

    public void setRepoId(String repoid) {
        this.repoid = repoid;
    }

    public String getRepoId() {
        return repoid;
    }

    public InputStream getObjectContent() {
        String repoid = getRepoId();
        System.out.println("Attempting to get object content for :"
                + getObjectKey());
        System.out.println(getRepoId());
        System.out.println(getObjectKey());

        S3Object object = s3.getObject(getRepoId(), getObjectKey());
        return (InputStream) object.getObjectContent();
    }
}
