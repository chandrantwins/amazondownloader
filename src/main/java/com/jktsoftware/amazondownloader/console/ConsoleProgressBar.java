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
     * @param progress total bytes read so far (e.g. 2048 bytes of a 3072 byte
     * file)
     * @param total total size of file being read (e.g. 3072 bytes)
     * @return a string representation of a progress bar
     */
    public String getProgress(long progress, long total) {
        String progressbar = "Bytes read: "
                + getProgressBar(progress, total, 20);
        return progressbar;
    }

    private String getProgressBar(long progress, long total, long sections) {
        String progressbar = "";
        progressbar = progressbar + "[";
        float bytespersec = (float) total / sections; //bytes per section
        String bar = "";
        float completed = 0;
        for (long passes = 1; passes <= sections; passes = passes + 1) {
            if ((passes) * bytespersec <= progress) {
                completed = passes;
                bar = bar + "=";
            } else {
                bar = bar + " ";
            }
        }

        String in = "          ";
        StringBuilder sb = new StringBuilder();
        sb.append(in);
        if (progress >= (1024 * 1024 * 1024)) {
            float display = (float) progress / (1024 * 1024 * 1024);
            String inprogress = String.format("%.1f", display) + " GiB";
            sb.insert(0, inprogress);
        } else if (progress >= (1024 * 1024)) {
            float display = (float) progress / (1024 * 1024);
            String inprogress = String.format("%.1f", display) + " MiB";
            sb.insert(0, inprogress);
        } else if (progress >= 1024) {
            float display = (float) progress / 1024;
            String inprogress = String.format("%.1f", display) + " kiB";
            sb.insert(0, inprogress);
        } else {
            float display = (float) progress;
            String inprogress = String.format("%.0f", display) + " bytes";
            sb.insert(0, inprogress);
        }

        progressbar = progressbar + bar + "]";
        progressbar = progressbar + " "
                + (String.format("%.2f", ((float) ((completed * bytespersec) / total) * 100)))
                + "% " + sb.toString() + " ";
        return progressbar;
    }
}
