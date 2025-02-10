package com.example.agenda;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agenda.database.EventDatabase;
import com.example.agenda.model.Event;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    private EditText etEventTitle, etEventDescription;
    private Button btnSelectDate, btnSelectStartTime, btnSelectEndTime, btnSaveEvent;
    private String selectedDate, selectedStartTime, selectedEndTime;
    private EventDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        db = EventDatabase.getInstance(this);

        etEventTitle = findViewById(R.id.etEventTitle);
        etEventDescription = findViewById(R.id.etEventDescription);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectStartTime = findViewById(R.id.btnSelectStartTime);
        btnSelectEndTime = findViewById(R.id.btnSelectEndTime);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);

        // Récupérer la date sélectionnée si elle vient du calendrier
        selectedDate = getIntent().getStringExtra("selectedDate");
        if (selectedDate != null) {
            btnSelectDate.setText(selectedDate);
        }

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectStartTime.setOnClickListener(v -> showTimePicker(true));
        btnSelectEndTime.setOnClickListener(v -> showTimePicker(false));

        btnSaveEvent.setOnClickListener(v -> saveEvent());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format(Locale.FRANCE, "%02d/%02d/%d", dayOfMonth, month + 1, year);
                    btnSelectDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void showTimePicker(boolean isStartTime) {
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.FRANCE, "%02d:%02d", hourOfDay, minute);
                    if (isStartTime) {
                        selectedStartTime = time;
                        btnSelectStartTime.setText(selectedStartTime);
                    } else {
                        selectedEndTime = time;
                        btnSelectEndTime.setText(selectedEndTime);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);

        timePickerDialog.show();
    }

    private void saveEvent() {
        String title = etEventTitle.getText().toString().trim();
        String description = etEventDescription.getText().toString().trim();

        if (title.isEmpty() || selectedDate == null || selectedStartTime == null || selectedEndTime == null) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event(title, description, selectedDate, selectedStartTime, selectedEndTime);

        new Thread(() -> {
            db.eventDao().insert(event);
            runOnUiThread(() -> {
                Toast.makeText(this, "Événement ajouté", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
