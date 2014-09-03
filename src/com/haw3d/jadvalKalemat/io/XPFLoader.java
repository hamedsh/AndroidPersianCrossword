package com.haw3d.jadvalKalemat.io;

/**
 * Created by haw3d on 1/18/14.
 */

import com.haw3d.jadvalKalemat.puz.Location;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import com.haw3d.jadvalKalemat.puz.Box;
import com.haw3d.jadvalKalemat.puz.Puzzle;

public class XPFLoader {
    private static String CHARSET_NAME = "utf8";

    public enum Node{
        TITLE, AUTHOR, COPYRIGHT, NOTEPAD, PUBLISHER,
        EDITOR, DATE, ROWS, COLS, ROW,
        CLUE, REBUS,SOURCE
    }
    public static class Clue{
        public String across;
        public Integer row;
        public Integer col;
        public Integer num;
    }
    public static class Rebus{
        public Integer row;
        public Integer col;
    }

    private static class UclickXMLParser extends DefaultHandler {
        private Puzzle puz;
        private Node node;
        private Boolean grid;
        private Boolean size;
        private Boolean clues;
        private Boolean userGrid;
        private Boolean rebusEntries;
        private Boolean userRebusEntries;
        private Boolean squareFlags;
        private Boolean pencils;
        private Box[][] boxes;
        private ArrayList<String> acrossClues = new ArrayList<String>();
        private ArrayList<String> downClues = new ArrayList<String>();
        private ArrayList<Integer> acrossCluesLookup = new ArrayList<Integer>();
        private ArrayList<Integer> downCluesLookup = new ArrayList<Integer>();
        private int row;
        private Clue clue=new Clue();
        private Rebus rebus=new Rebus();
        private ArrayList<Location> downClueLocations=new ArrayList<Location>();
        private ArrayList<Location> acrossClueLocations=new ArrayList<Location>();

        public UclickXMLParser(Puzzle puz) {
            this.puz =puz;
            grid=false;
            size=false;
            clues=false;
            userGrid=false;
            rebusEntries=false;
            userRebusEntries=false;
            squareFlags =false;
            pencils = false;
            row = 0;
        }

        @Override
        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes) throws SAXException {
            if(qName.equalsIgnoreCase("title"))
                node=Node.TITLE;
            else if(qName.equalsIgnoreCase("author"))
                node=Node.AUTHOR;
            else if(qName.equalsIgnoreCase("Source"))
                node=Node.SOURCE;
            else if(qName.equalsIgnoreCase("Copyright"))
                node=Node.COPYRIGHT;
            else if(qName.equalsIgnoreCase("notepad"))
                node=Node.NOTEPAD;
            else if(qName.equalsIgnoreCase("publisher"))
                node=Node.PUBLISHER;
            else if(qName.equalsIgnoreCase("editor"))
                node=Node.EDITOR;
            else if(qName.equalsIgnoreCase("date"))
                node=Node.DATE;
            else if(qName.equalsIgnoreCase("row"))
                node=Node.ROW;
            else if(qName.equalsIgnoreCase("Cols"))
                node=Node.COLS;
            else if(qName.equalsIgnoreCase("rows"))
                node=Node.ROWS;
            else if(qName.equalsIgnoreCase("Clue"))
                node=Node.CLUE;
            else if(qName.equalsIgnoreCase("flag") && squareFlags){
                boxes[Integer.parseInt(attributes.getValue("Row"))-1][Integer.parseInt(attributes.getValue("Col"))-1].setCheated(true);
            }
            else if(qName.equalsIgnoreCase("pencil") && pencils){
                boxes[Integer.parseInt(attributes.getValue("Row"))-1][Integer.parseInt(attributes.getValue("Col"))-1].setPencil(true);
            }
            else if(qName.equalsIgnoreCase("Rebus"))
            {
                rebus.row= Integer.parseInt(attributes.getValue("Row"))-1;
                rebus.col= Integer.parseInt(attributes.getValue("Col"))-1;
                node=Node.REBUS;
            }
            else if(qName.equalsIgnoreCase("size")) //Set 2 level
                size=true;
            else if(qName.equalsIgnoreCase("grid"))
                grid=true;
            else if(qName.equalsIgnoreCase("RebusEntries"))
                rebusEntries=true;
            else if(qName.equalsIgnoreCase("clues"))
                clues=true;
            else if(qName.equalsIgnoreCase("UserGrid"))
                userGrid=true;
            else if(qName.equalsIgnoreCase("UserRebusEntries"))
                userRebusEntries=true;
            else if(qName.equalsIgnoreCase("SquareFlags"))
                squareFlags=true;
            else if(qName.equalsIgnoreCase("Pencils"))
                pencils = true;

            if(clues && qName.equalsIgnoreCase("Clue"))
            {
                clue.across=attributes.getValue("Dir");
                clue.row= Integer.parseInt(attributes.getValue("Row"))-1;
                clue.col= Integer.parseInt(attributes.getValue("Col"))-1;
                clue.num= Integer.parseInt(attributes.getValue("Num"));
                node=Node.CLUE;
            }
        }

        @Override
        public void endElement(String uri, String localName,
                               String qName) throws SAXException {
            if(qName.equalsIgnoreCase("size")){
                boxes = new Box[puz.getHeight()][puz.getWidth()];
                size=false;
            }else if(qName.equalsIgnoreCase("grid")){
                row=0;
                grid=false;
            }else if(qName.equalsIgnoreCase("Clues")){
                clues=false;
            }else if(qName.equalsIgnoreCase("UserGrid")){
                userGrid=false;
            }else if(qName.equalsIgnoreCase("RebusEntries")){
                rebusEntries=false;
            }else if(qName.equalsIgnoreCase("UserRebusEntries")){
                userRebusEntries=false;
            }else if(qName.equalsIgnoreCase("SquareFlags")){
                squareFlags=false;
            }else if(qName.equalsIgnoreCase("pencils")){
                pencils = false;
            }else if(qName.equalsIgnoreCase("Puzzle")){
                puz.setBoxes(boxes);
                puz.setAcrossClues(acrossClues.toArray(new String[acrossClues.size()]));
                puz.setDownClues(downClues.toArray(new String[downClues.size()]));
                puz.setAcrossCluesLookup(acrossCluesLookup.toArray(new Integer[acrossCluesLookup.size()]));
                puz.setDownCluesLookup(downCluesLookup.toArray(new Integer[acrossCluesLookup.size()]));
                puz.setDownCluesLocation(downClueLocations.toArray(new Location[downClueLocations.size()]));
                puz.setAcrossCluesLocation(acrossClueLocations.toArray(new Location[acrossClueLocations.size()]));

            }
        }
        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            if(node!= null)
            {
                switch (node){
                    case  TITLE:
                        this.puz.setTitle(new String(ch, start, length));
                        break;
                    case AUTHOR:
                        this.puz.setAuthor(new String(ch, start, length));
                        break;
                    case SOURCE:
                        this.puz.setSource(new String(ch, start, length));
                        break;
                }
                if(size){
                    switch (node){
                        case ROWS:
                            puz.setWidth(Integer.parseInt(new String(ch, start, length)));
                            break;
                        case COLS:
                            puz.setHeight(Integer.parseInt(new String(ch, start, length)));
                            break;
                    }
                }else if(rebusEntries && node == Node.REBUS){
                    char tmpchar=ch[start];
                    if(tmpchar=='ا')
                        tmpchar='آ';
                    if(tmpchar == 'ي')
                        tmpchar = 'ی';
                    if(tmpchar == 'ك')
                        tmpchar = 'ک';
                    boxes[rebus.row][rebus.col].setSolution(tmpchar);
                }else if(grid && node == Node.ROW){
                    for(int c=0;c<puz.getWidth();c++)
                        if(ch[c+start]!='.')
                        {
                            boxes[row][c]=new Box();
                            boxes[row][c].setSolution(ch[c+start]);
                        }
                    row++;
                }else if(clues && node==Node.CLUE){
                    boxes[clue.row][clue.col].setClueNumber(clue.num);
                    if(clue.across.equalsIgnoreCase("across")){
                        acrossClues.add(new String(ch, start, length));
                        acrossCluesLookup.add(clue.num);
                        boxes[clue.row][clue.col].setAcross(true);
                        acrossClueLocations.add(new Location(clue.row, clue.col, clue.num));
                    }else if(clue.across.equalsIgnoreCase("down")){
                        downClues.add(new String(ch, start, length));
                        downCluesLookup.add(clue.num);
                        boxes[clue.row][clue.col].setDown(true);
                        downClueLocations.add(new Location(clue.row, clue.col, clue.num));
                    }
                /*}else if(userGrid && node == Node.ROW){
                    for(int c=0;c<puz.getWidth();c++)
                        if(ch[c+start]!='.')
                            boxes[row][c].setResponse(ch[c+start]);
                    row++;*/
                }else if(userRebusEntries && node == Node.REBUS){
                    boxes[rebus.row][rebus.col].setResponse(ch[start]);
                }
            }
            node=null;
        }
    }

    public static Puzzle loadXPF(InputStream is) {
        Puzzle puz = new Puzzle();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            //parser.setProperty("http://xml.org/sax/features/validation", false);
            XMLReader xr = parser.getXMLReader();
            xr.setContentHandler(new UclickXMLParser(puz));
            xr.parse(new InputSource(is));

            puz.setVersion(IO.VERSION_STRING);

            return puz;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
