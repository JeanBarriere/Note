package fr.jean_barriere.note.activity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.ExecutionOptions;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.jean_barriere.note.NoteApp;
import fr.jean_barriere.note.R;
import fr.jean_barriere.note.adapter.NotePagerAdapter;
import fr.jean_barriere.note.item.List;
import fr.jean_barriere.note.item.Note;
import fr.jean_barriere.note.manager.DataManager;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public final String id = UUID.randomUUID().toString();
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 1001;
    private static final int COMPLETE_AUTHORIZATION_REQUEST_CODE = 1002;
    private static final int PERMISSION_REQUEST_GET_ACCOUNTS = 1003;
    public static final int REQUEST_REFRESH_NOTE = 1004;
    public static final int REQUEST_REFRESH_LIST = 1005;
    private GoogleApiClient mGoogleApiClient;

    private Drawer drawer;
    private AccountHeader headerResult;

    private ResultCallback<DriveApi.DriveContentsResult> newDriveFileCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull final DriveApi.DriveContentsResult driveContentsResult) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DriveFolder appFolder = Drive.DriveApi.getAppFolder(mGoogleApiClient);
                    MetadataChangeSet set = new MetadataChangeSet.Builder().setTitle(getString(R.string.drive_file_name)).setMimeType(getString(R.string.drive_file_mimetype)).build();
                    appFolder.createFile(mGoogleApiClient, set, driveContentsResult.getDriveContents()).setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                        @Override
                        public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                            driveFileResult.getDriveFile().open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(writeDriveCallback);
                            loadingView.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();
        }
    };

    private ResultCallback<DriveApi.MetadataBufferResult> connectDriveCallback = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(@NonNull final DriveApi.MetadataBufferResult metadataBufferResult) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (metadataBufferResult.getMetadataBuffer().getCount() == 0) {
                        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(newDriveFileCallback);
                    } else {
                        if (metadataBufferResult.getMetadataBuffer().get(0).getOriginalFilename().equals(getString(R.string.drive_file_name))) {
                            DriveFile file = metadataBufferResult.getMetadataBuffer().get(0).getDriveId().asDriveFile();
//                              file.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(deleteFileContentDriveCallback);
                            file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(updateDataDriveCallback);
                        }
                    }
                }
            }).start();
        }
    };

    private ResultCallback<DriveApi.MetadataBufferResult> updateDriveCallback = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(@NonNull final DriveApi.MetadataBufferResult metadataBufferResult) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (metadataBufferResult.getMetadataBuffer().getCount() == 0) {
                        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(newDriveFileCallback);
                    } else {
                        if (metadataBufferResult.getMetadataBuffer().get(0).getOriginalFilename().equals(getString(R.string.drive_file_name))) {
                            DriveFile file = metadataBufferResult.getMetadataBuffer().get(0).getDriveId().asDriveFile();
                            file.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(writeDriveCallback);
                        }
                    }
                }
            }).start();
        }
    };

    private ResultCallback<DriveApi.DriveContentsResult> updateDataDriveCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
            try {
                InputStream inputStream = driveContentsResult.getDriveContents().getInputStream();
                byte[] b = new byte[4096];
                String current = "";
                int read = 0;

                while ((read = inputStream.read(b, 0, 4096)) != -1)
                    current += new String(b, 0, read);

                if (current.length() > 0)
                    DataManager.saveJsonAsData(new JSONObject(current));
                driveContentsResult.getDriveContents().reopenForWrite(mGoogleApiClient).setResultCallback(writeDriveCallback);
            } catch (IOException | JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.drive_fail_get_data), Toast.LENGTH_LONG).show();
            }
        }
    };

    private ResultCallback<DriveApi.DriveContentsResult> writeDriveCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
            OutputStream outputStream = driveContentsResult.getDriveContents().getOutputStream();
            try {
                outputStream.write(DataManager.getAsJsonObject().toString().getBytes());
                driveContentsResult.getDriveContents().commit(mGoogleApiClient, null).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (!status.isSuccess())
                            Toast.makeText(getApplicationContext(), getString(R.string.drive_fail_save_data), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.drive_fail_save_data), Toast.LENGTH_LONG).show();
            } finally {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingView.setVisibility(View.GONE);
                    }
                });
            }
        }
    };

    private ResultCallback<DriveApi.DriveContentsResult> deleteFileContentDriveCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
            OutputStream outputStream = driveContentsResult.getDriveContents().getOutputStream();
            try {
                outputStream.write("".getBytes());
                driveContentsResult.getDriveContents().commit(mGoogleApiClient, null).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (!status.isSuccess())
                            Toast.makeText(getApplicationContext(), getString(R.string.drive_fail_save_data), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.drive_fail_save_data), Toast.LENGTH_LONG).show();
            }
        }
    };


    @BindView(R.id.floating_btn_add_note) FloatingActionButton addNoteButton;
    @BindView(R.id.floating_btn_add_list) FloatingActionButton addListButton;
    @BindView(R.id.materialViewPager) MaterialViewPager materialViewPager;
    @BindView(R.id.search_view) MaterialSearchView searchView;
    @BindView(R.id.loading_view) LinearLayout loadingView;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_GET_ACCOUNTS: {
                    onConnected(Bundle.EMPTY);
                return;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        loadingView.setVisibility(View.VISIBLE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.GET_ACCOUNTS)) {
                Toast.makeText(getApplicationContext(), getString(R.string.should_accept_permissions), Toast.LENGTH_LONG).show();
                return;
                //                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS}, PERMISSION_REQUEST_GET_ACCOUNTS);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS}, PERMISSION_REQUEST_GET_ACCOUNTS);
            }
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                DataManager.setGoogleAccountLink(true);
                Drive.DriveApi.getAppFolder(mGoogleApiClient).listChildren(mGoogleApiClient).setResultCallback(connectDriveCallback);
            }
        }).start();

        Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

        headerResult.removeProfile(0);
        headerResult.addProfiles(new ProfileDrawerItem().withName(person.getDisplayName()).withEmail(Plus.AccountApi.getAccountName(mGoogleApiClient)).withIcon(person.getImage().getUrl()));
        if (person.getCover() != null && person.getCover().getCoverPhoto() != null && person.getCover().getCoverPhoto().getUrl() != null)
            headerResult.setHeaderBackground(new ImageHolder(person.getCover().getCoverPhoto().getUrl()));
        drawer.removeItems(4, 11);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK)
                    mGoogleApiClient.connect();
                break;
            case COMPLETE_AUTHORIZATION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    // App is authorized, you can go back to sending the API request
                } else {
                    // User denied access, show him the account chooser again
                }
                break;
            case REQUEST_REFRESH_NOTE:
                if (resultCode == RESULT_OK && DataManager.isGoogleAccountLinked())
                    Drive.DriveApi.getAppFolder(mGoogleApiClient).listChildren(mGoogleApiClient).setResultCallback(updateDriveCallback);
                else if (DataManager.isGoogleAccountLinked())
                    Drive.DriveApi.getAppFolder(mGoogleApiClient).listChildren(mGoogleApiClient).setResultCallback(connectDriveCallback);
                materialViewPager.getViewPager().getAdapter().notifyDataSetChanged();
                break;
            case REQUEST_REFRESH_LIST:
                if (resultCode == RESULT_OK && DataManager.isGoogleAccountLinked())
                    Drive.DriveApi.getAppFolder(mGoogleApiClient).listChildren(mGoogleApiClient).setResultCallback(updateDriveCallback);
                else if (DataManager.isGoogleAccountLinked())
                    Drive.DriveApi.getAppFolder(mGoogleApiClient).listChildren(mGoogleApiClient).setResultCallback(connectDriveCallback);
                materialViewPager.getViewPager().getAdapter().notifyDataSetChanged();
        }
    }

    public void updateDrive() {
        Drive.DriveApi.getAppFolder(mGoogleApiClient).listChildren(mGoogleApiClient).setResultCallback(updateDriveCallback);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.drive_fail_connect), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DataManager.isGoogleAccountLinked()) {
            mGoogleApiClient.connect();
            DataManager.registerMainActivity(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API).addApi(Plus.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });

        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(new ImageHolder(getString(R.string.header_unsplash_url)))
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .withTranslucentStatusBar(false)
                .build();


        setTitle("");

        Toolbar toolbar = materialViewPager.getToolbar();

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        final PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(0).withName(getString(R.string.home_viewpager_all)).withIcon(GoogleMaterial.Icon.gmd_dashboard);
        final PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(1).withName(getString(R.string.home_viewpager_notes)).withIcon(GoogleMaterial.Icon.gmd_note);
        final PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(2).withName(getString(R.string.home_viewpager_lists)).withIcon(GoogleMaterial.Icon.gmd_featured_play_list);
        final PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(3).withName(getString(R.string.page_title_settings)).withIcon(GoogleMaterial.Icon.gmd_settings);
        final PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIdentifier(4).withName(getString(R.string.google_signin)).withIcon(FontAwesome.Icon.faw_google);

        drawer = new DrawerBuilder().withActionBarDrawerToggle(true).withTranslucentStatusBar(false).withActivity(this).withDrawerLayout(R.layout.material_drawer_fits_not).withAccountHeader(headerResult)
                .addDrawerItems(
                item1,
                item2,
                item3,
                new DividerDrawerItem().withIdentifier(10),
                item4)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        PrimaryDrawerItem it = (PrimaryDrawerItem) drawerItem;
                        if (it.getIdentifier() >= 0 && it.getIdentifier() <= 2) {
                            materialViewPager.getViewPager().setCurrentItem((int) it.getIdentifier(), true);
                            drawer.closeDrawer();
                        } else if (it.getIdentifier() == 3) {
                            Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(settings);
                            drawer.setSelection(materialViewPager.getViewPager().getCurrentItem());
                            drawer.closeDrawer();
                        } else {
                            mGoogleApiClient.connect();
                            drawer.setSelection(materialViewPager.getViewPager().getCurrentItem());
                            drawer.closeDrawer();
                        }
                        return true;
                    }
                }).withToolbar(toolbar).build();

        materialViewPager.getViewPager().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                drawer.setSelection(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        if (!DataManager.isGoogleAccountLinked())
            drawer.addItems(new DividerDrawerItem().withIdentifier(11), item5);


        // Daily picture
        materialViewPager.setImageUrl(getString(R.string.unsplash_daily_url), 100);
        // materialViewPager.setMaterialViewPagerListener(NotePagerListener.getInstance());

        materialViewPager.getViewPager().setAdapter(new NotePagerAdapter(getSupportFragmentManager()));

        materialViewPager.getViewPager().setOffscreenPageLimit(materialViewPager.getViewPager().getAdapter().getCount());
        materialViewPager.getPagerTitleStrip().setViewPager(materialViewPager.getViewPager());

        materialViewPager.getViewPager().getAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if (DataManager.isGoogleAccountLinked())
                    Drive.DriveApi.getAppFolder(mGoogleApiClient).listChildren(mGoogleApiClient).setResultCallback(updateDriveCallback);
            }
        });


        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);

        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addNote = new Intent(MainActivity.this, AddNoteActivity.class);
                startActivityForResult(addNote, REQUEST_REFRESH_NOTE);
                menuMultipleActions.collapse();
            }
        });

        addListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addList = new Intent(MainActivity.this, AddListActivity.class);
                startActivityForResult(addList, REQUEST_REFRESH_LIST);
                menuMultipleActions.collapse();
            }
        });


        searchView.setSubmitOnClick(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.dismissSuggestions();
                searchView.closeSearch();
                ArrayList<List> lists = DataManager.getLists();
                if (lists != null)
                    for (List list : lists) {
                        if (list.getTitle().equals(query)) {
                            Intent intent = new Intent(MainActivity.this, EditListActivity.class);
                            intent.putExtra("list", list);
                            startActivityForResult(intent, REQUEST_REFRESH_LIST);
                            return true;
                        }
                    }
                ArrayList<Note> notes = DataManager.getNotes();
                if (notes != null)
                    for (Note note : notes) {
                        if (note.getValue().contains(query)) {
                            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                            intent.putExtra("note", note);
                            startActivityForResult(intent, REQUEST_REFRESH_NOTE);
                            return true;
                        }
                    }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                ArrayList<String> suggestions = new ArrayList<>();
                ArrayList<List> lists = DataManager.getLists();
                if (lists != null)
                    for (List list : lists)
                       suggestions.add(list.getTitle());
                ArrayList<Note> notes = DataManager.getNotes();
                if (notes != null)
                    for (Note note : notes)
                        suggestions.add(note.getValue());
                searchView.setSuggestions(suggestions.toArray(new String[]{}));
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, materialViewPager.getToolbar().getMenu());

        MenuItem item = menu.getItem(0);
        searchView.setMenuItem(item);

        return true;
    }
}
