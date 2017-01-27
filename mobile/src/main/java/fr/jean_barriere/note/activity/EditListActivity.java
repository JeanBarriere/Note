package fr.jean_barriere.note.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import fr.jean_barriere.note.manager.DataManager;
import fr.jean_barriere.note.item.List;
import fr.jean_barriere.note.item.ListItem;
import fr.jean_barriere.note.R;
import fr.jean_barriere.note.adapter.ListItemArrayAdapter;

public class EditListActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_view_list)
    Toolbar toolbar;

    @BindView(R.id.list_item_list_title)
    MaterialEditText list_item_list_title;

    @BindView(R.id.add_list_item)
    MaterialEditText add_list_item;

    @BindView(R.id.list_item_card_list_values)
    ListView list_item_card_list_values;

    private Calendar myCalendar;
    private Menu mMenu;
    private DatePickerDialog.OnDateSetListener date;
    private TimePickerDialog.OnTimeSetListener time;
    private Boolean hasReminder = false;
    private ListItemArrayAdapter adapter;
    private ArrayList<ListItem> items;
    private List self;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("calendar", myCalendar);
        outState.putBoolean("hasReminder", hasReminder);
        outState.putSerializable("list", self);
        outState.putSerializable("items", items);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        myCalendar = (Calendar) savedInstanceState.getSerializable("calendar");
        hasReminder = savedInstanceState.getBoolean("hasReminder");
        self = (List) savedInstanceState.getSerializable("list");
        items = (ArrayList<ListItem>) savedInstanceState.getSerializable("items");
        adapter = new ListItemArrayAdapter(this, items, self);
        list_item_card_list_values.setAdapter(adapter);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                int totalHeight = 0;
                for (int i = 0; i < adapter.getCount(); i++) {
                    View listItem = adapter.getView(i, null, list_item_card_list_values);
                    listItem.measure(0, 0);
                    totalHeight += listItem.getMeasuredHeight();
                }

                ViewGroup.LayoutParams params = list_item_card_list_values.getLayoutParams();
                params.height = totalHeight + (list_item_card_list_values.getDividerHeight() * (adapter.getCount() - 1));
                list_item_card_list_values.setLayoutParams(params);
                list_item_card_list_values.requestLayout();
            }
        });
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        self = (List) getIntent().getExtras().getSerializable("list");
        items = new ArrayList<>(self.getValues());
        adapter = new ListItemArrayAdapter(this, items, self);
        list_item_card_list_values.setAdapter(adapter);

        add_list_item.setImeOptions(EditorInfo.IME_ACTION_DONE);

        setTitle(getString(R.string.edit_list_title));
        list_item_list_title.setText(self.getTitle());
        if (self.isReminder())
            myCalendar = self.getDueDate();
        else
            myCalendar = Calendar.getInstance();
        hasReminder = self.isReminder();

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                new TimePickerDialog(EditListActivity.this, time, myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true).show();
            }
        };

        time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                myCalendar.set(Calendar.HOUR_OF_DAY, hour);
                myCalendar.set(Calendar.MINUTE, minute);
                if (myCalendar.getTime().before(Calendar.getInstance().getTime()))
                    Toast.makeText(EditListActivity.this, getString(R.string.provide_valid_date_time), Toast.LENGTH_LONG).show();
                else
                    updateRemind(true);
            }
        };

        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                int totalHeight = 0;
                for (int i = 0; i < adapter.getCount(); i++) {
                    View listItem = adapter.getView(i, null, list_item_card_list_values);
                    listItem.measure(0, 0);
                    totalHeight += listItem.getMeasuredHeight();
                }

                ViewGroup.LayoutParams params = list_item_card_list_values.getLayoutParams();
                params.height = totalHeight + (list_item_card_list_values.getDividerHeight() * (adapter.getCount() - 1));
                list_item_card_list_values.setLayoutParams(params);
                list_item_card_list_values.requestLayout();
            }
        });

        adapter.notifyDataSetChanged();
    }

    @OnEditorAction(R.id.add_list_item)
    public boolean check(TextView textView, int id, KeyEvent keyEvent) {
        if (((id == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_UP) || id == EditorInfo.IME_ACTION_DONE) && textView.getText().toString().trim().length() > 0) {
            items.add(new ListItem(textView.getText().toString(), false));
            adapter.notifyDataSetChanged();
            textView.setText("");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            supportFinishAfterTransition();
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
        super.onBackPressed();
    }

    public void updateRemind(boolean addRemind) {
        hasReminder = addRemind;
        mMenu.findItem(R.id.action_item_alarm_delete).setVisible(addRemind);
        mMenu.findItem(R.id.action_item_alarm_add).setVisible(!addRemind);
    }

    public String getDueDate() {
        String myFormat = "dd/MM/yyyy HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);
        return sdf.format(myCalendar.getTime());
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        mMenu = menu;

        menu.findItem(R.id.action_item_delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new AlertDialog.Builder(EditListActivity.this)
                        .setTitle(getString(R.string.delete_list_title))
                        .setMessage(getString(R.string.delete_list_message))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                self.deleteNotification();
                                DataManager.deleteList(self);
                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                        .setIcon(R.drawable.ic_action_delete)
                        .show();
                return true;
            }
        });

        menu.findItem(R.id.action_item_valid).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (list_item_list_title.getText().toString().trim().length() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cannot_update_empty_list), Toast.LENGTH_LONG).show();
                    return false;
                }
                self.setTitle(list_item_list_title.getText().toString());
                self.setValues(items);
                if (hasReminder)
                    self.setDueDate(myCalendar);
                else
                    self.removeDueDate();
                self.updateNotification();
                self.updateModificationDateTime();
                DataManager.saveList(self);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                return true;
            }
        });

        menu.findItem(R.id.action_item_alarm_add).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new DatePickerDialog(EditListActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                return true;
            }
        });

        menu.findItem(R.id.action_item_alarm_delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new AlertDialog.Builder(EditListActivity.this)
                        .setTitle(getString(R.string.delete_due_date_title))
                        .setMessage(getString(R.string.delete_due_date_message, getDueDate()))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                updateRemind(false);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                        .setIcon(R.drawable.ic_action_alarm_add_blue)
                        .show();
                return true;
            }
        });
        updateRemind(hasReminder);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        updateRemind(hasReminder);
        return super.onPrepareOptionsMenu(menu);
    }
}
