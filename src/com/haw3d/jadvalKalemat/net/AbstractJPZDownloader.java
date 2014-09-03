/**
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

package com.haw3d.jadvalKalemat.net;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;

import com.haw3d.jadvalKalemat.jadvalKalematApplication;
import com.haw3d.jadvalKalemat.io.JPZIO;

public abstract class AbstractJPZDownloader extends AbstractDownloader {

    protected AbstractJPZDownloader(String baseUrl, String downloaderName) {
        super(baseUrl, downloaderName);
    }

    @Override
    protected void download(Calendar date, String urlSuffix, Map<String, String> headers)
            throws IOException {
        download(date, urlSuffix, headers, JPZIO.NOOP_METADATA_SETTER);
    }

    protected void download(Calendar date, String urlSuffix, Map<String, String> headers,
            JPZIO.PuzzleMetadataSetter metadataSetter) throws IOException {
        URL url = new URL(this.baseUrl + urlSuffix);

        LOG.info("Downloading " + url);

        String filename = getFilename(date);
        File jpzFile = new File(jadvalKalematApplication.TEMP_DIR, filename);
        utils.downloadFile(url, headers, jpzFile, true, getName());

        try {
            File destFile = new File(jadvalKalematApplication.CROSSWORDS_DIR, filename);
            JPZIO.convertJPZPuzzle(jpzFile, destFile, metadataSetter);
        } finally {
            jpzFile.delete();
        }
    }
}
