package org.ogasimli.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;


public class MainActivity extends AppCompatActivity {

    public static String PACKAGE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
                    getSupportFragmentManager().
                    beginTransaction().
                    add(R.id.main_container, new MovieFragment()).
                    commit();
        }

        PACKAGE_NAME = getApplicationContext().getPackageName();
    }
}
