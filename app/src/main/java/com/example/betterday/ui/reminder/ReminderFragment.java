package com.example.betterday.ui.reminder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.betterday.MainActivity;
import com.example.betterday.R;
import com.example.betterday.common.constants.Day;
import com.example.betterday.common.fileio.JsonUtil;
import com.example.betterday.common.model.Reminder;
import com.example.betterday.common.model.SelectedDate;
import com.example.betterday.common.service.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.w3c.dom.Text;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ReminderFragment extends Fragment implements ReminderAdapter.OnItemRemoveListener, ReminderAdapter.UpdateListener {

    private RecyclerView recyclerView;
    private Dialog addReminderDialogFirstPage, addReminderDialogSecondPage;
    private TextInputEditText titleEditText;
    private TextInputLayout titleTextLayout;

    private TextView headerDescription;

    private ToggleButton ringOnce, everyWeek;

    private ToggleButton sunday, monday, tuesday, wednesday, thursday, friday, saturday;
    private String chosenColor;
    private CardView header;
    private TimePicker timeEditText;
    private ArrayList<Reminder> remindersList;
    private ReminderAdapter reminderAdapter;

    private ColorService colorService;
    private TimeService timeService;
    private ValidationService validationService;
    private AlarmService alarmService;

    private NumberPicker hourPicker, minutePicker;

    private BroadcastReceiver alarmOpenEventReceiver;

    private ToggleButton[] days;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeFragment(view);
        alarmOpenEventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int alarmId = intent.getIntExtra("alarmId", -1);
                if (alarmId != -1) {
                    updateReminderItem(alarmId, true);
                }
            }
        };

        IntentFilter openFilter = new IntentFilter("com.example.betterday.ALARM_EVENT");
        requireContext().registerReceiver(alarmOpenEventReceiver, openFilter, Context.RECEIVER_NOT_EXPORTED);
    }

    public void initializeFragment(@NonNull View view) {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        remindersList = JsonUtil.readFromJson(requireContext());
        if (remindersList == null) {
            remindersList = new ArrayList<>();
        }
        header = view.findViewById(R.id.header);
        header.setCardBackgroundColor(Color.TRANSPARENT);
        reminderAdapter = new ReminderAdapter(remindersList, this::onItemRemove, this, getContext());
        colorService = new ColorService(requireContext());
        timeService = new TimeService(remindersList, requireContext());
//        countdownService = new CountdownService();
        validationService = new ValidationService(remindersList);
        alarmService = new AlarmService(remindersList, getContext());
        recyclerView = view.findViewById(R.id.reminder_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(reminderAdapter);
        headerDescription = view.findViewById(R.id.header_description);
        updateHeaderDescription();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireContext().unregisterReceiver(alarmOpenEventReceiver);
    }

    public void initializeDialogFirstPage() {
        addReminderDialogFirstPage = new Dialog(requireContext());
        addReminderDialogFirstPage.setContentView(R.layout.add_reminder_dialog_first_page);
        addReminderDialogFirstPage.getWindow().setBackgroundDrawableResource(R.drawable.bottom_dialog_background);
        addReminderDialogFirstPage.setCancelable(false);

        final ImageView addItemDialogX = addReminderDialogFirstPage.findViewById(R.id.dialog_X);
        final Button nextButton = addReminderDialogFirstPage.findViewById(R.id.next_button);
        ringOnce = addReminderDialogFirstPage.findViewById(R.id.ring_once);
        everyWeek = addReminderDialogFirstPage.findViewById(R.id.every_week);
        sunday = addReminderDialogFirstPage.findViewById(R.id.sunday);
        monday = addReminderDialogFirstPage.findViewById(R.id.monday);
        tuesday = addReminderDialogFirstPage.findViewById(R.id.tuesday);
        wednesday = addReminderDialogFirstPage.findViewById(R.id.wednesday);
        thursday = addReminderDialogFirstPage.findViewById(R.id.thursday);
        friday = addReminderDialogFirstPage.findViewById(R.id.friday);
        saturday = addReminderDialogFirstPage.findViewById(R.id.saturday);
        days = new ToggleButton[]{sunday, monday, tuesday, wednesday, thursday, friday, saturday};
        nextButton.setBackgroundResource(R.drawable.button_background);
        titleEditText = addReminderDialogFirstPage.findViewById(R.id.title_text);
        titleTextLayout = addReminderDialogFirstPage.findViewById(R.id.name_text_input_layout);
        timeEditText = addReminderDialogFirstPage.findViewById(R.id.time_text);
        addReminderDialogFirstPage.show();
        addReminderDialogFirstPage.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 2000);
        addReminderDialogFirstPage.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        addReminderDialogFirstPage.getWindow().setGravity(Gravity.BOTTOM);
        addItemDialogX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReminderDialogFirstPage.dismiss();
            }
        });
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    titleTextLayout.setError(null); // Clear error
                } else {
                    titleTextLayout.setError("Title cannot be empty!");
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validationService.validateInput(titleEditText, titleTextLayout)) {
                    Reminder newReminder = new Reminder.Builder()
                            .id(remindersList.size())
                            .title(Objects.requireNonNull(titleEditText.getText()).toString())
                            .time(timeService.getTime(timeEditText))
                            .position(remindersList.size())
                            .selectedDate(new SelectedDate.Builder()
                                    .dayStringFormat(timeService.getDay(days, ringOnce))
                                    .hour(timeEditText.getHour())
                                    .minute(timeEditText.getMinute())
                                    .days(timeService.getSelectedDays()).build())
                            .build();
                    if (validationService.checkNewItemsDate(newReminder)) {
                        initializeDialogSecondPage(newReminder);
                        addReminderDialogFirstPage.dismiss();
                    } else {
                        Toast.makeText(requireContext(), "You can do one task at a time! Please change time or day.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        ringOnce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                everyWeek.setChecked(false);
                ringOnce.setEnabled(false);
                everyWeek.setEnabled(true);
                timeService.changeWeekDaysStatus(false, days);
            }
        });

        everyWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ringOnce.setChecked(false);
                everyWeek.setEnabled(false);
                ringOnce.setEnabled(true);
                timeService.changeWeekDaysStatus(true, days);
            }
        });


    }

    private void initializeDialogSecondPage(Reminder newReminder) {
        addReminderDialogSecondPage = new Dialog(requireContext());
        addReminderDialogSecondPage.setContentView(R.layout.add_reminder_dialog_second_page);
        addReminderDialogSecondPage.getWindow().setBackgroundDrawableResource(R.drawable.bottom_dialog_background);
        addReminderDialogSecondPage.setCancelable(false);
        addReminderDialogSecondPage.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 2000);
        addReminderDialogSecondPage.getWindow().setGravity(Gravity.BOTTOM);

        final ImageView addItemDialogX = addReminderDialogSecondPage.findViewById(R.id.dialog_X);
        final Button finalAdd = addReminderDialogSecondPage.findViewById(R.id.add_button);
        finalAdd.setBackgroundResource(R.drawable.button_background);
        hourPicker = addReminderDialogSecondPage.findViewById(R.id.hourPicker);
        hourPicker.setMaxValue(23);
        hourPicker.setMinValue(0);
        hourPicker.setValue(1);
        minutePicker = addReminderDialogSecondPage.findViewById(R.id.minutePicker);
        minutePicker.setMaxValue(59);
        minutePicker.setMinValue(0);

        //      color buttons
        ToggleButton[] colors = colorService.initializeColors(addReminderDialogSecondPage);
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
        hourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal == 0) {
                    minutePicker.setMinValue(1);
                } else {
                    minutePicker.setMinValue(0);
                }
            }
        });
        addReminderDialogSecondPage.show();

        addItemDialogX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReminderDialogFirstPage.dismiss();
                addReminderDialogSecondPage.dismiss();
            }
        });

        finalAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newReminder.setColor(chosenColor);
                SelectedDate selectedDate = newReminder.getSelectedDate();
                selectedDate.setDurationHour((byte) hourPicker.getValue());
                selectedDate.setDurationMinute((byte) minutePicker.getValue());
                newReminder.setSelectedDate(selectedDate);
                if (validationService.checkNewItemsDuration(newReminder)) {
                    addNewReminder(newReminder);
                    alarmService.setAlarmForItem(newReminder);
                    updateHeaderDescription();
                    addReminderDialogSecondPage.dismiss();
                } else {
                    Toast.makeText(requireContext(), "You can do one task at a time! Please make the duration shorter.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void addNewReminder(Reminder reminder) {
        remindersList.add(reminder);
        reminderAdapter.notifyItemInserted(remindersList.size() - 1);
        JsonUtil.writeToJson(requireContext(), remindersList);
        if (!remindersList.isEmpty()) {
            reminderAdapter.notifyItemChanged(remindersList.size() - 2);
        }
    }

    @Override
    public void onToggleChanged(int position, boolean isChecked) {
        remindersList.get(position).setToggleOn(isChecked);
        Reminder reminder = remindersList.get(position);
        if (isChecked) {
            alarmService.setAlarmForItem(reminder);
        } else {
            alarmService.cancelAlarmForItem(position);
        }
        updateHeaderDescription();
        JsonUtil.writeToJson(requireContext(), remindersList);
    }

    @Override
    public void onColorChange(int position, String chosenColor) {
        remindersList.get(position).setColor(chosenColor);
        updateHeaderDescription();
        JsonUtil.writeToJson(requireContext(), remindersList);
    }

    @Override
    public void onItemRemove(int position) {
        int size = remindersList.size();
        alarmService.cancelAlarmForItem(position);
        Reminder reminder = remindersList.get(position);
        if (reminder.isOpened()) {
            reminderAdapter.cancelCountDown();
        }
        remindersList.remove(position);
        reminderAdapter.notifyItemRemoved(position);
        reminderAdapter.notifyItemRangeChanged(position, remindersList.size());
        updateHeaderDescription();
        JsonUtil.writeToJson(requireContext(), remindersList);
        if (position == size - 1) {
            reminderAdapter.notifyItemChanged(position - 1);
        }
    }

    private void updateHeaderDescription() {
        if (remindersList != null && headerDescription != null) {
            long activeReminderCount = remindersList.stream().filter(Reminder::isToggleOn).count();
            headerDescription.setText(String.format("%d %s", activeReminderCount, activeReminderCount == 1 ? "active reminder" : "active reminders"));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateReminderItem(int alarmId, boolean isOpened) {
        for (Reminder reminder : remindersList) {
            if (reminder.getId() == alarmId) {
                reminder.setOpened(isOpened);
                int position = remindersList.indexOf(reminder);
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                if (holder instanceof ReminderAdapter.ReminderViewHolder) {
                    reminderAdapter.changeReminderItemSize((ReminderAdapter.ReminderViewHolder) holder, reminder.getColor(), isOpened);
                    if (isOpened) {
                        int duration = reminder.getSelectedDate().getDurationHour() * 3600 + reminder.getSelectedDate().getDurationMinute() * 60;
                        reminderAdapter.startCountdown((ReminderAdapter.ReminderViewHolder) holder, reminder, duration);
                    }
                }
                JsonUtil.writeToJson(requireContext(), remindersList);
                break;
            }
        }
    }

}
