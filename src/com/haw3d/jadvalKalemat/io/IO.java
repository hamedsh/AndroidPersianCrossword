package com.haw3d.jadvalKalemat.io; /**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.os.Environment;
import com.haw3d.jadvalKalemat.jadvalKalematApplication;
import com.haw3d.jadvalKalemat.puz.Puzzle;

import java.io.*;
import java.util.logging.Logger;

import static com.haw3d.jadvalKalemat.io.XPFLoader.loadXPF;

public class IO {

    public static final String VERSION_STRING = "1.2";

    private static final Logger LOG = Logger.getLogger("com.haw3d.jadvalKalemat");

    /**
     * Copies the data from an InputStream object to an OutputStream object.
     *
     * @param sourceStream
     *            The input stream to be read.
     * @param destinationStream
     *            The output stream to be written to.
     * @return int value of the number of bytes copied.
     * @exception IOException
     *                from java.io calls.
     */
    public static int copyStream(InputStream sourceStream, OutputStream destinationStream)
            throws IOException {
        int bytesRead = 0;
        int totalBytes = 0;
        byte[] buffer = new byte[4096];

        while (bytesRead >= 0) {
            bytesRead = sourceStream.read(buffer, 0, buffer.length);

            if (bytesRead > 0) {
                destinationStream.write(buffer, 0, bytesRead);
            }

            totalBytes += bytesRead;
        }

        destinationStream.flush();

        return totalBytes;
    }

    public static Puzzle load(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            //return load(new DataInputStream(fis));
            Puzzle puz=loadXPF(new DataInputStream(fis));
            return puz;
        } finally {
            fis.close();
        }
    }

    public static void save(Puzzle puzzle, File destFile) throws IOException {
        long incept = System.currentTimeMillis();
        //TODO: Save method
        File tempFile = new File(jadvalKalematApplication.CROSSWORDS_DIR + "tmp.pzl");
        tempFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            save(puzzle, fos);
        } finally {
            fos.close();
        }
        if (!tempFile.renameTo(destFile)) {
            throw new IOException("Failed to rename " + tempFile + " to " + destFile);
        }
        LOG.info("Save complete in " + (System.currentTimeMillis() - incept) + " ms");
    }

    public static boolean save(Puzzle puz, OutputStream os)
            throws IOException {
            XPFSave t=new XPFSave();
            return t.SaveXPF(puz,os);
    }
}
