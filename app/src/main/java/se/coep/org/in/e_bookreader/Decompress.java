package se.coep.org.in.e_bookreader;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Decompress {
    private String _zipFile;
    private String _location;

    public Decompress(String zipFile, String location) {
        _zipFile = zipFile;
        _location = location;
        File f = new File(_location);
        if(f.isDirectory()) {
            deleteRecursive(f);
        }
    }

    public void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public void unzip() {
        try  {
            File file = new File(_location);
            file.mkdir();
            ZipFile zipFile = new ZipFile(_zipFile);
            Enumeration files = zipFile.entries();
            while (files.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) files.nextElement();
                Log.v("Decompress", "ZipEntry: "+entry);
                Log.v("Decompress", "isDirectory: " + entry.isDirectory());

                if (entry.isDirectory()) {
                    if(entry.toString().endsWith(".epub_FILES/")) {
                        continue;
                    } else {
                        File file1 = new File(_location + entry.getName());
                        file1.mkdir();
                        Log.d("Decompress", "Create dir " + entry.getName());
                    }
                } else {
                    File f = new File(_location + entry.getName());
                    f.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(f);
                    InputStream is = zipFile.getInputStream(entry);
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    fos.close();
                    Log.d("Decompress", "Create File " + entry.getName());
                }
            }
        } catch(Exception e) {
            Log.e("Decompress", "unzip", e);
        }

    }
}
