package crop.computer.askey.sqlitepractice;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import crop.computer.askey.sqlitepractice.model.NewsModel;

public class MainActivity extends AppCompatActivity
        implements NewsModel.OnDataBaseCallback {

    NewsModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        model = new NewsModel(this);

        model.deleteAllData(this);

        putRSSs();

        findViewById(R.id.btnPut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                put();
            }
        });

        findViewById(R.id.btnQuery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query();
            }
        });

        findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });

        findViewById(R.id.btnUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });
    }

    @Override
    protected void onDestroy() {
        model.close();
        super.onDestroy();
    }

    private void putRSSs() {
        model.put("title1", "subtitle1", this);
        model.put("title2", "subtitle2", this);
        model.put("title2", "subtitle3", this);
        model.put("title3", "subtitle4", this);
    }

    private void put() {
        EditText edtTitle = findViewById(R.id.edtTitleForPut);
        String title = edtTitle.getText().toString();

        EditText edtSubtitle = findViewById(R.id.edtSubtitleForPut);
        String subtitle = edtSubtitle.getText().toString();

        if(!title.isEmpty()) {
            model.put(title, subtitle, this);
        }else {
            Toast.makeText(this, "No date to put", Toast.LENGTH_SHORT).show();
        }
    }

    private void query() {
        EditText edtTitleForQuery = findViewById(R.id.edtTitleForQuery);
        String title = edtTitleForQuery.getText().toString();

        if(title.isEmpty()) {
            model.queryAll(this);
        }else {
            model.queryByTitle(title, this);
        }
    }

    private void delete() {
        EditText edtTitleForDelete = findViewById(R.id.edtTitleForDelete);
        String title = edtTitleForDelete.getText().toString();

        if(!title.isEmpty()) {
            if(title.equals("ALL")) {
                model.deleteAllData(this);
            }else {
                model.deleteByTitle(title, this);
            }
        }
    }

    private void update() {
        EditText edtOldTitle = findViewById(R.id.edtOldTitleForUpdate);
        String oldTitle = edtOldTitle.getText().toString();

        EditText edtNewTitle = findViewById(R.id.edtNewTitleForUpdate);
        String newTitle = edtNewTitle.getText().toString();

        if(!oldTitle.isEmpty()) {
            model.updateTitle(oldTitle, newTitle, this);
        }else {
            Toast.makeText(this, "No this title: "+oldTitle, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSuccess(Object data) {
        Log.w("MainActivity", data.toString());

        TextView textView = findViewById(R.id.txtResult);
        textView.setTextColor(Color.BLUE);

        if(data instanceof ArrayList) {
            ArrayList list = (ArrayList) data;
            textView.setText("");
            for(Object o: list) {
                textView.append(o.toString());
                textView.append("\n");
            }
        }else {
            textView.setText(data.toString());
        }
    }

    @Override
    public void onFail(String error) {
        Log.w("MainActivity", error);

        TextView textView = findViewById(R.id.txtResult);
        textView.setTextColor(Color.RED);
        textView.setText(error);
    }
}
