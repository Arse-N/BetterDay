package com.example.betterday.common.service;

import android.app.Dialog;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import com.example.betterday.R;

public class ColorService {

    private ToggleButton green, yellow, blue, red, pink, purple;

    private String chosenColor;

    public ColorService() {
    }


    public ToggleButton[] initializeColors(Dialog dialog) {
        green = dialog.findViewById(R.id.green);
        yellow = dialog.findViewById(R.id.yellow);
        blue = dialog.findViewById(R.id.blue);
        red = dialog.findViewById(R.id.red);
        pink = dialog.findViewById(R.id.pink);
        purple = dialog.findViewById(R.id.purple);

        ToggleButton[] colors = {green, yellow, blue, red, pink, purple};
        return colors;

    }


    public String getColorName(int index) {
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
