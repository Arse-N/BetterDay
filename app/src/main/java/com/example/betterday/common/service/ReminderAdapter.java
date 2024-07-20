package com.example.betterday.common.service;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.example.betterday.R;
import com.example.betterday.common.fileio.JsonUtil;
import com.example.betterday.common.model.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private ArrayList<Reminder> remindersList;
    private OnItemRemoveListener onItemRemoveListener;
    private UpdateListener updateListener;
    private Dialog changeColorDialog;
    private Context context;

    private ColorService colorService;

    private CountDownTimer countDownTimer;

    private String chosenColor;

    public ReminderAdapter(ArrayList<Reminder> reminders, OnItemRemoveListener onItemRemoveListener, UpdateListener updateListener, Context context) {
        this.remindersList = reminders;
        this.onItemRemoveListener = onItemRemoveListener;
        this.updateListener = updateListener;
        this.context = context;
        this.colorService = new ColorService(context);
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
        colorService.changeTextColor(holder, reminder.isToggleOn());
        colorService.changeItemColor(holder, reminder.getColor(), reminder.isToggleOn());
        holder.toggle.setChecked(reminder.isToggleOn());
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (position == getItemCount() - 1) {
            params.bottomMargin = 150;
        } else {
            params.bottomMargin = 0;
        }
        holder.itemView.setLayoutParams(params);

        holder.optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, holder, position);
            }
        });
        holder.toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateListener.onToggleChanged(position, isChecked);
            colorService.changeTextColor(holder, isChecked);
            colorService.changeItemColor(holder, reminder.getColor(), isChecked);
        });

        changeReminderItemSize(holder, reminder.getColor(), reminder.isOpened());
        if (reminder.isOpened()) {
            long currentTimeSeconds = System.currentTimeMillis() / 1000;
            int duration = reminder.getSelectedDate().getDurationHour() * 3600 + reminder.getSelectedDate().getDurationMinute() * 60;
            int reminderTimeInSeconds = (int) getTimeInSeconds(reminder.getSelectedDate().getHour(), reminder.getSelectedDate().getMinute());
            duration = currentTimeSeconds - reminderTimeInSeconds > 0 ? (int) (duration - (currentTimeSeconds - reminderTimeInSeconds)) : 0;
            if (duration == 0) {
                remindersList.get(position).setOpened(false);
                JsonUtil.writeToJson(context, remindersList);
                changeReminderItemSize(holder, reminder.getColor(), false);
            } else {
                startCountdown(holder, reminder, duration);
            }
        }

    }

    @Override
    public int getItemCount() {
        return remindersList.size();
    }

    public void cancelCountDown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
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
                        colors[index].setEnabled(false);
                        for (int j = 0; j < colors.length; j++) {
                            if (j != index) {
                                colors[j].setChecked(false);
                                colors[j].setEnabled(true);
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
                colorService.changeItemColor(holder, chosenColor, holder.toggle.isChecked());
                chosenColor = null;
                changeColorDialog.dismiss();
            }
        });


    }

    public void changeReminderItemSize(@NonNull ReminderViewHolder holder, String color, boolean isOpened) {
        int lightColor = colorService.getColorLightVersion(color);
        float density = holder.itemView.getContext().getResources().getDisplayMetrics().density;
        int heightInPixels = (int) (130 * density);
        if (isOpened) {
            heightInPixels = (int) (400 * density);
        }
        holder.reminderItem.getLayoutParams().height = heightInPixels;
        holder.reminderItem.requestLayout();
        holder.openedCard.setBackgroundResource(lightColor);
        holder.toggle.setEnabled(!isOpened);
    }

    public long getTimeInSeconds(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis() / 1000;
    }

    public void startCountdown(@NonNull ReminderViewHolder holder, Reminder reminder, int duration) {
        Handler uiHandler = new Handler(Looper.getMainLooper());
        new CountDownTimer(duration * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                uiHandler.post(() -> {
                    String time = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished), TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)), TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                    final String[] hourMinSec = time.split(":");
                    holder.hourValue.setText(hourMinSec[0]);
                    holder.minuteValue.setText(hourMinSec[1]);
                    holder.secondValue.setText(hourMinSec[2]);
                });

            }

            public void onFinish() {
                remindersList.get(reminder.getPosition()).setOpened(false);
                if (reminder.getSelectedDate().getDays().contains(-1)) {
                    remindersList.get(reminder.getPosition()).setToggleOn(false);
                    holder.toggle.setChecked(false);
                }
                JsonUtil.writeToJson(context, remindersList);
                changeReminderItemSize(holder, reminder.getColor(), false);

            }
        }.start();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout reminderItem;
        LinearLayout recyclerItem;
        TextView titleTextView, timeTextView, slashTextView, dayTextView, durationTextView;
        public TextView hourValue, minuteValue, secondValue;
        ImageView optionsMenu;
        CardView colorCard, openedCard;
        Switch toggle;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerItem = itemView.findViewById(R.id.recycler_item);
            reminderItem = itemView.findViewById(R.id.reminder_item);
            titleTextView = itemView.findViewById(R.id.name_value);
            timeTextView = itemView.findViewById(R.id.time_value);
            slashTextView = itemView.findViewById(R.id.slash);
            dayTextView = itemView.findViewById(R.id.day_value);
            durationTextView = itemView.findViewById(R.id.duration_text);
            optionsMenu = itemView.findViewById(R.id.options_menu);
            toggle = itemView.findViewById(R.id.toggle);
            colorCard = itemView.findViewById(R.id.color_card);
            openedCard = itemView.findViewById(R.id.opened_card);
            hourValue = itemView.findViewById(R.id.hourValue);
            minuteValue = itemView.findViewById(R.id.minuteValue);
            secondValue = itemView.findViewById(R.id.secondValue);
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
