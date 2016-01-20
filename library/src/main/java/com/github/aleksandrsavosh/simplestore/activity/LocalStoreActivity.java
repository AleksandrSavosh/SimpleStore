package com.github.aleksandrsavosh.simplestore.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.github.aleksandrsavosh.simplestore.R;
import com.github.aleksandrsavosh.simplestore.SimpleStoreManager;
import com.github.aleksandrsavosh.simplestore.sqlite.SimpleStoreUtil;
import com.github.aleksandrsavosh.simplestore.sqlite.SQLiteHelper;

import java.util.List;

public class LocalStoreActivity extends Activity {

    SQLiteHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_store_activity);

        final WebView webView = (WebView) findViewById(R.id.local_store_web_view);

        helper = SimpleStoreManager.instance.getSqLiteHelper();

        if(helper == null){
            return;
        }

        List<String> tableNames = SimpleStoreUtil.getTableNames(helper.getReadableDatabase());

        Spinner spinner = (Spinner) findViewById(R.id.local_store_spinner);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tableNames));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tableName = (String) parent.getAdapter().getItem(position);
                List<List<String>> date = SimpleStoreUtil.getTableData(helper.getReadableDatabase(), tableName);

                StringBuilder htmlBuilder = new StringBuilder();
                htmlBuilder.append("<table>");
                htmlBuilder.append("<tr>");
                for(String name : date.get(0)){
                    htmlBuilder.append("<td>");
                    htmlBuilder.append(name);
                    htmlBuilder.append("<td>");
                }
                htmlBuilder.append("</tr>");

                for(int i = 1; i < date.size(); i++) {
                    htmlBuilder.append("<tr>");
                    for (String data : date.get(i)) {
                        htmlBuilder.append("<td>");
                        htmlBuilder.append(data);
                        htmlBuilder.append("<td>");
                    }
                    htmlBuilder.append("</tr>");
                }


                htmlBuilder.append("</table>");

                webView.loadData(htmlBuilder.toString(), "text/html", null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


}
