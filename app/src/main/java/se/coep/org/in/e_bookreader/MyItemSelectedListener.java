package se.coep.org.in.e_bookreader;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;


public class MyItemSelectedListener implements AdapterView.OnItemSelectedListener {
    EpubFile file;
    int selectionPosition;

    public MyItemSelectedListener(EpubFile file, int fontFamilyPosition) {
        this.file = file;
        this.selectionPosition = fontFamilyPosition;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        file.changeFontStyle(parent.getItemAtPosition(position).toString());
        this.selectionPosition = position;
        file.setFontFamilyPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public int getSelectedPosition() {
        return this.selectionPosition;
    }
}
