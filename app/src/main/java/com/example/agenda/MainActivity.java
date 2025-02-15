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

public class MainActivity extends AppCompatActivity implements EventAdapter.EventActionListener {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private EventDatabase db;
    private String selectedDate;
    private static final int ADD_EVENT_REQUEST = 1;
    private static final int EDIT_EVENT_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = EventDatabase.getInstance(this);
        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventAdapter = new EventAdapter(this);
        recyclerView.setAdapter(eventAdapter);

        selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Calendar.getInstance().getTime());
        loadEvents(selectedDate);

        calendarView.setOnDayClickListener(eventDay -> {
            selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                    .format(eventDay.getCalendar().getTime());
            loadEvents(selectedDate);
        });

        findViewById(R.id.fabAddEvent).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
            intent.putExtra("selectedDate", selectedDate);
            startActivityForResult(intent, ADD_EVENT_REQUEST);
        });
    }

    private void loadEvents(String date) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Event> events = db.eventDao().getEventsByDate(date);
            runOnUiThread(() -> updateEventList(events));
        });
    }

    private void updateEventList(List<Event> events) {
        eventAdapter.setEvents(events);
        if (events.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_event), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ADD_EVENT_REQUEST || requestCode == EDIT_EVENT_REQUEST) && resultCode == RESULT_OK) {
            loadEvents(selectedDate);
        }
    }

    @Override
    public void onEditEvent(Event event, int position) {
        Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
        intent.putExtra("eventId", event.getId());
        startActivityForResult(intent, EDIT_EVENT_REQUEST);
    }

    @Override
    public void onDeleteEvent(Event event, int position) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            db.eventDao().delete(event);
            runOnUiThread(() -> {
                eventAdapter.removeEvent(position);
                Toast.makeText(MainActivity.this, getString(R.string.deleted_event), Toast.LENGTH_SHORT).show();
            });
        });
    }
}
