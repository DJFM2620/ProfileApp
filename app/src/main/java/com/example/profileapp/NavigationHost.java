package com.example.profileapp;

import androidx.fragment.app.Fragment;

public interface NavigationHost {

    void NavigateTo(Fragment fragment, boolean addToBackStack);
}