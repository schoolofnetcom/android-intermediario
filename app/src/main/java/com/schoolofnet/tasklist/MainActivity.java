package com.schoolofnet.tasklist;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.schoolofnet.tasklist.db.DbHelper;
import com.schoolofnet.tasklist.db.TaskDb;
import com.schoolofnet.tasklist.db.TaskEntry;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<String> tasks = new ArrayList<String>();
    private ListView listView;
    private ArrayAdapter arrayAdapter;
    private DbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DbHelper(this);

        listView = (ListView) findViewById(R.id.listView);
//        arrayAdapter = new ArrayAdapter(this, R.layout.item_task, R.id.textView, tasks);

//        listView.setAdapter(arrayAdapter);

        findTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
               return createTask();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void findTask() {
        SQLiteDatabase dbHelper = db.getReadableDatabase();

        String[] values = new String[]{TaskEntry._ID, TaskEntry.COL_TASK_TITLE};

        Cursor cursor = dbHelper.query(TaskEntry.TABLE, values, null, null, null, null, null);

        while(cursor.moveToNext()) {
            int id = cursor.getColumnIndex(TaskEntry.COL_TASK_TITLE);
            Log.d("Main Activity -> ", cursor.getString(id));

            if (tasks.size() > 0) {
                tasks.clear();
            }

            tasks.add(cursor.getString(id));
        }

        cursor.close();
        db.close();
    }

    public Boolean createTask() {
        final EditText taskText = new EditText(this);

        AlertDialog dialog = new AlertDialog
                                    .Builder(this)
                                    .setTitle("Add a new task")
                                    .setMessage("What do you want to do ? ")
                                    .setView(taskText)
                                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            String task = String.valueOf(taskText.getText());

                                            SQLiteDatabase dbHelper = db.getWritableDatabase();
                                            ContentValues values = new ContentValues();

                                            values.put(TaskEntry.COL_TASK_TITLE, task);
                                            dbHelper.insertWithOnConflict(TaskEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                            db.close();
                                            updateUI();

                                        }
                                    })
                                    .setNegativeButton("Close", null)
                                    .create();

        dialog.show();

        return true;
    }

    public void removeTask(View view) {
        View parent = (View) view.getParent();

        TextView taskTextView = (TextView) parent.findViewById(R.id.textView);
        String task = String.valueOf(taskTextView.getText());

        SQLiteDatabase dbHelper = db.getWritableDatabase();

        String[] values = new String[]{task};

        dbHelper.delete(TaskEntry.TABLE, TaskEntry.COL_TASK_TITLE + " = ? ", values);

        db.close();
        updateUI();
    }

    private void updateUI() {
        tasks = new ArrayList<>();

        SQLiteDatabase dbHelper = db.getReadableDatabase();

        String[] values = new String[]{TaskEntry._ID, TaskEntry.COL_TASK_TITLE};

        Cursor cursor = dbHelper.query(TaskEntry.TABLE, values, null, null, null, null, null);

        while(cursor.moveToNext()) {
            int id = cursor.getColumnIndex(TaskEntry.COL_TASK_TITLE);
            Log.d("Main Activity -> ", cursor.getString(id));

            if (tasks.size() > 0) {
                tasks.clear();
            }

            tasks.add(cursor.getString(id));
        }

        if (arrayAdapter == null) {
            arrayAdapter = new ArrayAdapter(this, R.layout.item_task, R.id.textView, tasks);
            listView.setAdapter(arrayAdapter);
        } else {
            arrayAdapter.clear();
            arrayAdapter.add(tasks);
            arrayAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }
}
