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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.jktsoftware.amazondownloader.download.interfaces.ICredentials;
import com.jktsoftware.amazondownloader.download.interfaces.IObject;
import com.jktsoftware.amazondownloader.download.interfaces.IObjectRepo;

/**
 *
 * @author jktdev
 */

import java.util.ArrayList;
import java.util.List;

public class S3TypeBucket implements IObjectRepo {
	ICredentials credentials;
	String repoid;
	String endpoint;
	
	public S3TypeBucket(
			ICredentials credentials, 
			String repoid,
			String endpoint) {
		this.credentials = credentials;
		this.repoid = repoid;
		this.endpoint = endpoint;
	}
	
	public ICredentials getCredentials() {
		return this.credentials;
	}

	public void setRepoId(String repoid) {
		this.repoid = repoid;
	}
	public String getRepoId() {
		return this.repoid;
	}

	public List<IObject> getObjectsInRepo() {
		String repoid = getRepoId();
		AWSCredentials awscredentials = 
				new BasicAWSCredentials(
						this.credentials.getAccessKey(),
						this.credentials.getSecretAccessKey());

		AmazonS3 s3 = new AmazonS3Client(awscredentials);
        s3.setEndpoint(endpoint);
        
        System.out.println("Getting objects");
        ObjectListing objectListing = 
        		s3.listObjects(repoid);
        
        List<IObject> objects = new ArrayList<IObject>();
        
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            S3TypeObject obj = new S3TypeObject(
            		objectSummary.getKey(),
            		objectSummary.getSize(),
            		objectSummary.getBucketName(),
            		objectSummary.getStorageClass(),
            		s3);
            objects.add(obj);
        }
		return objects;
	}

}
