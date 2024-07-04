package com.example.betterday.ui.reminder;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
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
import com.example.betterday.common.fileio.JsonUtil;
import com.example.betterday.common.model.Reminder;
import com.example.betterday.common.service.ReminderAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class ReminderFragment extends Fragment implements ReminderAdapter.OnItemRemoveListener, ReminderAdapter.ToggleChangeListener {

    private RecyclerView recyclerView;
    private Dialog addReminderDialog;
    private FloatingActionButton add;
    private TextInputEditText titleEditText;

    private TextView headerDescription;

    private CardView header;
    private TimePicker timeEditText;
    private ArrayList<Reminder> remindersList;
    private ReminderAdapter reminderAdapter;

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
                initializeDialog();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initializeDialog() {
        addReminderDialog = new Dialog(requireContext());
        addReminderDialog.setContentView(R.layout.add_reminder_dialog);
        addReminderDialog.getWindow().setBackgroundDrawableResource(R.drawable.bottom_dialog_background);
        addReminderDialog.setCancelable(false);
//        addReminderDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        final ImageView addItemDialogX = addReminderDialog.findViewById(R.id.dialog_X);
        final Button finalAdd = addReminderDialog.findViewById(R.id.add_button);
        finalAdd.setBackgroundResource(R.drawable.button_background);
        titleEditText = addReminderDialog.findViewById(R.id.title_text);
        timeEditText = addReminderDialog.findViewById(R.id.time_text);
        addReminderDialog.show();
        addReminderDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addReminderDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        addReminderDialog.getWindow().setGravity(Gravity.BOTTOM);

        addItemDialogX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReminderDialog.dismiss();
            }
        });

        finalAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewReminder(titleEditText.getText().toString(), getTime(timeEditText));
                updateHeaderDescription();
                addReminderDialog.dismiss();
            }
        });

    }

    private void addNewReminder(String title, String time) {
        remindersList.add(new Reminder(title, time, "day"));
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
}
