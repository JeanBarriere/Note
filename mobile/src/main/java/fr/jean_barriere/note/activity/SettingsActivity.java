package fr.jean_barriere.note.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.jean_barriere.note.NoteApp;
import fr.jean_barriere.note.R;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_settings)
    Toolbar toolbar;

    @BindView(R.id.url_android_iconics)
    TextView url_android_iconics;

    @BindView(R.id.url_butterknife)
    TextView url_butterknife;

    @BindView(R.id.url_floatingactionbutton)
    TextView url_floatingactionbutton;

    @BindView(R.id.url_materialdrawer)
    TextView url_materialdrawer;

    @BindView(R.id.url_materialedittext)
    TextView url_materialedittext;

    @BindView(R.id.url_materialsearchview)
    TextView url_materialsearchview;

    @BindView(R.id.url_materialviewpager)
    TextView url_materialviewpager;

    @BindView(R.id.url_picasso)
    TextView url_picasso;

    @BindView(R.id.url_unsplash)
    TextView url_unsplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setTitle(getString(R.string.page_title_settings));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        url_android_iconics.setMovementMethod(LinkMovementMethod.getInstance());
        url_butterknife.setMovementMethod(LinkMovementMethod.getInstance());
        url_floatingactionbutton.setMovementMethod(LinkMovementMethod.getInstance());
        url_materialdrawer.setMovementMethod(LinkMovementMethod.getInstance());
        url_materialedittext.setMovementMethod(LinkMovementMethod.getInstance());
        url_materialsearchview.setMovementMethod(LinkMovementMethod.getInstance());
        url_materialviewpager.setMovementMethod(LinkMovementMethod.getInstance());
        url_picasso.setMovementMethod(LinkMovementMethod.getInstance());
        url_unsplash.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();  return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
