package se.coep.org.in.e_bookreader;

import android.content.Context;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.interfaces.ECPublicKey;
import java.util.List;

/**
 * Created by dell on 2/4/18.
 */

public class ContentNavigation {
    private String fileType;
    private Context context;
    private View view;
    private String ncxFile;
    private EpubFile file;
    private int previousItem = 0;

    public ContentNavigation(String fileType, Context context, View view) {
        this.fileType = fileType;
        this.context = context;
        this.view = view;
    }

    public void addContent(String ncxFile, EpubFile file) {
        this.ncxFile = ncxFile;
        this.file = file;
        if(fileType == "epub") {
            addEpubContentToNavigation();
        }else if(fileType == "mobi") {
            addMobiContentToNavigation();
        }
    }

    public void addEpubContentToNavigation() {
        final NavigationView navigationView = (NavigationView)view.findViewById(R.id.nav_view);
        final DrawerLayout mDrawerLayout;
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        previousItem = 0;
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        navigationView.getMenu().getItem(previousItem).setChecked(false);
                        previousItem = menuItem.getItemId();
                        mDrawerLayout.closeDrawers();
                        file.setCurrentChapter(menuItem.getItemId());
                        file.open(context, view, true);
                        return true;
                    }
                });


        Menu menu = navigationView.getMenu();
        List<String> contentList = file.parseForContent(file.getNcxFilePath());
        int[] bookmarkedChapters = file.getBookmarks(contentList);
        if(bookmarkedChapters == null) {
            //Toast.makeText(context, "No Bookmarks yet!", Toast.LENGTH_SHORT).show();
            for (int i = 0; i < contentList.size(); i++) {
                MenuItem item = menu.add(1, i, 0, contentList.get(i));
            }
        }else {
            for (int i = 0; i < contentList.size(); i++) {
                MenuItem item = menu.add(1, i, 0, contentList.get(i));
                if (bookmarkedChapters[i] == 1) {
                    item.setIcon(R.drawable.ic_turned_in);
                }
            }
        }
    }

    private void addMobiContentToNavigation() {

    }
}
