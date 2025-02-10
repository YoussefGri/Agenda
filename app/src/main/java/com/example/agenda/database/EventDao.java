package com.example.agenda.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import com.example.agenda.model.Event;

import java.util.List;

@Dao
public interface EventDao {
    @Insert
    void insert(Event event);

    @Delete
    void delete(Event event);

    @Query("SELECT * FROM events WHERE date = :date ORDER BY timeStart ASC")
    List<Event> getEventsByDate(String date);

}
