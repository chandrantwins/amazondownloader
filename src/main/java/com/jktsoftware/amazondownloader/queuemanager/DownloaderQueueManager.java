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
package com.jktsoftware.amazondownloader.queuemanager;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.jktsoftware.amazondownloader.download.interfaces.ICredentials;
import com.jktsoftware.amazondownloader.queuemanager.interfaces.IQueueManager;

/**
 *
 * @author jktdev
 */

public class DownloaderQueueManager implements IQueueManager {
	String requestqueueurl="";
	String receivedqueueurl="";
	String failedqueueurl="";
	ICredentials credentials;
	AmazonSQS sqs=null;
	
	public DownloaderQueueManager(
			ICredentials credentials,
			String receivedqueuename,
			String failedqueuename) {
		this.credentials=credentials;
		AWSCredentials awscredentials = 
				new BasicAWSCredentials(
						this.credentials.getAccessKey(),
						this.credentials.getSecretAccessKey());
		
		sqs = new AmazonSQSClient(awscredentials);
        Region euWest1 = Region.getRegion(Regions.EU_WEST_1);
        sqs.setRegion(euWest1);
        
        System.out.println("Creating amazon download queues.\n");
        
        CreateQueueRequest createReceivedQueueRequest = new CreateQueueRequest(receivedqueuename);
        this.receivedqueueurl = sqs.createQueue(createReceivedQueueRequest).getQueueUrl();
        
        CreateQueueRequest createFailedQueueRequest = new CreateQueueRequest(failedqueuename);
        this.failedqueueurl = sqs.createQueue(createFailedQueueRequest).getQueueUrl();
	}
	
	public boolean sendReceivedObjectMessageToQueue(String objectid) {
		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		JsonObject value = factory.createObjectBuilder()
			    .add("eventtype", "downloaderReceived")
			    .add("objectid", objectid).build();
		
		sqs.sendMessage(
				new SendMessageRequest(this.receivedqueueurl, value.toString()));
		
		return true;
	}

	public boolean sendFailedObjectMessageToQueue(String objectid, String error) {
		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		JsonObject value = factory.createObjectBuilder()
			    .add("eventtype", "downloaderFailed")
			    .add("objectid", objectid)
				.add("error", error).build();
		
		sqs.sendMessage(
				new SendMessageRequest(this.failedqueueurl, value.toString()));
		
		return true;
	}

	public ICredentials getCredentials() {
		return this.credentials;
	}
}
