package se.coep.org.in.e_bookreader;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

public class MyItemSelectedListener implements AdapterView.OnItemSelectedListener {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        FileRendererActivity.currentFont = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), FileRendererActivity.currentFont, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
