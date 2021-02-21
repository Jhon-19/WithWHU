package com.jhony.withwhu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

public class WithWHUActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return WithWHUFragment.newInstance();
    }
}