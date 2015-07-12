package org.ogasimli.MovieBox;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import org.ogasimli.MovieBox.fragments.MovieFragment;


public class MainActivity extends AppCompatActivity {

    public static String PACKAGE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Set Window.FEATURE_ACTIVITY_TRANSITIONSin order to enable transition effect
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().
                    beginTransaction().
                    add(R.id.main_container, new MovieFragment()).
                    commit();
        }
        //Get package name to use within intents
        PACKAGE_NAME = getApplicationContext().getPackageName();
    }
}
