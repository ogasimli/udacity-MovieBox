package org.ogasimli.MovieBox;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;


public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Movie movie;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            movie = extras.getParcelable(MainActivity.PACKAGE_NAME);
        } else {
            throw new NullPointerException("No movie found in extras");
        }

        DetailFragment fragment = DetailFragment.getInstance(movie);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.detail_container, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Reverse the scene transition by home button press
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
