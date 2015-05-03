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
package com.jktsoftware.amazondownloader.console;

/**
 *
 * @author jktdev
 */
public class ConsoleProgressBar {
        
    /**
     *
     * @param progressbytes total bytes read so far (e.g. 2048 bytes of a 
     * 3072 byte file)
     * @param totalbytes total size of file being read (e.g. 3072 bytes)
     * @return a string representation of a progress bar
     */
    public String getProgress(
            long progressbytes,
            long totalbytes) 
        {
		String progress = "Bytes read: " +
			getProgressBar(progressbytes, totalbytes, 20);
		return progress;
	}
	
	private String getProgressBar(
			long progressbytes,
			long totalbytes,
			long totalsections) {
		String progressbar="";
		progressbar = progressbar + "[";
		float bytespersection = (float) totalbytes/totalsections;
		String bar = "";
		float completedsections = 0;
		for(long passes=1; passes<=totalsections; passes=passes+1) {
			if((passes)*bytespersection <= progressbytes) {
				completedsections=passes;
				bar = bar + "=";
			} else {
				bar = bar + " ";
			}
		}
		
		String in="          ";
		StringBuilder sb = new StringBuilder();
		sb.append(in);
		if(progressbytes>=(1024*1024*1024)) {
			float display = (float) progressbytes/(1024*1024*1024);
			String inprogress=String.format("%.1f", display) + " GiB";
			sb.insert(0, inprogress);
		} else if(progressbytes>=(1024*1024)) {
			float display = (float) progressbytes/(1024*1024);
			String inprogress=String.format("%.1f", display) + " MiB";
			sb.insert(0, inprogress);
		} else if(progressbytes>=1024) {
			float display = (float) progressbytes/1024;
			String inprogress=String.format("%.1f",display) + " kiB";
			sb.insert(0, inprogress);
		} else {
			float display = (float) progressbytes;
			String inprogress=String.format("%.0f",display) + " bytes";
			sb.insert(0, inprogress);
		}
		
		progressbar = progressbar + bar + "]";
		progressbar = progressbar + " " +
				  ( 
				  String.format("%.2f",((float)((completedsections * bytespersection) / totalbytes)*100))) 
				  + "% " + sb.toString() + " ";
		return progressbar;
	}
}
