package com.example.betterday.common.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.example.betterday.R;
import com.example.betterday.common.constants.CustomColor;
import com.example.betterday.common.fileio.JsonUtil;
import com.example.betterday.common.model.Reminder;

import java.util.ArrayList;
import java.util.Calendar;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private ArrayList<Reminder> remindersList;
    private OnItemRemoveListener onItemRemoveListener;
    private UpdateListener updateListener;
    private Dialog changeColorDialog;
    private Context context;

    private ColorService colorService;

    private String chosenColor;


    public ReminderAdapter(ArrayList<Reminder> reminders, OnItemRemoveListener onItemRemoveListener, UpdateListener updateListener, Context context) {
        this.remindersList = reminders;
        this.onItemRemoveListener = onItemRemoveListener;
        this.updateListener = updateListener;
        this.context = context;
        this.colorService = new ColorService();
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Reminder reminder = remindersList.get(position);
        holder.titleTextView.setText(reminder.getTitle());
        holder.timeTextView.setText(reminder.getTime());
        holder.dayTextView.setText(reminder.getSelectedDate().getDayStringFormat());
        holder.durationTextView.setText(String.format("%02dh %02dm", reminder.getSelectedDate().getDurationHour(), reminder.getSelectedDate().getDurationMinute()));
        changeTextColor(holder, reminder.isToggleOn());
        changeCardColor(holder, reminder.getColor());
        holder.toggle.setChecked(reminder.isToggleOn());
        holder.optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, holder, position);
            }
        });
        holder.toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateListener.onToggleChanged(position, isChecked);
            changeTextColor(holder, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return remindersList.size();
    }

    public void changeTextColor(@NonNull ReminderViewHolder holder, boolean toggle) {
        if (!toggle) {
            holder.titleTextView.setTextAppearance(R.style.secondaryTextColor);
            holder.timeTextView.setTextAppearance(R.style.secondaryTextColor);
            holder.slashTextView.setTextAppearance(R.style.secondaryTextColor);
            holder.dayTextView.setTextAppearance(R.style.secondaryTextColor);
            holder.durationTextView.setTextAppearance(R.style.secondaryTextColor);
        } else {
            holder.titleTextView.setTextAppearance(R.style.primaryTextColor);
            holder.timeTextView.setTextAppearance(R.style.primaryTextColor);
            holder.slashTextView.setTextAppearance(R.style.primaryTextColor);
            holder.dayTextView.setTextAppearance(R.style.primaryTextColor);
            holder.durationTextView.setTextAppearance(R.style.primaryTextColor);
        }
    }

    public void changeCardColor(@NonNull ReminderViewHolder holder, String color) {
        int backgroundResource = R.drawable.card_green_bg;
        if (color == null || color.equals(CustomColor.colors[0])) {
        } else if (color.equals(CustomColor.colors[1])) {
            backgroundResource = R.drawable.card_yellow_bg;
        } else if (color.equals(CustomColor.colors[2])) {
            backgroundResource = R.drawable.card_blue_bg;
        } else if (color.equals(CustomColor.colors[3])) {
            backgroundResource = R.drawable.card_red_bg;
        } else if (color.equals(CustomColor.colors[4])) {
            backgroundResource = R.drawable.card_pink_bg;
        } else if (color.equals(CustomColor.colors[5])) {
            backgroundResource = R.drawable.card_purple_bg;
        }
        holder.colorCard.setBackgroundResource(backgroundResource);
    }

    private void showPopupMenu(View view, @NonNull ReminderViewHolder holder, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.item_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_change_color_option) {
                    initializeChangeColorDialog(holder, position);
                    return true;
                } else if (item.getItemId() == R.id.menu_delete_option) {
                    onItemRemoveListener.onItemRemove(position);
                    return true;
                } else {
                    return false;
                }

            }
        });
        popup.show();
    }

    public void initializeChangeColorDialog(@NonNull ReminderViewHolder holder, int position) {
        changeColorDialog = new Dialog(context);
        changeColorDialog.setContentView(R.layout.change_color_dialog);
        changeColorDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        changeColorDialog.setCancelable(false);
        changeColorDialog.getWindow().setLayout(1000, 1000);
        final ImageView addItemDialogX = changeColorDialog.findViewById(R.id.dialog_X);
        final Button updateButton = changeColorDialog.findViewById(R.id.update_button);
        updateButton.setBackgroundResource(R.drawable.button_background);
        ToggleButton[] colors = colorService.initializeColors(changeColorDialog);
        for (int i = 0; i < colors.length; i++) {
            final int index = i;
            colors[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        chosenColor = colorService.getColorName(index);
                        for (int j = 0; j < colors.length; j++) {
                            if (j != index) {
                                colors[j].setChecked(false);
                            }
                        }
                    }
                }
            });
        }
        changeColorDialog.show();

        addItemDialogX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeColorDialog.dismiss();
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateListener.onColorChange(position, chosenColor);
                changeCardColor(holder, chosenColor);
                changeColorDialog.dismiss();
            }
        });


    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout teammateItem;
        TextView titleTextView, timeTextView, slashTextView, dayTextView, durationTextView;
        ImageView optionsMenu;
        CardView colorCard;
        Switch toggle;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            teammateItem = itemView.findViewById(R.id.teammate_item);
            titleTextView = itemView.findViewById(R.id.name_value);
            timeTextView = itemView.findViewById(R.id.time_value);
            slashTextView = itemView.findViewById(R.id.slash);
            dayTextView = itemView.findViewById(R.id.day_value);
            durationTextView = itemView.findViewById(R.id.duration_text);
            optionsMenu = itemView.findViewById(R.id.options_menu);
            toggle = itemView.findViewById(R.id.toggle);
            colorCard = itemView.findViewById(R.id.color_card);
        }
    }

    public interface OnItemRemoveListener {
        void onItemRemove(int position);
    }

    public interface UpdateListener {
        void onToggleChanged(int position, boolean isChecked);

        void onColorChange(int position, String chosenColor);
    }
}
