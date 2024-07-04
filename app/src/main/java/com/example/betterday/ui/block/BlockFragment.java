package com.example.betterday.ui.block;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.betterday.databinding.FragmentBlockBinding;

public class BlockFragment extends Fragment {

    private FragmentBlockBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BlockViewModel calendarViewModel =
                new ViewModelProvider(this).get(BlockViewModel.class);

        binding = FragmentBlockBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textBlock;
        calendarViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}