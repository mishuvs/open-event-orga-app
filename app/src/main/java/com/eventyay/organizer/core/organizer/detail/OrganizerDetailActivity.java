package com.eventyay.organizer.core.organizer.detail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.view.MenuItem;

import com.eventyay.organizer.R;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class OrganizerDetailActivity extends AppCompatActivity implements HasSupportFragmentInjector {

    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private AlertDialog saveAlertDialog;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_detail_activity);

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                .replace(R.id.fragment, new OrganizerDetailFragment())
                .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (fragmentManager.getBackStackEntryCount() > 0)
                    fragmentManager.popBackStack();
                else
                    finish();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() == 2 && saveAlertDialog == null && id != -1) {
            saveAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog))
                .setMessage(getString(R.string.save_changes))
                .setPositiveButton(getString(R.string.stay), (dialog, which) -> {
                    //do nothing
                })
                .setNegativeButton(getString(R.string.discard), (dialog, which) -> {
                    dialog.dismiss();
                    super.onBackPressed();
                })
                .create();
            saveAlertDialog.show();
        } else {
            super.onBackPressed();
        }
    }
}
