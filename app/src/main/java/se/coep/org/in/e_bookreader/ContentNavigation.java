package se.coep.org.in.e_bookreader;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        // close drawer when item is tapped
                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        file.setCurrentChapter(menuItem.getItemId());
                        file.open(context, view);
                        return true;
                    }
                });


        Menu menu = navigationView.getMenu();
        List<String> contentList = file.parseForContent(file.getNcxFilePath());
        for(int i=0; i<contentList.size(); i++) {
            menu.add(1, i, 0, contentList.get(i));
        }
    }

    private void addMobiContentToNavigation() {

    }
}
