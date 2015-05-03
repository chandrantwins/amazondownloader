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
import com.jktsoftware.amazondownloader.download.interfaces.ICredentials;
import org.apache.commons.cli.HelpFormatter;

public class Application {

    private static Boolean _arglistobjects = false;
    private static Boolean _argstartdownloads = false;
    private static String _argobjectidtodownload = "";
    private static String _argrepoid="";
    private static String _awsS3accesskey="";
    private static String _awsS3secretkey="";
    private static String _awsSQSaccesskey="";
    private static String _awsSQSsecretkey="";
    private static String _awsSQSsuccessqueue="";
    private static String _awsSQSfailqueue="";
    
    private static long _argwaittime = 0;

    public static void main(String[] args) {
        if(parseOptions(args)) {
            processQueue();
        }
    }
    
    private static Options addOptions() {
        //add available options to the options object for 
        //later parsing within the commandline
        Options options = new Options();
        options.addOption("rid","repositoryid", true, "Repository ID");
        options.addOption("oid","objectid", true, "Object ID");
        options.addOption("cdwn","confirmdownload", 
                false, "Confirm download of objects");
        options.addOption("wt","waittime", true, "Wait time between polling");
        options.addOption("s3ak","s3accesskey",true, "S3 access key");
        options.addOption("s3sk","s3secretkey",true, "S3 secret key");
        options.addOption("sqsak","sqsaccesskey",true, "SQS access key");
        options.addOption("sqssk","sqssecretkey",true,"SQS secret key");
        options.addOption("sq", "successqueue", true, "Success queue name");
        options.addOption("fq", "failqueue", true, "Fail queue name");
        options.addOption("ls","listobjects", 
                false, "List objects in repository");
        options.addOption("h","help",false, "Help screen");
        return options;
    }
    
    private static boolean parseOptions(String[] args) {
        //add available command line options to the command line 
        //options object (for later parsing)
        Options options = addOptions();

        //create a basic command line parser object
        CommandLineParser parser = new BasicParser();

        try {
            //parse the command line arguments
            CommandLine cmd = parser.parse(options, args);

            //if the -rid option has been specified this
            //indicates we will not be using the default repository
            //specified within the spring.xml file and instead we
            //will be using the newly specified bucket repository
            if (cmd.hasOption("rid")) {
                _argrepoid = (cmd.getOptionValue("rid"));
            }
            //if the -oid has been specified we will be
            //downloading the specified object only from the
            //repository
            if (cmd.hasOption("oid")) {
                _argobjectidtodownload
                        = (cmd.getOptionValue("oid"));
            }
            //these are the amazon access key and secret key parameters
            if (cmd.hasOption("s3ak")) {
                _awsS3accesskey=(cmd.getOptionValue("s3ak"));
            }
            if (cmd.hasOption("s3sk")) {
                _awsS3secretkey=(cmd.getOptionValue("s3sk"));
            }
            if (cmd.hasOption("sqsak")) {
                _awsSQSaccesskey=(cmd.getOptionValue("sqsak"));
            }
            if (cmd.hasOption("sqssk")) {
                _awsSQSsecretkey=(cmd.getOptionValue("sqssk"));
            }
            if (cmd.hasOption("sq")) {
                _awsSQSsuccessqueue = (cmd.getOptionValue("sq"));
            }
            if (cmd.hasOption("fq")) {
                _awsSQSfailqueue = (cmd.getOptionValue("fq"));
            }
            //if the -ls option has been specified then the program
            //will list the available objects within the repository
            if (cmd.hasOption("ls")) {
                _arglistobjects = true;
            }
            //if the -cdwn option has been specified we will
            //actually download the object. -download must be
            //within the command line to actually download the 
            //object as this will cost money
            if (cmd.hasOption("cdwn")) {
                _argstartdownloads = true;
            }
            //if the -wt option has been specified the 
            //program will wait for a specified period of time
            //in milliseconds before trying to download the
            //next file in the queue
            if (cmd.hasOption("wt")) {
                try {
                    _argwaittime = new Long(
                            cmd.getOptionValue("waittime"))
                            .longValue();
                } catch (Exception e) {
                    _argwaittime = 0;
                }
            }
            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "java -jar <name of jar> [options]", 
                        options );
                return false;
            }
        } catch (ParseException e1) {
            // if there is an error print the stack
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "java -jar <name of jar> [options]", options );
            return false;
        }
        return true;
    }

    public static void processQueue() {
        //load spring.xml configuration file
        ApplicationContext context
                = new ClassPathXmlApplicationContext(
                        new String[]{"spring.xml"});

        //create an s3 bucket type repository object
        IObjectRepo repo
                = (IObjectRepo) context.getBean("s3bucket");
        
        if(!_awsS3accesskey.isEmpty() && !_awsS3secretkey.isEmpty()) {
            //if the aws S3 command line parameters are present then
            //override them
            ICredentials overridecredentials = (ICredentials)
                    new AwsCredentials(_awsS3accesskey, _awsS3secretkey);
            repo.setCredentials(overridecredentials);
        }
                
        System.out.println(repo.getCredentials().getAccessKey());
        
        //setup the amazon sqs based queue manager
        IQueueManager queuemanager
                = (IQueueManager) context.getBean("queuemanager");
        
        if(!_awsSQSaccesskey.isEmpty() && !_awsSQSsecretkey.isEmpty()) {
            //if the aws SQS command line parameters are present then
            //override them
            ICredentials overridecredentials = (ICredentials)
                    new AwsCredentials(_awsSQSaccesskey, _awsSQSsecretkey);
            queuemanager.setCredentials(overridecredentials);
        }
        
        //if success queue name is specified in commandline use it instead
        if(!_awsSQSsuccessqueue.isEmpty()) {
            queuemanager.setSuccessQueueName(_awsSQSsuccessqueue);
        }
        
        //if fail queue name is specified in commandline use it instead
        if(!_awsSQSfailqueue.isEmpty()) {
            queuemanager.setFailQueueName(_awsSQSfailqueue);
        }

        //create queues on the cloud platform
        queuemanager.CreateQueues();
        
        System.out.println(queuemanager.getCredentials().getAccessKey());

        //set the repository id if it has been overridden by commandline
        if (!_argrepoid.isEmpty()){
            repo.setRepoId(_argrepoid);
        }
        
        //get a list of all objects in the repository
        List<IObject> objectsinrepo = repo.getObjectsInRepo();

        //if the listobject option has been specified print a list
        //of objects and the associate properties to the console
        if (_arglistobjects) {
            listObjects(objectsinrepo);
        }

        //if the -download option has been specified then commence
        //downloading the objects from the repository
        if (_argstartdownloads) {
            //loop throught the objects in the repository
            for (IObject objecttodownload : objectsinrepo) {
                if (_argobjectidtodownload.contentEquals("")
                        || objecttodownload.getObjectKey()
                        .contentEquals(_argobjectidtodownload)) {
                    //download the object
                    downloadObject(objecttodownload, queuemanager);
                    //print the pause message
                    System.out.println("Pausing for " 
                            + _argwaittime + " milliseconds");
                    try {
                        //sleep for the specified amount of time
                        Thread.sleep(_argwaittime);
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
            //create an inputstream object for the download from the repo
            InputStream input
                    = objecttodownload.getObjectContent();
            //create an outputstream for writing the stream to a local file
            OutputStream outStream
                    = new FileOutputStream(objecttodownload.getObjectKey());
            //current bytes read from the repo
            long currentbytes = 0;
            //bytes read within the loop
            int bytesread = 0;
            //print status message to the console
            System.out.println("Starting download of :"
                    + objecttodownload.getObjectKey());
            //progress message variable
            String previousprogress = "";
            //buffer for holding downloaded bytes
            byte[] bytebuffer = new byte[512 * 1024];
            //start downloading file bit by bit
            while ((bytesread = input.read(bytebuffer)) != -1) {
                //write downloaded bytes to the file
                outStream.write(bytebuffer, 0, bytesread);
                //update downloaded byte count
                currentbytes = currentbytes + bytesread;
                //get a text representation of a progress bar
                String newprogress = progressbar.getProgress(
                        currentbytes,
                        objecttodownload.getObjectSize()) + "\r";
                if (!newprogress.contentEquals(previousprogress)) {
                    //write progress bar to console
                    System.out.print(newprogress);
                    previousprogress = newprogress;
                }
            }
            //write progress bar to console
            System.out.println(progressbar.getProgress(
                    currentbytes,
                    objecttodownload.getObjectSize()));
            System.out.println("Object downloaded");
            //close the streams
            input.close();
            outStream.close();
            //update queue with details of downloaded file
            queuemanager.sendReceivedObjectMessageToQueue(
                    objecttodownload.getObjectKey());

        } catch (IOException e) {
            e.printStackTrace();
            //update queue with details of failed file
            queuemanager.sendFailedObjectMessageToQueue(
                    objecttodownload.getObjectKey(),
                    e.getStackTrace().toString());
        }
    }

    public static void listObjects(List<IObject> objectsinrepo) {
        //list objects within the repository 
        for (IObject obj : objectsinrepo) {
            System.out.println(
                    obj.getObjectKey() + " : "
                    + obj.getObjectSize() + " : "
                    + obj.getStorageClass());
        }
    }
}
