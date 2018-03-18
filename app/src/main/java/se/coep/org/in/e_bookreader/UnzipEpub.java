package se.coep.org.in.e_bookreader;

import android.os.Environment;
import android.util.Log;

/**
 * Created by dell on 18/3/18.
 */

public class UnzipEpub {

    public UnzipEpub(String filename) {
        String zipFile = filename;
        String unzipLocation = "/storage/emulated/0/Download/unzipped/";

        Log.d("Decompress", "here");

        Decompress d = new Decompress(zipFile, unzipLocation);
        d.unzip();
    }
}
