package se.coep.org.in.e_bookreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by dell on 7/4/18.
 */

public class Bookmarks {
    public Bookmarks() {
        File bookmarks_file = new File("bookmarks.xml");
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(bookmarks_file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
