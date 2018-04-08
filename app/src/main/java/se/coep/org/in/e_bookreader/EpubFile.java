package se.coep.org.in.e_bookreader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

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

    public EpubFile(String fileName, Context context, View view) {
        this.context = context;
        this.fileName = fileName;
        this.view = view;
        this.fontFamilyPosition = 0;
        webView = (WebView) view.findViewById(R.id.webview);
        currentChapter = 0;
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
        unzip();
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
                }
                else {
                    Toast.makeText(context, "End of the book", Toast.LENGTH_SHORT).show();
                }
            }
            public void onSwipeRight() {
                if(currentChapter != 0) {
                    currentChapter--;
                    webView.loadUrl(String.valueOf(Uri.fromFile(new File(getCurrentChapterPath()))));
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
        //String path = getCSSDirectory();
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

    public String[] getContentOfNcxFile(String fileToBeParsed) {
        BufferedReader in = null;
        String[] stringArr;
        try {
            in = new BufferedReader(    new InputStreamReader( new FileInputStream(fileToBeParsed)));
            String str;

            List<String> list = new ArrayList<String>();
            while((str = in.readLine()) != null){
                list.add(str);
            }
            stringArr = list.toArray(new String[0]);
            return stringArr;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] addAttributeForBookmark(String ncxFilePath) {
        String[] array = getContentOfNcxFile(ncxFilePath);
        String [] newArray = new String[array.length];
        for(int i=0; i<array.length; i++) {
            if(array[i].contains("<navPoint")) {
               newArray[i] = array[i].replaceFirst("<navPoint", "<navPoint bookmark=1");
            }else {
                newArray[i] = array[i];
            }
            Log.v("content", " "+newArray[i]);
        }

        FileWriter fw = null;
        try {
            File file = new File(unzipLocation.toString() + "/unzipped/test.xml");
            fw = new FileWriter(file);
            for (int i = 0; i < newArray.length; i++) {
                fw.write(newArray[i] + "\n");
            }
            fw.close();
            Log.v("file", "written");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newArray;
    }

    public void makeFile(String[] buffer) {
        for(int i = 0; i<buffer.length; i++) {
            Log.v("check", ""+buffer[i]);
        }

        File temp = new File(getUnzippedDirectory()+"/test.xml");
        FileWriter fw = null;
        try {
            fw = new FileWriter(temp);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(java.util.Arrays.toString(buffer));
            Log.v("file","written");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initializeForBookmark(String ncxFilePath) {
        String[] arr = addAttributeForBookmark(ncxFilePath);
        //makeFile(arr);
    }
}
