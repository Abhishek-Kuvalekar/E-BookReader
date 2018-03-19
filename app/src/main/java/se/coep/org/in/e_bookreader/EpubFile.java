package se.coep.org.in.e_bookreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by dell on 19/3/18.
 */

public class EpubFile {
    private String fileName;
    private File unzipLocation;
    private List<String> contents;
    private String unzippedDir;
    private int currentChapter;

    public EpubFile(String fileName) {
        this.fileName = fileName;
        currentChapter = 0;
    }

    public void unzip(Context context) {
        String zipFile = fileName;
        unzipLocation = context.getFilesDir();
        Decompress d = new Decompress(zipFile, unzipLocation.toString() + "/unzipped/");
        d.unzip();
    }

    public String getUnzippedDirectory() {
        return unzipLocation.toString() + "/unzipped";
    }

    public void parse(String fileToBeParsed) {
        List<String> navList = new ArrayList<String>();
        try {
            File inputFile = new File(fileToBeParsed);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("navPoint");

            for(int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if(nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    int pos = Integer.parseInt(eElement.getAttribute("playOrder")) - 1;
                    Element node = (Element)eElement.getElementsByTagName("content").item(0);
                    String chapter = node.getAttribute("src");
                    navList.add(pos, chapter);
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.contents = navList;
    }

    public String getContentDir(String[] fileList) {
        for(String file: fileList) {
            if(file.startsWith("O")) {
                return file;
            }
        }
        return null;
    }

    public String getContentFile() {
        String rootDir = getUnzippedDirectory();
        String dir = getContentDir(new File(rootDir).list());
        if(dir == null) {
            return null;
        }
        String[] files = new File(rootDir + "/" +dir).list();
        for(String fileName : files) {
            if(fileName.endsWith("ncx")) {
                return fileName;
            }
        }
        return null;
    }

    public void getContent(Context context) {
        unzip(context);
        unzippedDir = getUnzippedDirectory();
        parse(unzippedDir + "/" +
                getContentDir(new File(unzippedDir).list()) + "/" +
                getContentFile());
    }

    public String getCurrentChapterPath() {
        String path = unzippedDir + "/" + getContentDir(new File(unzippedDir).list()) +
                "/" + contents.get(currentChapter);
        return path;
    }
    public void open(final Context context, View view) {
        final WebView webView = (WebView) view.findViewById(R.id.webview);
        webView.loadUrl(String.valueOf(Uri.fromFile(new File(getCurrentChapterPath()))));
        WebSettings settings = webView.getSettings();
        settings.setBuiltInZoomControls(true); //sets zooming with pinching
        //settings.setTextZoom(110);     //sets the zoom of the page in percent
        settings.setDefaultFontSize(18); //sets the font size. default is 16.
        settings.setDisplayZoomControls(false); //set display of zoom controls

        webView.setOnTouchListener(new OnSwipeTouchListener(context) {
            public void onSwipeLeft() {
                if(currentChapter != contents.size()) {
                    currentChapter++;
                }
                else {
                    Toast.makeText(context, "End of the book", Toast.LENGTH_SHORT).show();
                }
                webView.loadUrl(String.valueOf(Uri.fromFile(new File(getCurrentChapterPath()))));
            }
            public void onSwipeRight() {
                if(currentChapter != 0) {
                    currentChapter--;
                }
                else {
                    Toast.makeText(context, "Start of the book", Toast.LENGTH_SHORT).show();
                }
                webView.loadUrl(String.valueOf(Uri.fromFile(new File(getCurrentChapterPath()))));
            }
        });
    }
}
