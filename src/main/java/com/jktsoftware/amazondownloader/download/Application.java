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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jktsoftware.amazondownloader.download.interfaces.IObject;
import com.jktsoftware.amazondownloader.download.interfaces.IObjectRepo;
import com.jktsoftware.amazondownloader.queuemanager.interfaces.IQueueManager;
import com.jktsoftware.amazondownloader.console.ConsoleProgressBar;
import org.apache.commons.cli.Option;

public class Application {

	public static void main(String[] args) {
		processQueue(args);
	}
	
	public static void processQueue(String[] args) {
		Boolean listobjects=false;
		Boolean startdownloads=false;
		String objectidtodownload="";
		long waittime=0;
		
		ApplicationContext context = 
		          new ClassPathXmlApplicationContext(
		        		  new String[] {"spring.xml"});
		IObjectRepo repo = 
				(IObjectRepo)context.getBean("s3bucket");
		System.out.println(repo.getCredentials().getAccessKey());
		
		IQueueManager queuemanager = 
				(IQueueManager)context.getBean("queuemanager");
		System.out.println(queuemanager.getCredentials().getAccessKey());
		
		Options options = addOptions();
		
		CommandLineParser parser = new BasicParser();
		
		try {
			CommandLine cmd = parser.parse( options, args);
                        
                        System.out.println(args[0]);
                        
			if(cmd.hasOption("repoid")){ 
				repo.setRepoId(cmd.getOptionValue("repoid"));
			}
			if(cmd.hasOption("objectid")) {
				objectidtodownload = (cmd.getOptionValue("objectid"));
			}
			if(cmd.hasOption("ls")) {
				listobjects = true;
			}
			if(cmd.hasOption("download")) {
				startdownloads=true;
			}
			if(cmd.hasOption("waittime")) {
				try {
					waittime = new Long(cmd.getOptionValue("waittime")).longValue();
				} catch (Exception e) {
					waittime = 0;
				}
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		List<IObject> objectsinrepo = repo.getObjectsInRepo();
		
		if(listobjects) {
			listObjects(objectsinrepo);
		}
		
		if(startdownloads) {
			for (IObject objecttodownload : objectsinrepo) {
				if(objectidtodownload.contentEquals("") ||
				    objecttodownload.getObjectKey()
				       .contentEquals(objectidtodownload)) {
					downloadObject(objecttodownload, queuemanager);
					System.out.println("Pausing for " + waittime + " milliseconds");
					try {
						Thread.sleep(waittime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void downloadObject(
			IObject objecttodownload,
			IQueueManager queuemanager) {
		ConsoleProgressBar progressbar = new ConsoleProgressBar();
		System.out.println("Attempting to get object: " 
				+ objecttodownload.getObjectKey());
		try {
			InputStream input = 
					objecttodownload.getObjectContent();
			OutputStream outStream = 
					new FileOutputStream(objecttodownload.getObjectKey());
			long currentbytes = 0;
			int bytesread=0;
			
			System.out.println("Starting download of :" 
					+ objecttodownload.getObjectKey());
			String previousprogress="";
			
			byte[] bytebuffer = new byte[512*1024];
			
			while ((bytesread = input.read(bytebuffer))!=-1) {
				outStream.write(bytebuffer,0,bytesread);
				currentbytes = currentbytes + bytesread;
				String newprogress = progressbar.getProgress(
					currentbytes,
					bytesread,
					objecttodownload.getObjectSize()) + "\r";
				if(!newprogress.contentEquals(previousprogress)) {
					System.out.print(newprogress);
					previousprogress = newprogress;
				}
			}
			System.out.println(progressbar.getProgress(
					currentbytes,
					bytesread,
					objecttodownload.getObjectSize()));
			System.out.println("Object downloaded");
			input.close();
			outStream.close();
			queuemanager.sendReceivedObjectMessageToQueue(
					objecttodownload.getObjectKey());
			
		} catch (IOException e) {
			e.printStackTrace();
			queuemanager.sendFailedObjectMessageToQueue(
					objecttodownload.getObjectKey(), 
					e.getStackTrace().toString());
		}
	}
	
	public static void listObjects(List<IObject> objectsinrepo) {
		for(IObject obj : objectsinrepo) {
			System.out.println(
				obj.getObjectKey() + " : " + 
			    obj.getObjectSize() + " : " +
				obj.getStorageClass());
		}
	}
	
	public static Options addOptions() {
		Options options = new Options();
		options.addOption("repoid", true, "Repository ID");
		options.addOption("objectid", true, "Object ID");
		options.addOption("ls", false, "List Objects");
		options.addOption("download", false, "Download objects");
		options.addOption("waittime", true, "Wait time between polling");
		return options;
	}
}
