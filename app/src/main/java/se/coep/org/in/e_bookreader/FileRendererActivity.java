package se.coep.org.in.e_bookreader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


//import com.folioreader.util.FolioReader;
//import com.github.barteksc.pdfviewer.PDFView;


//import com.github.barteksc.pdfviewer.PDFView;

/*import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;*/


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class FileRendererActivity extends AppCompatActivity {
    private String fileName;
    private boolean immersiveVisibilityFlag = true;
    private DrawerLayout mDrawerLayout;
    private boolean isDrawerPressed = false;

    //private PDFView pdfView;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_renderer_pdf);
        ActionBar actionbar = getSupportActionBar();
        Drawable mDrawable = ContextCompat.getDrawable(this, R.drawable.ic_menu);;
        mDrawable.setColorFilter(new
                PorterDuffColorFilter(R.color.white,PorterDuff.Mode.SRC_ATOP));
        actionbar.setHomeAsUpIndicator(mDrawable);
        actionbar.setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        fileName = getIntent().getStringExtra(FileChooserActivity.FILE_NAME);

        /*pdfView = (PDFView) findViewById(R.id.pdfView);

        pdfView.fromFile(new File(fileName))
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(true)
                .enableDoubletap(true)
                /*.defaultPage(0)
                // allows to draw something on the current page, usually visible in the middle of the screen
                .onDraw(onDrawListener)
                // allows to draw something on all pages, separately for every page. Called only for visible pages
                .onDrawAll(onDrawListener)
                .onLoad(onLoadCompleteListener) // called after document is loaded and starts to be rendered
                .onPageChange(onPageChangeListener)
                .onPageScroll(onPageScrollListener)
                .onError(onErrorListener)
                .onPageError(onPageErrorListener)
                .onRender(onRenderListener) // called after document is rendered for the first time
                // called on single tap, return true if handled, false to toggle scroll handle visibility
                .onTap(onTapListener)
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                // spacing between pages in dp. To define spacing color, set view background
                .spacing(0)
                .linkHandler()
                .pageFitPolicy(FitPolicy.WIDTH)
                .load();*/
        if (fileName.endsWith("epub")) {
            EpubFile file = new EpubFile(fileName, this);
            String ncxFilePath = file.getNcxFilePath();
            file.parse(ncxFilePath);
            file.open(this, this.getWindow().getDecorView());
            ContentNavigation nav = new ContentNavigation("epub", this, this.getWindow().getDecorView());
            nav.addContent(file.getContentFile(), file);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!isDrawerPressed) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    isDrawerPressed = true;
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }else {;
            mDrawerLayout.closeDrawers();
            isDrawerPressed = false;
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, FileChooserActivity.class);
        startActivity(intent);
        finish();
    }
}
