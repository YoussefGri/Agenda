package com.example.agenda;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agenda.model.Event;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<Event> events = new ArrayList<>();
    private EventActionListener eventActionListener;

    public EventAdapter(EventActionListener eventActionListener) {
        this.eventActionListener = eventActionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvTime.setText("🕒 " + event.getTimeStart() + " - " + event.getTimeEnd());
        holder.tvDescription.setText(event.getDescription());

        holder.btnEdit.setOnClickListener(v -> eventActionListener.onEditEvent(event, holder.getAdapterPosition()));
        holder.btnDelete.setOnClickListener(v -> eventActionListener.onDeleteEvent(event, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<Event> newEvents) {
        this.events.clear();
        this.events.addAll(newEvents);
        notifyDataSetChanged();
    }

    public void updateEvent(int position, Event updatedEvent) {
        if (position >= 0 && position < events.size()) {
            events.set(position, updatedEvent);
            notifyItemChanged(position);
        }
    }

    public void removeEvent(int position) {
        if (position >= 0 && position < events.size()) {
            events.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvDescription;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvTime = itemView.findViewById(R.id.tvEventTime);
            tvDescription = itemView.findViewById(R.id.tvEventDescription);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public interface EventActionListener {
        void onEditEvent(Event event, int position);
        void onDeleteEvent(Event event, int position);
    }
}
