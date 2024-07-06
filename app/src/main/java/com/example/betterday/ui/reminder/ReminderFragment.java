package com.example.betterday.ui.reminder;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.betterday.R;
import com.example.betterday.common.constants.CustomColor;
import com.example.betterday.common.constants.Day;
import com.example.betterday.common.fileio.JsonUtil;
import com.example.betterday.common.model.Reminder;
import com.example.betterday.common.service.ReminderAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class ReminderFragment extends Fragment implements ReminderAdapter.OnItemRemoveListener, ReminderAdapter.ToggleChangeListener {

    private RecyclerView recyclerView;
    private Dialog addReminderDialogFirstPage, addReminderDialogSecondPage;
    private FloatingActionButton add;
    private TextInputEditText titleEditText;
    private TextInputLayout titleTextLayout;

    private TextView headerDescription;

    private ToggleButton ringOnce, everyWeek;

    private ToggleButton sunday, monday, tuesday, wednesday, thursday, friday, saturday;
    private ToggleButton green, yellow, blue, red, pink, purple;
    private String chosenColor;

    private CardView header;
    private TimePicker timeEditText;
    private ArrayList<Reminder> remindersList;
    private ReminderAdapter reminderAdapter;

    private NumberPicker hourPicker, minutePicker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        remindersList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        remindersList = JsonUtil.readFromJson(requireContext());
        if (remindersList == null) {
            remindersList = new ArrayList<>();
        }
        header = view.findViewById(R.id.header);
        header.setCardBackgroundColor(Color.TRANSPARENT);
        reminderAdapter = new ReminderAdapter(remindersList, this::onItemRemove, this::onToggleChanged);
        recyclerView = view.findViewById(R.id.reminder_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(reminderAdapter);
        headerDescription = view.findViewById(R.id.header_description);
        updateHeaderDescription();
        add = view.findViewById(R.id.plus_button);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeDialogFirstPage();
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initializeDialogFirstPage() {
        addReminderDialogFirstPage = new Dialog(requireContext());
        addReminderDialogFirstPage.setContentView(R.layout.add_reminder_dialog_first_page);
        addReminderDialogFirstPage.getWindow().setBackgroundDrawableResource(R.drawable.bottom_dialog_background);
        addReminderDialogFirstPage.setCancelable(false);

        final ImageView addItemDialogX = addReminderDialogFirstPage.findViewById(R.id.dialog_X);
        final Button nextButton = addReminderDialogFirstPage.findViewById(R.id.next_button);
        ringOnce = addReminderDialogFirstPage.findViewById(R.id.ring_once);
        everyWeek = addReminderDialogFirstPage.findViewById(R.id.every_week);
//      weekdays buttons
        sunday = addReminderDialogFirstPage.findViewById(R.id.sunday);
        monday = addReminderDialogFirstPage.findViewById(R.id.monday);
        tuesday = addReminderDialogFirstPage.findViewById(R.id.tuesday);
        wednesday = addReminderDialogFirstPage.findViewById(R.id.wednesday);
        thursday = addReminderDialogFirstPage.findViewById(R.id.thursday);
        friday = addReminderDialogFirstPage.findViewById(R.id.friday);
        saturday = addReminderDialogFirstPage.findViewById(R.id.saturday);

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
                if (validateInput()) {
                    initializeDialogSecondPage();
                    addReminderDialogFirstPage.dismiss();
                }
            }
        });
        ringOnce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                everyWeek.setChecked(false);
                ringOnce.setEnabled(false);
                everyWeek.setEnabled(true);
                changeWeekDaysStatus(false);
            }
        });

        everyWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ringOnce.setChecked(false);
                everyWeek.setEnabled(false);
                ringOnce.setEnabled(true);
                changeWeekDaysStatus(true);
            }
        });


    }

    private void initializeDialogSecondPage() {
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
        green = addReminderDialogSecondPage.findViewById(R.id.green);
        yellow = addReminderDialogSecondPage.findViewById(R.id.yellow);
        blue = addReminderDialogSecondPage.findViewById(R.id.blue);
        red = addReminderDialogSecondPage.findViewById(R.id.red);
        pink = addReminderDialogSecondPage.findViewById(R.id.pink);
        purple = addReminderDialogSecondPage.findViewById(R.id.purple);
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

        ToggleButton[] colors = {green, yellow, blue, red, pink, purple};

        for (int i = 0; i < colors.length; i++) {
            final int index = i;
            colors[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        chosenColor = getColorName(index);
                        for (int j = 0; j < colors.length; j++) {
                            if (j != index) {
                                colors[j].setChecked(false);
                            }
                        }
                    }
                }
            });
        }

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
                Reminder newReminder = new Reminder(titleEditText.getText().toString(), getTime(timeEditText), getDay(), (byte) hourPicker.getValue(), (byte) minutePicker.getValue(), chosenColor);
                addNewReminder(newReminder);
                updateHeaderDescription();
                addReminderDialogSecondPage.dismiss();
            }
        });
    }

    private void addNewReminder(Reminder reminder) {
        remindersList.add(reminder);
        reminderAdapter.notifyItemInserted(remindersList.size() - 1);
        JsonUtil.writeToJson(requireContext(), remindersList);
    }

    @Override
    public void onToggleChanged(int position, boolean isChecked) {
        remindersList.get(position).setToggleOn(isChecked);
        updateHeaderDescription();
        JsonUtil.writeToJson(requireContext(), remindersList);
    }

    @Override
    public void onItemRemove(int position) {
        remindersList.remove(position);
        reminderAdapter.notifyItemRemoved(position);
        reminderAdapter.notifyItemRangeChanged(position, remindersList.size());
        updateHeaderDescription();
        JsonUtil.writeToJson(requireContext(), remindersList);
    }

    private String getTime(TimePicker timePicker) {
        int hour, minute;
        String amPm;
        hour = timePicker.getHour();
        minute = timePicker.getMinute();

        if (hour >= 12) {
            amPm = "PM";
            if (hour > 12) {
                hour -= 12;
            }
        } else {
            amPm = "AM";
            if (hour == 0) {
                hour = 12;
            }
        }
        return String.format("%02d:%02d %s", hour, minute, amPm);
    }

    private void updateHeaderDescription() {
        if (remindersList != null && headerDescription != null) {
            long activeReminderCount = remindersList.stream().filter(Reminder::isToggleOn).count();
            headerDescription.setText(String.format("%d %s", activeReminderCount, activeReminderCount == 1 ? "active reminder" : "active reminders"));
        }
    }

    private void changeWeekDaysStatus(boolean status) {
        ToggleButton[] days = {sunday, monday, tuesday, wednesday, thursday, friday, saturday};
        for (ToggleButton day : days) {
            day.setChecked(status);
            day.setEnabled(status);
        }
    }

    private String getDay() {
        ToggleButton[] days = {sunday, monday, tuesday, wednesday, thursday, friday, saturday};
        if (ringOnce.isChecked()) {
            return Day.RING_ONCE;
        } else {
            StringBuilder a = new StringBuilder();
            long checkedCount = IntStream.range(0, days.length).filter(i -> days[i].isChecked()).count();

            if (checkedCount == 0) {
                return Day.RING_ONCE;
            }

            boolean allChecked = checkedCount == days.length;
            IntStream.range(0, days.length)
                    .filter(i -> days[i].isChecked())
                    .forEach(i -> {
                        if (a.length() > 0) {
                            a.append(" ");
                        }
                        a.append(Day.WEEK_DAYS_SHORT[i]);
                    });

            if (allChecked) {
                return Day.EVERY_DAY;
            }

            return a.toString();
        }
    }

    private boolean validateInput() {
        String input = titleEditText.getText().toString().trim();

        if (TextUtils.isEmpty(input)) {
            titleTextLayout.setError("Title cannot be empty!");
            return false;
        } else {
            titleTextLayout.setError(null);
            return true;
        }
    }

    private String getColorName(int index) {
        switch (index) {
            case 0:
                return "green";
            case 1:
                return "yellow";
            case 2:
                return "blue";
            case 3:
                return "red";
            case 4:
                return "pink";
            case 5:
                return "purple";
            default:
                return "";
        }
    }


}
