package com.example.betterday.common.service;

import android.app.Dialog;
import android.content.Context;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.example.betterday.R;
import com.example.betterday.common.constants.CustomColor;
import com.example.betterday.common.service.ReminderAdapter.ReminderViewHolder;

public class ColorService {

    private ToggleButton green, yellow, blue, red, pink, purple;

    private Context context;

    public ColorService(Context context) {
        this.context = context;
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
            case 0:
            default:
                return "green";
        }
    }

    public int getColorLightVersion(String color) {
        color = color == null ? "green" : color;
        switch (color) {
            case "yellow":
                return R.drawable.card_light_yellow_bg;
            case "blue":
                return R.drawable.card_light_blue_bg;
            case "red":
                return R.drawable.card_light_red_bg;
            case "pink":
                return R.drawable.card_light_pink_bg;
            case "purple":
                return R.drawable.card_light_purple_bg;
            case "green":
            default:
                return R.drawable.card_light_green_bg;
        }
    }

    public void changeTextColor(@NonNull ReminderViewHolder holder, boolean toggle) {
        if (!toggle) {
            holder.titleTextView.setTextAppearance(R.style.secondaryTextColor);
            holder.timeTextView.setTextAppearance(R.style.secondaryTextColor);
            holder.slashTextView.setTextAppearance(R.style.secondaryTextColor);
            holder.dayTextView.setTextAppearance(R.style.secondaryTextColor);
            holder.durationTextView.setTextAppearance(R.style.secondaryTextColor);
        } else {
            int secondaryTextColor = ContextCompat.getColor(context, R.color.white);
            holder.titleTextView.setTextColor(secondaryTextColor);
            holder.timeTextView.setTextColor(secondaryTextColor);
            holder.slashTextView.setTextColor(secondaryTextColor);
            holder.dayTextView.setTextColor(secondaryTextColor);
            holder.durationTextView.setTextColor(secondaryTextColor);
        }
    }

    public void changeItemColor(@NonNull ReminderViewHolder holder, String color, boolean isToggleOn) {
        int itemBackgroundResource = R.drawable.card_green_bg;
        int cardBackgroundResource = R.drawable.card_left_green_bg;
        int trackBackgroundResource = R.drawable.track_selector_green;
        if (color == null || color.equals(CustomColor.colors[0])) {
        } else if (color.equals(CustomColor.colors[1])) {
            itemBackgroundResource = R.drawable.card_yellow_bg;
            cardBackgroundResource = R.drawable.card_left_yellow_bg;
            trackBackgroundResource = R.drawable.track_selector_yellow;
        } else if (color.equals(CustomColor.colors[2])) {
            itemBackgroundResource = R.drawable.card_blue_bg;
            cardBackgroundResource = R.drawable.card_left_blue_bg;
            trackBackgroundResource = R.drawable.track_selector_blue;
        } else if (color.equals(CustomColor.colors[3])) {
            itemBackgroundResource = R.drawable.card_red_bg;
            cardBackgroundResource = R.drawable.card_left_red_bg;
            trackBackgroundResource = R.drawable.track_selector_red;
        } else if (color.equals(CustomColor.colors[4])) {
            itemBackgroundResource = R.drawable.card_pink_bg;
            cardBackgroundResource = R.drawable.card_left_pink_bg;
            trackBackgroundResource = R.drawable.track_selector_pink;
        } else if (color.equals(CustomColor.colors[5])) {
            itemBackgroundResource = R.drawable.card_purple_bg;
            cardBackgroundResource = R.drawable.card_left_purple_bg;
            trackBackgroundResource = R.drawable.track_selector_purple;
        }
        holder.colorCard.setBackgroundResource(cardBackgroundResource);
        int lightColor = getColorLightVersion(color);
        holder.openedCard.setBackgroundResource(lightColor);
        if (isToggleOn) {
            holder.recyclerItem.setBackgroundResource(itemBackgroundResource);
            holder.toggle.setTrackResource(trackBackgroundResource);
            holder.optionsMenu.setImageResource(R.drawable.ic_more_vert_white);
        } else {
            itemBackgroundResource = R.drawable.card_disabled_bg;
            holder.recyclerItem.setBackgroundResource(itemBackgroundResource);
            holder.optionsMenu.setImageResource(R.drawable.ic_more_vert);
        }

    }
}
