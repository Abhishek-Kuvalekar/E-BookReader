package se.coep.org.in.e_bookreader;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

public class FileRendererActivity extends AppCompatActivity {
    private String fileName;
    private boolean immersiveVisibilityFlag = true;
    private DrawerLayout mDrawerLayout;
    private boolean isDrawerPressed = false;
    private EpubFile file;

    public static String currentFont = "Default";
    private String pasteData;
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
            file.unzip();
            String ncxFilePath = file.getNcxFilePath();
            file.parse(ncxFilePath);
            file.addHighlightToCSS();
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

                    Spinner fontFamily = (Spinner) optionsDialog.findViewById(R.id.font_family_spinner_options_dialog);
                    MyItemSelectedListener myItemSelectedListener = new MyItemSelectedListener(file, file.getFontFamilyPosition());
                    fontFamily.setOnItemSelectedListener(myItemSelectedListener);
                    fontFamily.setSelection(file.getFontFamilyPosition());

                    final Switch nightMode = (Switch) optionsDialog.findViewById(R.id.night_mode_switch_options_dialog);
                    nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(nightMode.isChecked()) {
                                file.switchNightMode(true);
                            }
                            else {
                                file.switchNightMode(false);
                            }
                        }
                    });
                    nightMode.setChecked(file.getNightModeState());

                    SeekBar brightness = (SeekBar) optionsDialog.findViewById(R.id.brightness_seekbar_options_dialog);
                    brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            file.adjustBrightness(getWindow(), (float) (progress/100.0));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    brightness.setProgress((int)(file.getBrightness() * 100));

                    final TextView annotate = (TextView) optionsDialog.findViewById(R.id.annotate_options_dialog);
                    annotate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Dialog annotateDialog = new Dialog(FileRendererActivity.this);
                            annotateDialog.setContentView(R.layout.annotate_dialog);
                            annotateDialog.show();

                            TextView cancel = (TextView) annotateDialog.findViewById(R.id.cancel_button_annotate_dialog);
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    annotateDialog.hide();
                                }
                            });

                            TextView save = (TextView) annotateDialog.findViewById(R.id.save_button_annotate_dialog);
                            save.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    EditText text = (EditText) annotateDialog.findViewById(R.id.annotation_editext_annotate_dialog);
                                    String note = String.valueOf(text.getText());
                                    annotateDialog.hide();
                                    file.addAnnotation(note);
                                    //Toast.makeText(FileRendererActivity.this, note, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                    TextView viewAnnotations = (TextView) optionsDialog.findViewById(R.id.view_annotations_options_dialog);
                    viewAnnotations.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Dialog showAnnotationsDialog = new Dialog(FileRendererActivity.this);
                            showAnnotationsDialog.setContentView(R.layout.view_annotation_dialog);
                            showAnnotationsDialog.show();

                            TextView note = (TextView) showAnnotationsDialog.findViewById(R.id.textView_view_annotaion_dialog);
                            note.setText(file.getAnnotationForCurrentChapter());

                            TextView ok = (TextView) showAnnotationsDialog.findViewById(R.id.ok_button_view_annotation_dialog);
                            ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showAnnotationsDialog.hide();
                                }
                            });
                        }
                    });

                    TextView bookmark = (TextView) optionsDialog.findViewById(R.id.bookmark_options_dialog);
                    bookmark.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.v("bookmark", "bookmarked");
                            file.addBookmark();
                            Toast.makeText(FileRendererActivity.this, "Chapter Bookmarked!", Toast.LENGTH_SHORT).show();
                            optionsDialog.hide();
                        }
                    });

                    final TextView search = (TextView) optionsDialog.findViewById(R.id.search_options_dialog);
                    search.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final Dialog searchDialog = new Dialog(FileRendererActivity.this);
                            searchDialog.setContentView(R.layout.view_search_dialog);
                            searchDialog.show();

                            TextView cancel = (TextView) searchDialog.findViewById(R.id.cancel_button_search_dialog);
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    searchDialog.hide();
                                }
                            });

                            TextView save = (TextView) searchDialog.findViewById(R.id.search_button_search_dialog);
                            save.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    EditText text = (EditText) searchDialog.findViewById(R.id.search_editext_dialog);
                                    String note = String.valueOf(text.getText());
                                    searchDialog.hide();
                                    optionsDialog.hide();

                                    file.searchPatternInDoc(note);

                                    LinearLayout mainLayout = (LinearLayout)findViewById(R.id.fileShower);

                                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                    View popupView = inflater.inflate(R.layout.popup_window, null);

                                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                    //boolean focusable = true; // lets taps outside the popup also dismiss it
                                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, false);

                                    // show the popup window
                                    popupWindow.showAtLocation(mainLayout, Gravity.BOTTOM, 0, 10);

                                    // dismiss the popup window when touched
                                    popupView.setOnTouchListener(new View.OnTouchListener() {
                                        @Override
                                        public boolean onTouch(View v, MotionEvent event) {
                                            popupWindow.dismiss();
                                            file.stringToBeSearched = null;
                                            file.open(FileRendererActivity.this, FileRendererActivity.this.getWindow().getDecorView(), false);
                                            return true;
                                        }
                                    });
                                }
                            });

                        }
                    });

                    TextView textToSpeech = (TextView) optionsDialog.findViewById(R.id.text_to_speech_options_dialog);
                    textToSpeech.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Dialog ttsDialog = new Dialog(FileRendererActivity.this);
                            ttsDialog.setContentView(R.layout.view_tts_dialog);
                            ttsDialog.show();

                            final SeekBar speed = (SeekBar) ttsDialog.findViewById(R.id.speed_tts_dialog);
                            speed.setProgress(50);

                            final SeekBar pitch = (SeekBar) ttsDialog.findViewById(R.id.pitch_tts_dialog);
                            pitch.setProgress(50);

                            final TextView start = (TextView) ttsDialog.findViewById(R.id.start_tts_dialog);
                            start.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    start.setEnabled(false);
                                    file.startTTS((float)(speed.getProgress()/50.0), (float)(pitch.getProgress()/50.0));
                                    speed.setEnabled(false);
                                    pitch.setEnabled(false);
                                }
                            });

                            TextView stop = (TextView) ttsDialog.findViewById(R.id.stop_tts_dialog);
                            stop.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    file.stopTTS();
                                    start.setEnabled(true);
                                    speed.setEnabled(true);
                                    pitch.setEnabled(true);
                                }
                            });
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
        file.save();
        Intent intent = new Intent(this, FileChooserActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onActionModeStarted(final ActionMode mode) {
        super.onActionModeStarted(mode);

        MenuInflater menuInflater = mode.getMenuInflater();
        final Menu menu = mode.getMenu();

        //menu.clear();
        //menuInflater.inflate(R.menu.context_menu, menu);

        menu.add(0, 5, 0, "Highlight");

        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Toast.makeText(FileRendererActivity.this, "Text Highlighted!", Toast.LENGTH_SHORT).show();
                final WebView webview = (WebView) findViewById(R.id.webview);
                webview.getSettings().setJavaScriptEnabled(true);
                webview.evaluateJavascript("(function() {return window.getSelection().toString()})()",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                Log.v("select", s);
                                pasteData = s;
                                String data = pasteData.substring(1, pasteData.length()-1);
                                file.highlightDocument(data);
                               /* Log.v("select", data);
                                webview.findAllAsync(data);
                                Method m = null;
                                try {
                                    m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                                    m.invoke(webview, true);
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }*/
                            }
                        }
                );
                mode.finish();
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        file.stopTTS();
    }

}
