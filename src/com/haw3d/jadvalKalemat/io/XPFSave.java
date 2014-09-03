package com.haw3d.jadvalKalemat.io;

/**
 * Created by haw3d on 1/19/14.
 */
import com.haw3d.jadvalKalemat.puz.Box;
import com.haw3d.jadvalKalemat.puz.Puzzle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.OutputStream;

/**
 * Created by haw3d on 1/18/14.
 */
public class XPFSave {
    private DocumentBuilderFactory docFactory;
    DocumentBuilder docBuilder;
    Document doc;
    XPFSave(){
        super();
        docFactory = DocumentBuilderFactory.newInstance();
        docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        doc = docBuilder.newDocument();
    }

    private Element addNode(String name,String value){
        Element t=doc.createElement(name);
        if(value!=null)
            t.appendChild(doc.createTextNode(value));
        return t;
    }
    private Element addNode(String name){
        Element t=doc.createElement(name);
        return t;
    }
    private Attr addAttribute(String name,String value){
        Attr attr=doc.createAttribute(name);
        attr.setValue(value);
        return attr;
    }

    public Boolean SaveXPF(Puzzle puz,OutputStream outputStream){
        try {
            String tmpStr;
            Box[][] boxes=puz.getBoxes();
            String[] acrossClues=puz.getAcrossClues();
            String[] downClues=puz.getDownClues();
            Element tmp;
            Element tmp2=null;
            Attr attr;

            Element Puzzles = doc.createElement("Puzzles");
            doc.appendChild(Puzzles);
            Puzzles.setAttributeNode(addAttribute("Version","1.0"));

            Element Puzzle = doc.createElement("Puzzle");
            Puzzles.appendChild(Puzzle);

            Puzzle.appendChild(addNode("Title",puz.getTitle()));
            Puzzle.appendChild(addNode("Source",puz.getSource()));
            Puzzle.appendChild(addNode("Author",puz.getAuthor()));
            Puzzle.appendChild(addNode("Copyright",puz.getCopyright()));
            Puzzle.appendChild(addNode("Notepad",puz.getNotes()));
            Puzzle.appendChild(addNode("Publisher"));
            Puzzle.appendChild(addNode("Editor",puz.getNotes()));
            Puzzle.appendChild(addNode("Date"));

            tmp = doc.createElement("Size");
            tmp.appendChild(addNode("Rows", String.valueOf(puz.getHeight())));
            tmp.appendChild(addNode("Cols", String.valueOf(puz.getWidth())));
            Puzzle.appendChild(tmp);

            tmp=doc.createElement("Grid");
            for(int r=0;r<puz.getHeight();r++)
            {
                tmpStr="";
                for(int c=0;c<puz.getWidth();c++)
                {
                    if(boxes[r][c]==null)
                        tmpStr+='.';
                    else if(boxes[r][c].getSolution()>='A' && boxes[r][c].getSolution()<='Z')
                        tmpStr+=boxes[r][c].getSolution();
                    else{
                        tmpStr+=' ';
                    }

                }
                tmp.appendChild(addNode("Row",tmpStr));
            }
            Puzzle.appendChild(tmp);

            Puzzle.appendChild(addNode("Circles"));

            tmp=doc.createElement("RebusEntries");
            for(int r=0;r<puz.getHeight();r++)
            {
                tmp2=null;
                for(int c=0;c<puz.getWidth();c++)
                {
                    if(boxes[r][c]!=null)
                        if(boxes[r][c].getSolution()>'Z')
                        {
                            tmp2=addNode("Rebus", String.valueOf(boxes[r][c].getSolution()));
                            tmp2.setAttributeNode(addAttribute("Row",String.valueOf(r+1)));
                            tmp2.setAttributeNode(addAttribute("Col",String.valueOf(c+1)));
                            tmp2.setAttributeNode(addAttribute("Short"," "));
                            if(tmp2!=null)
                                tmp.appendChild(tmp2);
                        }
                }
            }
            Puzzle.appendChild(tmp);

            Puzzle.appendChild(addNode("Shades"));

            tmp=doc.createElement("Clues");
            for(int num=0;num<acrossClues.length;num++)
            {
                tmp2=null;
                tmp2=addNode("Clue",acrossClues[num]);
                tmp2.setAttributeNode(addAttribute("Dir","Across"));
                tmp2.setAttributeNode(addAttribute("Row", String.valueOf(puz.getLocation(num,true).row+1)));
                tmp2.setAttributeNode(addAttribute("Col", String.valueOf(puz.getLocation(num,true).col+1)));
                tmp2.setAttributeNode(addAttribute("Num", String.valueOf(puz.getLocation(num,true).num)));
                tmp.appendChild(tmp2);
            }
            for(int num=0;num<downClues.length;num++)
            {
                tmp2=null;
                tmp2=addNode("Clue",downClues[num]);
                tmp2.setAttributeNode(addAttribute("Dir","Down"));
                tmp2.setAttributeNode(addAttribute("Row", String.valueOf(puz.getLocation(num,false).row+1)));
                tmp2.setAttributeNode(addAttribute("Col", String.valueOf(puz.getLocation(num,false).col+1)));
                tmp2.setAttributeNode(addAttribute("Num", String.valueOf(puz.getLocation(num,false).num)));
                tmp.appendChild(tmp2);
            }
            Puzzle.appendChild(tmp);

            tmp=doc.createElement("UserGrid");
            for(int r=0;r<puz.getHeight();r++)
            {
                tmpStr="";
                for(int c=0;c<puz.getWidth();c++)
                {
                    if(boxes[r][c]==null)
                        tmpStr+='.';
                    else if((boxes[r][c].getResponse()>='A' && boxes[r][c].getResponse()<='Z') ||boxes[r][c].getResponse()==' ')
                        tmpStr+=boxes[r][c].getResponse();
                    else
                        tmpStr+=' ';
                }
                tmp.appendChild(addNode("Row",tmpStr));
            }
            Puzzle.appendChild(tmp);

            tmp=doc.createElement("UserRebusEntries");
            for(int r=0;r<puz.getHeight();r++)
            {
                tmp2=null;
                for(int c=0;c<puz.getWidth();c++)
                {
                    if(boxes[r][c]!=null)
                        if(boxes[r][c].getResponse()>'Z')
                        {
                            tmp2=addNode("Rebus", String.valueOf(boxes[r][c].getResponse()));
                            tmp2.setAttributeNode(addAttribute("Row",String.valueOf(r+1)));
                            tmp2.setAttributeNode(addAttribute("Col",String.valueOf(c+1)));
                            tmp2.setAttributeNode(addAttribute("Short"," "));
                            if(tmp2!=null)
                                tmp.appendChild(tmp2);
                        }
                }
            }
            Puzzle.appendChild(tmp);

            tmp=doc.createElement("SquareFlags");
            for(int r=0;r<puz.getHeight();r++)
            {
                tmp2=null;
                for(int c=0;c<puz.getWidth();c++)
                {
                    if(boxes[r][c]!=null)
                        if(boxes[r][c].isCheated())
                        {
                            tmp2=addNode("Flag");
                            tmp2.setAttributeNode(addAttribute("Row",String.valueOf(r+1)));
                            tmp2.setAttributeNode(addAttribute("Col",String.valueOf(c+1)));
                            tmp2.setAttributeNode(addAttribute("Revealed","true"));
                            if(tmp2!=null)
                                tmp.appendChild(tmp2);
                        }
                }
            }
            Puzzle.appendChild(tmp);

            tmp=doc.createElement("Pencils");
            for(int r=0;r<puz.getHeight();r++)
            {
                tmp2=null;
                for(int c=0;c<puz.getWidth();c++)
                {
                    if(boxes[r][c]!=null)
                        if(boxes[r][c].isPencil())
                        {
                            tmp2=addNode("pencil");
                            tmp2.setAttributeNode(addAttribute("Row",String.valueOf(r+1)));
                            tmp2.setAttributeNode(addAttribute("Col",String.valueOf(c+1)));
                            tmp2.setAttributeNode(addAttribute("UsePencil","true"));
                            if(tmp2!=null)
                                tmp.appendChild(tmp2);
                        }
                }
            }
            Puzzle.appendChild(tmp);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(outputStream);
            //StreamResult result =  new StreamResult(System.out);
            transformer.transform(source, result);
            return true;
        }catch (TransformerException e) {
            e.printStackTrace();
        }
        return false;
    }
}
