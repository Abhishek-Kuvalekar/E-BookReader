package se.coep.org.in.e_bookreader;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class FileRendererActivity extends AppCompatActivity {
    private String fileName;
    private boolean immersiveVisibilityFlag = true;
    private DrawerLayout mDrawerLayout;
    private boolean isDrawerPressed = false;
    private EpubFile file;

    //private PDFView pdfView;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_renderer_pdf);
        ActionBar actionbar = getSupportActionBar();
        Drawable mDrawable = ContextCompat.getDrawable(this, R.drawable.ic_menu);;
        actionbar.setHomeAsUpIndicator(mDrawable);
        actionbar.setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        fileName = getIntent().getStringExtra(FileChooserActivity.FILE_NAME);

        if (fileName.endsWith("epub")) {
            file = new EpubFile(fileName, this, this.getWindow().getDecorView());
            String ncxFilePath = file.getNcxFilePath();
            file.parse(ncxFilePath);
            file.open(this, this.getWindow().getDecorView(), false);
            ContentNavigation nav = new ContentNavigation("epub", this, this.getWindow().getDecorView());
            nav.addContent(file.getContentFile(), file);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    if(!isDrawerPressed) {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                        isDrawerPressed = true;

                    }
                    else {
                        mDrawerLayout.closeDrawers();
                        isDrawerPressed = false;
                    }
                    return true;

                case R.id.optionsButton:
                    final Dialog optionsDialog = new Dialog(this);
                    optionsDialog.setContentView(R.layout.options_dialog);
                    optionsDialog.getWindow().setGravity(Gravity.RIGHT | Gravity.TOP);
                    optionsDialog.show();
                    final TextView fontSize = (TextView) optionsDialog.findViewById(R.id.font_size_options_dialog);
                    fontSize.setText(Integer.toString(file.getFontSize()));
                    ImageButton fontPlus = (ImageButton) optionsDialog.findViewById(R.id.plus_button_options_dialog);
                    ImageButton fontMinus = (ImageButton) optionsDialog.findViewById(R.id.minus_button_options_dialog);
                    fontPlus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            file.changeFontSize(true);
                            fontSize.setText(Integer.toString(file.getFontSize()));
                        }
                    });
                    fontMinus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            file.changeFontSize(false);
                            fontSize.setText(Integer.toString(file.getFontSize()));
                        }
                    });
                    return true;
            }
            return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, FileChooserActivity.class);
        startActivity(intent);
        finish();
    }
}
