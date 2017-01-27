package fr.jean_barriere.note.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.jean_barriere.note.manager.DataManager;
import fr.jean_barriere.note.item.Note;
import fr.jean_barriere.note.R;

public class EditNoteActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_view_note)
    Toolbar toolbar;

    @BindView(R.id.note_content)
    MaterialEditText view_note_content;

    private Calendar myCalendar;
    private Menu mMenu;
    private DatePickerDialog.OnDateSetListener date;
    private TimePickerDialog.OnTimeSetListener time;
    private Boolean hasReminder = false;
    private Note self;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("calendar", myCalendar);
        outState.putBoolean("hasReminder", hasReminder);
        outState.putSerializable("note", self);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        myCalendar = (Calendar) savedInstanceState.getSerializable("calendar");
        hasReminder = savedInstanceState.getBoolean("hasReminder");
        self = (Note) savedInstanceState.getSerializable("note");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        self = (Note) getIntent().getExtras().getSerializable("note");
        setTitle(getString(R.string.edit_note_title));
        view_note_content.setText(self.getValue());
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
                new TimePickerDialog(EditNoteActivity.this, time, myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true).show();
            }
        };

        time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                myCalendar.set(Calendar.HOUR_OF_DAY, hour);
                myCalendar.set(Calendar.MINUTE, minute);
                if (myCalendar.getTime().before(Calendar.getInstance().getTime()))
                    Toast.makeText(EditNoteActivity.this, getString(R.string.provide_valid_date_time), Toast.LENGTH_LONG).show();
                else
                    updateRemind(true);
            }
        };
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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

        menu.findItem(R.id.action_item_valid).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (view_note_content.getText().toString().trim().length() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cannot_update_empty_note), Toast.LENGTH_LONG).show();
                    return false;
                }
                self.setValue(view_note_content.getText().toString());
                self.updateModificationDateTime();
                if (hasReminder)
                    self.setDueDate(myCalendar);
                else
                    self.removeDueDate();
                self.updateNotification();
                DataManager.saveNote(self);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                return true;
            }
        });

        menu.findItem(R.id.action_item_delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new AlertDialog.Builder(EditNoteActivity.this)
                        .setTitle(getString(R.string.delete_note_title))
                        .setMessage( getString(R.string.delete_note_message))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                self.deleteNotification();
                                DataManager.deleteNote(self);
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

        menu.findItem(R.id.action_item_alarm_add).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new DatePickerDialog(EditNoteActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                return true;
            }
        });

        menu.findItem(R.id.action_item_alarm_delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new AlertDialog.Builder(EditNoteActivity.this)
                        .setTitle(getString(R.string.delete_due_date_title))
                        .setMessage( getString(R.string.delete_due_date_message, getDueDate()))
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
