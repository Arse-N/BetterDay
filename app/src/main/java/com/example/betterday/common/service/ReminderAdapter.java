package com.example.betterday.common.service;// PersonAdapter.java

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.example.betterday.R;
import com.example.betterday.common.fileio.JsonUtil;
import com.example.betterday.common.model.Reminder;

import java.util.ArrayList;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private ArrayList<Reminder> remindersList;
    private OnItemRemoveListener onItemRemoveListener;

    private ToggleChangeListener toggleChangeListener;


    public ReminderAdapter(ArrayList<Reminder> reminders, OnItemRemoveListener onItemRemoveListener, ToggleChangeListener toggleChangeListener) {
        this.remindersList = reminders;
        this.onItemRemoveListener = onItemRemoveListener;
        this.toggleChangeListener = toggleChangeListener;

    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = remindersList.get(position);
        holder.titleTextView.setText(reminder.getTitle());
        holder.timeTextView.setText(reminder.getTime());
        holder.dayTextView.setText(reminder.getDay());
        holder.toggle.setChecked(reminder.isToggleOn());
        holder.removeButton.setOnClickListener(v -> {
            onItemRemoveListener.onItemRemove(position);
        });
        holder.toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleChangeListener.onToggleChanged(position, isChecked);
        });
    }


    @Override
    public int getItemCount() {
        return remindersList.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout teammateItem;
        TextView titleTextView;
        TextView timeTextView;
        TextView dayTextView;
        ImageView removeButton;

        Switch toggle;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            teammateItem = itemView.findViewById(R.id.teammate_item);
            titleTextView = itemView.findViewById(R.id.name_value);
            timeTextView = itemView.findViewById(R.id.time_value);
            dayTextView = itemView.findViewById(R.id.day_value);
            removeButton = itemView.findViewById(R.id.remove_button);
            toggle = itemView.findViewById(R.id.toggle);
        }
    }

    public interface OnItemRemoveListener {
        void onItemRemove(int position);
    }

    public interface ToggleChangeListener {
        void onToggleChanged(int position, boolean isChecked);
    }
}
