package com.haw3d.jadvalKalemat.io.charset;

import com.haw3d.jadvalKalemat.puz.Puzzle;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by haw3d on 1/18/14.
 */
public class XPFSave {
    public void SaveXPF(Puzzle puz){
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}
