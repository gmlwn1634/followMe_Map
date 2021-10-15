package com.minewbeacon.blescan.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.yuliwuli.blescan.demo.R;
import com.yuliwuli.blescan.demo.databinding.FragmentListBinding;


public class ListFragment extends Fragment {

    private FragmentListBinding binding;
    private TabLayout tabs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater);

        tabs = binding.tab;

        tabs.addTab(tabs.newTab().setText("결제리스트"));
        tabs.addTab(tabs.newTab().setText("진료리스트"));
        setFrag(0);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if (pos == 0)
                    setFrag(0);
                else if (pos == 1)
                    setFrag(1);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        return binding.getRoot();
    }


    private void setFrag(int n) {
        switch (n) {
            case 0:
                getChildFragmentManager().beginTransaction().replace(R.id.childFrame, new PaymentListFragment()).commit();
                break;
            case 1:
                getChildFragmentManager().beginTransaction().replace(R.id.childFrame, new ClinicListFragment()).commit();
                break;
        }
    }

}
