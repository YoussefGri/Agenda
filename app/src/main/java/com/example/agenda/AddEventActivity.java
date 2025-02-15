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
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEventActivity extends AppCompatActivity {

    private EditText etEventTitle, etEventDescription;
    private Button btnSelectDate, btnSelectStartTime, btnSelectEndTime, btnSaveEvent;
    private String selectedDate, selectedStartTime, selectedEndTime;
    private EventDatabase db;
    private int eventId = -1; // Par défaut, pas d'événement existant
    private Event existingEvent; // Pour stocker l'événement en modification

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

        // Récupération des données de l'intent
        selectedDate = getIntent().getStringExtra("selectedDate");
        eventId = getIntent().getIntExtra("eventId", -1); // Vérifie si un ID est passé

        if (selectedDate != null) {
            btnSelectDate.setText(selectedDate);
        }

        if (eventId != -1) { // Mode édition : Charger l'événement
            loadExistingEvent(eventId);
        }

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectStartTime.setOnClickListener(v -> showTimePicker(true));
        btnSelectEndTime.setOnClickListener(v -> showTimePicker(false));

        btnSaveEvent.setOnClickListener(v -> saveOrUpdateEvent());
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

    private void loadExistingEvent(int eventId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            existingEvent = db.eventDao().getEventById(eventId);
            runOnUiThread(() -> {
                if (existingEvent != null) {
                    etEventTitle.setText(existingEvent.getTitle());
                    etEventDescription.setText(existingEvent.getDescription());
                    btnSelectDate.setText(existingEvent.getDate());
                    btnSelectStartTime.setText(existingEvent.getTimeStart());
                    btnSelectEndTime.setText(existingEvent.getTimeEnd());
                    selectedDate = existingEvent.getDate();
                    selectedStartTime = existingEvent.getTimeStart();
                    selectedEndTime = existingEvent.getTimeEnd();
                }
            });
        });
    }

    private void saveOrUpdateEvent() {
        String title = etEventTitle.getText().toString().trim();
        String description = etEventDescription.getText().toString().trim();

        if (title.isEmpty() || selectedDate == null || selectedStartTime == null || selectedEndTime == null) {
            Toast.makeText(this, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();

        if (eventId == -1) {
            // Mode ajout : Création d'un nouvel événement
            Event event = new Event(title, description, selectedDate, selectedStartTime, selectedEndTime);
            executor.execute(() -> {
                db.eventDao().insert(event);
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.added_event), Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        } else {
            // Mode modification : Mise à jour de l'événement existant
            existingEvent.setTitle(title);
            existingEvent.setDescription(description);
            existingEvent.setDate(selectedDate);
            existingEvent.setTimeStart(selectedStartTime);
            existingEvent.setTimeEnd(selectedEndTime);

            executor.execute(() -> {
                db.eventDao().update(existingEvent);
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.updated_event), Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }
}
