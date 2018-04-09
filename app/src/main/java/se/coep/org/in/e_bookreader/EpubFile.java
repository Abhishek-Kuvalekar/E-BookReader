package se.coep.org.in.e_bookreader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by dell on 19/3/18.
 */

public class EpubFile {
    private String fileName;
    private File unzipLocation;
    private static List<String> contents;
    private String unzippedDir;
    private static int currentChapter;
    private Context context;
    private View view;
    public static final String TAG = "tag";
    private int counter = 0;
    private int fontSize = 18;
    private WebView webView;
    private int fontFamilyPosition;
    private boolean isNightModeOn;
    private double brightness;
    private String stringToBeSearched = null;

    public EpubFile(String fileName, Context context, View view) {
        this.context = context;
        this.fileName = fileName;
        this.view = view;
        this.fontFamilyPosition = 0;
        webView = (WebView) view.findViewById(R.id.webview);
        currentChapter = 0;
        this.isNightModeOn = false;
        this.brightness = 0.5;
    }

    public void setFontFamilyPosition(int fontFamilyPosition) {
        this.fontFamilyPosition = fontFamilyPosition;
    }

    public int getFontFamilyPosition() {
        return this.fontFamilyPosition;
    }

    public void unzip() {
        String zipFile = fileName;
        //unzipLocation = context.getFilesDir();
        unzipLocation = Environment.getExternalStorageDirectory();
        Log.d(TAG, String.valueOf(unzipLocation.toString()));
        Decompress d = new Decompress(zipFile, unzipLocation.toString() + "/unzipped/");
        d.unzip();
    }

    public String getUnzippedDirectory() {
        File f = new File(unzipLocation.toString()+"/unzipped/");
        String[] list = f.list();
        if(list[0].endsWith(".epub_FILES")) {
            Log.d("Decompress", "nestedfile"+ unzipLocation.toString() + "/unzipped"+list[0]);
            return unzipLocation.toString() + "/unzipped/"+list[0];
        }
        return unzipLocation.toString() + "/unzipped";
    }

    public List<String> parse(String fileToBeParsed) {
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
                    if (chapter.startsWith("OEBPS/")) {
                        String[] chapterName = chapter.split("/", 2);
                        Log.d("tag", chapterName[1]);
                        navList.add(pos, chapterName[1]);
                    }else {
                        navList.add(pos, chapter);
                    }
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
        return navList;
    }

    public List<String> parseForContent(String fileToBeParsed) {
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
                    Element node = (Element)eElement.getElementsByTagName("text").item(0);
                    String chapter = node.getTextContent();
                    if (chapter.startsWith("OEBPS/")) {
                        String[] chapterName = chapter.split("/", 2);
                        navList.add(pos, chapterName[1]);
                    }else {
                        navList.add(pos, chapter);
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return navList;
    }

    public String getContentDir(String[] fileList) {
        for(String file: fileList) {
            if(file.startsWith("O")) {
                return file;
            }
        }
        return null;
    }

    public String getCoverPage() {
        String coverPagePath = unzippedDir +"/"+ getContentDir(new File(unzippedDir).list());
        Log.d("tag3", coverPagePath);
        File f = new File(coverPagePath);
        String[] list = f.list();
        for(String coverFile: list) {
            if(coverFile.contains("cover")) {
                return coverPagePath+"/"+coverFile;
            } else if(coverFile.contains("title.xhtml")) {
                return coverPagePath+"/"+coverFile;
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

    public String getNcxFilePath() {
        //unzip();
        unzippedDir = getUnzippedDirectory();
        List<String> parsedContentList = parse(unzippedDir + "/" +
                getContentDir(new File(unzippedDir).list()) + "/" +
                getContentFile());
        String ncxFilePath = unzippedDir + "/" +
                getContentDir(new File(unzippedDir).list()) + "/" +
                getContentFile();
        return ncxFilePath;
    }

    public String getCurrentChapterPath() {
        String path = unzippedDir + "/" + getContentDir(new File(unzippedDir).list()) +
                "/" + contents.get(currentChapter);
        return path;
    }

    public String getCSSDirectory() {
        String path = unzippedDir + "/" + getContentDir(new File(unzippedDir).list()) +
                "/" + "css";
        return path;
    }

    public void setCurrentChapter(int currentChapter) {
        this.currentChapter = currentChapter;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void open(final Context context, View view, boolean navigationClicked) {
        final WebView webView = (WebView)view.findViewById(R.id.webview);
        if(getCoverPage() == null && navigationClicked == false) {
            webView.loadUrl(String.valueOf(Uri.fromFile(new File(getCurrentChapterPath()))));
        } else if(navigationClicked == true) {
            webView.loadUrl(String.valueOf(Uri.fromFile(new File(getCurrentChapterPath()))));
        } else {
            currentChapter = -1;
            webView.loadUrl(String.valueOf(Uri.fromFile(new File(getCoverPage()))));
        }
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(true); //sets zooming with pinching
        //settings.setTextZoom(110);     //sets the zoom of the page in percent
        settings.setDefaultFontSize(fontSize); //sets the font size. default is 16.
        settings.setDisplayZoomControls(false); //set display of zoom controls

        webView.setOnTouchListener(new OnSingleTapListener(context) {
            public void onSwipeLeft() {
                if(currentChapter != contents.size()) {
                    currentChapter++;
                    Log.d("tag2", getCurrentChapterPath());
                    webView.loadUrl(String.valueOf(Uri.fromFile(new File(getCurrentChapterPath()))));
                    if(stringToBeSearched != null) {
                        focusSearchedString();
                    }
                }
                else {
                    Toast.makeText(context, "End of the book", Toast.LENGTH_SHORT).show();
                }
            }
            public void onSwipeRight() {
                if(currentChapter != 0) {
                    currentChapter--;
                    webView.loadUrl(String.valueOf(Uri.fromFile(new File(getCurrentChapterPath()))));
                    if(stringToBeSearched != null) {
                        focusSearchedString();
                    }
                }
                else if(currentChapter == 0) {
                    webView.loadUrl(String.valueOf(Uri.fromFile(new File(getCoverPage()))));
                }
                else {
                    Toast.makeText(context, "Start of the book", Toast.LENGTH_SHORT).show();
                }
            }
            public void onSwipeBottom() {
                showSystemUI();
            }
            public void onSwipeTop() {
                hideSystemUI();
            }


        });
    }

    public void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        DrawerLayout drawerLayout = view.findViewById(R.id.drawer_layout);
        drawerLayout.setFitsSystemWindows(false);
        view.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public void showSystemUI() {
        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    public Activity getActivity() {
        return (Activity) context;
    }

    private void updateWebView() {
        if(this.currentChapter != -1) {
            webView.loadUrl(String.valueOf(Uri.fromFile(new File(getCurrentChapterPath()))));
        }
        else {
            currentChapter = -1;
            webView.loadUrl(String.valueOf(Uri.fromFile(new File(getCoverPage()))));
        }
    }

    private String getCurrentChapterID() {
        try {
            File file = new File(getNcxFilePath());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = null;
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("navPoint");

            for(int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if(nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    int pos = Integer.parseInt(eElement.getAttribute("playOrder")) - 1;
                    if(pos == currentChapter) {
                        String id = eElement.getAttribute("id");
                        return id;
                    }
                }
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void changeFontSize(boolean isToBeIncreased) {
        if(isToBeIncreased == true && fontSize != 24) {
            this.fontSize++;
        }
        else if(fontSize != 12 && isToBeIncreased == false) {
            this.fontSize--;
        }
        webView.getSettings().setDefaultFontSize(fontSize);
        updateWebView();
    }

    public int getFontSize() {
        return this.fontSize;
    }

    public boolean getNightModeState() {
        return this.isNightModeOn;
    }

    public float getBrightness() {
        return (float)this.brightness;
    }

    public void adjustBrightness(Window window, float value) {
        WindowManager.LayoutParams layout = window.getAttributes();
        layout.screenBrightness = value;
        window.setAttributes(layout);
        this.brightness = (double)value;
    }

    public void addCSSToXML(String CSS, String filePath) {
        BufferedReader reader = null;
        PrintWriter out = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            String content = stringBuilder.toString();
            int separator = content.indexOf("</head>");
            String finalContent = content.substring(0, separator) + "\n" + CSS + "\n" +
                    content.substring(separator, content.length());

            out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
            out.print(finalContent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                out.close();
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void changeFontStyle(String fontStyle) {
        String CSS = "<style rel = \"stylesheet\" type = \"text/css\">" +
                "body {" +
                "font-family:\"" + fontStyle + "\";}" +
                "</style>";
        if(currentChapter != -1) {
            addCSSToXML(CSS, getCurrentChapterPath());
            webView.reload();
        }
        for(int i = 0; i < contents.size(); i++) {
            String path = getUnzippedDirectory() + "/" +
                    getContentDir(new File(getUnzippedDirectory()).list()) + "/" +
                    contents.get(i);
            if (currentChapter != -1) {
                if (path == getCurrentChapterPath()) {
                    continue;
                }
            }
            addCSSToXML(CSS, path);
        }
    }

    public void switchNightMode(boolean nightMode) {
        String CSS;
        if(nightMode == true) {
            webView.setBackgroundColor(Color.BLACK);
            CSS = "<style rel = \"stylesheet\" type = \"text/css\">" +
                    "body{" +
                    "color: #FFF;}" +
                    "a {" +
                    "color: #FFF;}" +
                    ".chapterHeader {" +
                    "color: #FFF;" +
                    "background-color: #000;}" +
                    ".chapterHeader .translation{" +
                    "color: #FFF;" +
                    "background-color: #000;}" +
                    ".chapterHeader .count{" +
                    "color: #FFF;" +
                    "background-color: #000;}" +
                    "</style>";
            this.isNightModeOn = true;
        }
        else {
            webView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            CSS = "<style rel = \"stylesheet\" type = \"text/css\">" +
                    "body{" +
                    "color: #000;}" +
                    "a {" +
                    "color: #000;}" +
                    ".chapterHeader {" +
                    "color: #000;" +
                    "background-color: #FFF;}" +
                    ".chapterHeader .translation{" +
                    "color: #000;" +
                    "background-color: #FFF;}" +
                    ".chapterHeader .count{" +
                    "color: #000;" +
                    "background-color: #FFF;}" +
                    "</style>";
            this.isNightModeOn = false;
        }
        if(currentChapter != -1) {
            addCSSToXML(CSS, getCurrentChapterPath());
            webView.reload();
        }
        for(int i = 0; i < contents.size(); i++) {
            String path = getUnzippedDirectory() + "/" +
                    getContentDir(new File(getUnzippedDirectory()).list()) + "/" +
                    contents.get(i);
            if (currentChapter != -1) {
                if (path == getCurrentChapterPath()) {
                    continue;
                }
            }
            addCSSToXML(CSS, path);
        }
    }

    public String getAnnotationFilePath() {
        return getUnzippedDirectory() + "/" + getContentDir(new File(getUnzippedDirectory()).list()) + "/" + "annotations.xml";
    }

    public String getBookmarkFilePath() {
        return getUnzippedDirectory() + "/" + getContentDir(new File(getUnzippedDirectory()).list()) + "/" + "bookmarks.xml";
    }

    public void addAnnotation(String content) {
        String path = getAnnotationFilePath();
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Element rootElement, noteElement;

            Document doc;
            if(new File(path).exists() == false) {
                doc = docBuilder.newDocument();
                rootElement = doc.createElement("annotation");
                doc.appendChild(rootElement);
            }
            else {
                doc = docBuilder.parse(new File(path));
                rootElement = (Element) doc.getElementsByTagName("annotation").item(0);
            }

            String id = getCurrentChapterID();
            if(id == null) {
                Toast.makeText(context, "Annotation could not be added.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(rootElement == null) {
                Log.d("Annotaition", "null root");
            }
            noteElement = doc.createElement(id);
            noteElement.setTextContent(content);
            rootElement.appendChild(noteElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));
            transformer.transform(source, result);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getAnnotationForCurrentChapter() {
        String id = getCurrentChapterID();
        String path = getAnnotationFilePath();
        if(new File(path).exists() == false) {
            return "No Annotations have been created for this book.";
        }
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(path));
            StringBuilder content = new StringBuilder("");

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName(id);
            if(nList.getLength() == 0) {
                return "No Annotations have been created for this chapter.";
            }
            for(int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    content.append(element.getTextContent());
                }
                content.append("\n----------\n");
            }
            return String.valueOf(content);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "No Annotations have been created for this chapter.";
    }

    public void addBookmark() {
        String path = getBookmarkFilePath();
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Element rootElement, noteElement;

            Document doc;
            if(new File(path).exists() == false) {
                doc = docBuilder.newDocument();
                rootElement = doc.createElement("Bookmark");
                doc.appendChild(rootElement);
            }
            else {
                doc = docBuilder.parse(new File(path));
                rootElement = (Element) doc.getElementsByTagName("Bookmark").item(0);
            }

            int id = currentChapter;
            Log.v("current", String.valueOf(id));
            final NavigationView navigationView = (NavigationView)view.findViewById(R.id.nav_view);
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.getItem(id);
            Log.v("item", String.valueOf(item.getItemId()));
            item.setIcon(R.drawable.ic_turned_in);

            if(rootElement == null) {
                Log.d("Bookmark", "null root");
            }
            noteElement = doc.createElement("Chapter");
            rootElement.appendChild(noteElement);

            Attr attr = doc.createAttribute("id");
            attr.setValue(String.valueOf(id));
            noteElement.setAttributeNode(attr);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));
            transformer.transform(source, result);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int[] getBookmarks(List<String> contentList) {
        int[] array = new int[contentList.size()];
        String path = getBookmarkFilePath();
        if(new File(path).exists() == false) {
           return null;
        }
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(path));
            StringBuilder content = new StringBuilder("");

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("Chapter");

            for(int i = 0; i < contentList.size(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    int pos = Integer.parseInt(eElement.getAttribute("id"));
                    if(pos == i) {
                        array[i] = pos;
                    }else {
                        array[i] = -1;
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;
    }
    public void highlightDocument(String data) {
        int id = currentChapter;
        String chapterName = contents.get(id);
        Log.v("highlight", data);
        String notification = parseCurrentChapter(chapterName, data);
        if(notification == "highlighted") {
             open(context, view, true);
        }
        Log.v("highlight", notification);
    }

    public String parseCurrentChapter(String chapterName, String highlighted) {
        String chapterPath = getUnzippedDirectory() + "/" + getContentDir(new File(getUnzippedDirectory()).list()) + "/" + chapterName;
        BufferedReader reader = null;
        PrintWriter out = null;
        try {
            reader = new BufferedReader(new FileReader(chapterPath));
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            String content = stringBuilder.toString();
            String finalcontent = content;
            String[] firstSecondhalf = content.split(highlighted, 2);
            if(firstSecondhalf.length == 2) {
                finalcontent = firstSecondhalf[0] + "<span class=\"highlighted\">" + highlighted + "</span>" + firstSecondhalf[1];
            }else if(firstSecondhalf.length == 1) {
                finalcontent = content;
            }
            String finalFinalcontent;
            if(finalcontent.contains("<style>")) {
                String[] twohalves = finalcontent.split("</style>", 2);
                finalFinalcontent = twohalves[0]+".highlighted {background-color:yellow;}"+"\n</style>\n"+twohalves[1];
            }else {
                String[] twohalves = finalcontent.split("</head>", 2);
                finalFinalcontent = twohalves[0]+"\n<style>\n.highlighted {background-color:yellow;}\n</style>\n</head>\n"+twohalves[1];
            }
            //Log.v("highlighted", finalFinalcontent);
            out = new PrintWriter(new BufferedWriter(new FileWriter(chapterPath)));
            out.print(finalFinalcontent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                out.close();
            }
        }
        return "highlighted";
    }

    public void addHighlightToCSS() {
        String CSSDir = getCSSDirectory();
        File file = new File(CSSDir);
        if(!file.exists()) {
            Toast.makeText(context, "Cannot be highlighted!", Toast.LENGTH_SHORT).show();
        }else {
            String[] cddFiles = file.list();
            for(String css : cddFiles) {
                if(css.endsWith(".css")) {
                    Log.v("cssfiles", css);
                    BufferedWriter bw = null;

                    try {
                        bw = new BufferedWriter(new FileWriter(CSSDir+"/"+css, true));
                        bw.newLine();
                        bw.write(".highlighted {background-color: yellow;}");
                        bw.newLine();
                        bw.flush();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    } finally {
                        if (bw != null) try {
                            bw.close();
                        } catch (IOException ioe2) {
                        }
                    }
                }
            }
        }
    }

    public void searchPatternInDoc(String searchString) {
        stringToBeSearched = searchString;
        focusSearchedString();
    }

    public void focusSearchedString() {
        WebView mWebView = (WebView) view.findViewById(R.id.webview);
        mWebView.findAllAsync(stringToBeSearched.toString());

        try{
            Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
            m.invoke(mWebView, true);
        }catch(Exception ignored){}
    }
}
