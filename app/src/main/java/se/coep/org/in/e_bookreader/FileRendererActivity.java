package se.coep.org.in.e_bookreader;

import android.graphics.Canvas;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

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
    //private PDFView pdfView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_renderer_pdf);

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
        //final File file = new File(fileName);
        /*Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PDDocument document = PDDocument.load(file);
                    PDFTextStripper pdfStripper = new PDFTextStripper();
                    String text = pdfStripper.getText(document);
                    Log.d("FILE: ", text);
                    //Toast.makeText(FileRendererActivity.this, text, Toast.LENGTH_LONG).show();
                    document.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();*/
        WebView webView = (WebView) findViewById(R.id.webview);
        Log.d("FILE", fileName);
        webView.loadUrl(String.valueOf(Uri.fromFile(new File(fileName))));
    }
}