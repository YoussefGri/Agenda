package com.example.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.example.agenda.database.EventDatabase;
import com.example.agenda.model.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private EventDatabase db;
    private String selectedDate;
    private static final int ADD_EVENT_REQUEST = 1;
    private static final int EDIT_EVENT_REQUEST = 2; // Ajout pour la modification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = EventDatabase.getInstance(this);
        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sélectionner la date actuelle par défaut
        selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Calendar.getInstance().getTime());
        loadEvents(selectedDate);

        // Listener pour la sélection d'une date
        calendarView.setOnDayClickListener(eventDay -> {
            selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                    .format(eventDay.getCalendar().getTime());
            loadEvents(selectedDate);
        });

        // Bouton pour ajouter un événement
        findViewById(R.id.fabAddEvent).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
            intent.putExtra("selectedDate", selectedDate);
            startActivityForResult(intent, ADD_EVENT_REQUEST);
        });
    }

    /**
     * Charge les événements associés à une date
     */
    private void loadEvents(String date) {
        new Thread(() -> {
            List<Event> events = db.eventDao().getEventsByDate(date);
            runOnUiThread(() -> {
                eventAdapter = new EventAdapter(events, this);
                recyclerView.setAdapter(eventAdapter);
                if (events.isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_event), Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Rafraîchit la liste après l'ajout, la modification ou la suppression d'un événement
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ADD_EVENT_REQUEST || requestCode == EDIT_EVENT_REQUEST) && resultCode == RESULT_OK) {
            loadEvents(selectedDate); // Recharger les événements après ajout/modification
        }
    }

    /**
     * Supprime un événement
     */
    public void deleteEvent(Event event) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            db.eventDao().delete(event);
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, getString(R.string.deleted_event), Toast.LENGTH_SHORT).show();
                loadEvents(selectedDate);
            });
        });
    }

    /**
     * Lance l'activité de modification d'un événement
     */
    public void editEvent(Event event) {
        Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
        intent.putExtra("eventId", event.getId()); // On passe l'ID de l'événement
        startActivityForResult(intent, EDIT_EVENT_REQUEST);
    }
}
