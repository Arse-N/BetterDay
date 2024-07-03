package com.example.betterday.common.fileio;
import android.content.Context;
import com.example.betterday.common.model.Reminder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class JsonUtil {
    private static final String FILE_NAME = "reminders.json";

    public static void writeToJson(Context context, ArrayList<Reminder> remindersList) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(remindersList);
        File file = new File(context.getExternalFilesDir(null), FILE_NAME);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Reminder> readFromJson(Context context) {
        File file = new File(context.getExternalFilesDir(null), FILE_NAME);
        if (!file.exists()) {
            return null;
        }

        Gson gson = new Gson();
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Reminder>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
