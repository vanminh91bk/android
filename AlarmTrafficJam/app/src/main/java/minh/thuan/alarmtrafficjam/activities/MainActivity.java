package minh.thuan.alarmtrafficjam.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.minhthuan.lib.maps.Marker;
import com.minhthuan.lib.maps.MyLatLong;
import com.minhthuan.lib.network.ISocketStringEvent;
import com.minhthuan.lib.result.BaseResult;
import com.minhthuan.lib.result.Protocol;
import com.minhthuan.lib.ultil.Global;
import com.minhthuan.lib.user.SettingUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import minh.thuan.alarmtrafficjam.R;
import minh.thuan.alarmtrafficjam.location.ILocationChange;
import minh.thuan.alarmtrafficjam.location.MyLocation;
import minh.thuan.alarmtrafficjam.ulities.GMessage;
import minh.thuan.alarmtrafficjam.ulities.Ultil;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, View.OnClickListener, ILocationChange, ISocketStringEvent, GoogleMap.OnMarkerClickListener, SearchView.OnQueryTextListener, GoogleMap.OnCameraChangeListener {
    public static final String OLD_IP = "old_ip";
    public static final String OLD_PORT = "old_port";
    private static final int RESULT_GALLERY = 100;
    private final Context context = this;
    private final int HOME = 0, MY_REPORT = 1, ALL_REPORT = 2, SETTING = 3;
    private final String KEY_PAGE = "key_page";
    private final String TAG = "MyMainActivity";
    Thread updateJamMarker = null;
    private GoogleMap mMap;
    private MyLocation myLocation;
    private MarkerOptions myLocationMarker;
    private FloatingActionButton btnSendWarning, btnMyLocation;
    private SupportMapFragment mMapFragment;
    private FrameLayout mOtherLayout;
    private NavigationView navigationView;
    private FragmentManager mFragmentManager;
    private SettingFragment mSettingFragment;
    private int mCurrentPage = HOME;
    private MenuItem menuItemConnect, menuItemSearch;
    private LatLng mCurrentLatLng;
    private CircleImageView profile_image;
    private TextView txtFullName;
    private TextView txtEmail;
    private boolean updateCamera = true;
    private LatLng center = null;
    private HashMap<com.google.android.gms.maps.model.Marker, Marker> listMarker = new HashMap<>();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case Global.MSG_SUCCESS:
                    removeCallbacksAndMessages(null);
                    BaseResult baseResult = (BaseResult) msg.obj;
                    switch (baseResult.getType()) {
                        case GET_MARKER:
                            receiveMarker(baseResult.getObject());
                            break;
                        case UPDATESETTING:
                            Toast.makeText(context, "Update successfully!", Toast.LENGTH_SHORT).show();
                            setCurrentPage(HOME);
                            getSupportActionBar().setTitle("Map");
                            showFloatButton(true);
                            navigationView.getMenu().getItem(0).setChecked(true);
                            break;
                    }
                    break;
                case Global.MSG_ERROR:
                    removeCallbacksAndMessages(null);
                    Toast.makeText(context, "Fail!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };


    private void receiveMarker(String obj) {
        TypeToken<List<Marker>> type = new TypeToken<List<Marker>>() {
        };
        List<Marker> listJam = new Gson().fromJson((String) obj, type.getType());

        mMap.clear();
        if (mCurrentLatLng != null) {
            myLocationMarker = new MarkerOptions().position(mCurrentLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_thislocation)).title("My location");
            mMap.addMarker(myLocationMarker);
        }

        listMarker.clear();

        for (Marker m : listJam) {
            LatLng latLng = new LatLng(m.getLatLong().getLatitude(), m.getLatLong().getLongitude());
            MarkerOptions mark = null;
            switch (m.getLevel()) {
                case Good:
                    mark = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_good24)).title("Here's an info window \n with some info");
                    break;
                case Light_Jam:
                    mark = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_lightjam24)).title("Here's an info window \n with some info");
                    break;
                case Jam:
                    mark = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_jam24)).title("Here's an info window \n with some info");
                    break;
                case Heavy_Jam:
                    mark = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_heavyjam24)).title("Here's an info window \n with some info");
                    break;
                default:
                    break;
            }
            if (mark != null) {
                com.google.android.gms.maps.model.Marker returnMark = mMap.addMarker(mark);
                listMarker.put(returnMark, m);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        GMessage.showMessage(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        GMessage.showMessage(TAG, Ultil.user.toString());

        // Setup first page
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(mCurrentPage).setChecked(true);
        //

        // get header
        View header = navigationView.getHeaderView(0);

        profile_image = (CircleImageView) header.findViewById(R.id.imageHeaderAvatar);
        txtFullName = (TextView) header.findViewById(R.id.txtHeaderUserName);
        txtEmail = (TextView) header.findViewById(R.id.txtHeaderEmail);

        //Set image, txt for header
//        if(Ultil.user.getAvatar()!=null)
//        profile_image.setImageURI(Uri.fromFile(Ultil.user.getAvatar()));
//        else
        profile_image.setImageResource(R.drawable.ic_avatar_default);
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_GALLERY);
            }
        });
        txtEmail.setText(Ultil.user.getEmail());
        txtFullName.setText(Ultil.user.getFullName());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Manage Fragment
        mFragmentManager = (FragmentManager) getSupportFragmentManager();
        mSettingFragment = new SettingFragment();

        // Get Map
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fgmapmain);
        mMapFragment.getMapAsync(this);
        myLocation = new MyLocation(context, this);
        //

        // all of other view
        mOtherLayout = (FrameLayout) findViewById(R.id.otherLayout);
        //

        if (savedInstanceState != null) {
            setCurrentPage(savedInstanceState.getInt(KEY_PAGE));
        }

        btnSendWarning = (FloatingActionButton) findViewById(R.id.btnSendWarning);
        btnSendWarning.setOnClickListener(this);

        btnMyLocation = (FloatingActionButton) findViewById(R.id.btnMyLocation);
        btnMyLocation.setOnClickListener(this);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        ((NavigationView) findViewById(R.id.nav_view)).setNavigationItemSelectedListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_GALLERY: {
                if (null != data) {
                    Uri uri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        profile_image.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        GMessage.showMessage(context,e.getMessage());
                    }
                }
            }
            break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        GMessage.showMessage(TAG, "onBackPressed");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mMapFragment.getView().getVisibility() == View.INVISIBLE) {
                setCurrentPage(HOME);
                navigationView.getMenu().getItem(mCurrentPage).setChecked(true);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        GMessage.showMessage(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.main, menu);
        menuItemConnect = (MenuItem) menu.findItem(R.id.menu_connect);
        menuItemSearch = (MenuItem) menu.findItem(R.id.menu_search);


        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItemSearch);
        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
        }

        showMenuItem();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        GMessage.showMessage(TAG, "onOptionsItemSelected " + item.getTitle());
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_connect:
                SettingUser setting = mSettingFragment.getSetting();
                if (setting == null) {
                    Toast.makeText(context, "Please fill out setting", Toast.LENGTH_SHORT).show();
                } else {
                    Protocol protocol = new Protocol(Protocol.Type.UPDATESETTING, setting, Ultil.user);
                    Ultil.MY_CLIENT.send(protocol);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        GMessage.showMessage(TAG, "onNavigationItemSelected");
        switch (item.getItemId()) {
            case R.id.nav_home:
                setCurrentPage(HOME);
                getSupportActionBar().setTitle("Map");
                showFloatButton(true);
                break;
            case R.id.nav_setting:
                setCurrentPage(SETTING);
                mFragmentManager.beginTransaction().replace(R.id.otherLayout, mSettingFragment).commit();
                getSupportActionBar().setTitle("Setting");
                showFloatButton(false);

                break;
            case R.id.nav_logout:
                SharedPreferences.Editor editor = getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
                editor.putBoolean(Ultil.PRE_IS_LOGIN, false);
                editor.commit();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;

            default:
                break;
        }

        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GMessage.showMessage(TAG, "onMapReady");
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnCameraChangeListener(this);
        mMap = googleMap;
        LatLng myHome = new LatLng(21.007704, 105.843845);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myHome, 13));
    }


    @Override
    public void onClick(View v) {
        GMessage.showMessage(TAG, "onClick");
        switch (v.getId()) {
            case R.id.btnSendWarning:
                if (mCurrentLatLng == null) {
                    myLocation.requestUpdateLocation();
                    return;
                }
                Intent intent = new Intent(context, SendReport.class);
                intent.putExtra(Ultil.AC_LOCATION, new Gson().toJson(mCurrentLatLng));
                startActivity(intent);
                break;
            case R.id.btnMyLocation:
                myLocation.requestUpdateLocation();
                if (mCurrentLatLng != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 13));
                }
                updateCamera = true;
                break;

            default:
                break;
        }
    }

    @Override
    public void locationChange(LatLng location) {
        GMessage.showMessage(TAG, "locationChange");
        mCurrentLatLng = location;
        if (mMap != null) {
            mMap.clear();
            myLocationMarker = new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_thislocation)).title("My location");
            mMap.addMarker(myLocationMarker);
            if (updateCamera) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13));
                updateCamera = false;
            }
        }
    }

    private void showFloatButton(boolean show) {
        GMessage.showMessage(TAG, "showFloatButton " + show);
        if (show) {
            btnMyLocation.setVisibility(View.VISIBLE);
            btnSendWarning.setVisibility(View.VISIBLE);
        } else {
            btnMyLocation.setVisibility(View.INVISIBLE);
            btnSendWarning.setVisibility(View.INVISIBLE);
        }
    }

    private void showMapView() {
        GMessage.showMessage(TAG, "showMapView ");
        if (mMapFragment == null || mOtherLayout == null) {
            GMessage.showMessage(TAG, "mMapFragment or mOtherLayout null");
            return;
        }
        if (mCurrentPage == HOME) {
            mMapFragment.getView().setVisibility(View.VISIBLE);
            mOtherLayout.setVisibility(View.INVISIBLE);
        } else {
            mMapFragment.getView().setVisibility(View.INVISIBLE);
            mOtherLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setCurrentPage(int page) {
        GMessage.showMessage(TAG, "setCurrentPage " + page);
        this.mCurrentPage = page;
        showMapView();
        showMenuItem();
    }

    private void showMenuItem() {
        if (menuItemConnect == null && menuItemSearch == null) {
            GMessage.showMessage(TAG, "menuItemConnect null");
            return;
        }
        if (mCurrentPage == SETTING) {
            menuItemConnect.setVisible(true);
            menuItemSearch.setVisible(false);

        } else if (mCurrentPage == HOME) {
            menuItemConnect.setVisible(false);
            menuItemSearch.setVisible(true);


        } else {
            menuItemConnect.setVisible(false);
            menuItemSearch.setVisible(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        GMessage.showMessage(TAG, "onPause");
        // stop update marker
        updateJamMarker.interrupt();

    }

    @Override
    protected void onResume() {
        super.onResume();
        GMessage.showMessage(TAG, "onResume");
        //start thread update marker
        updateJamMarker = new Thread(new Runnable() {
            @Override
            public void run() {
                GMessage.showMessage(TAG, "Start thread updateJamMarker");
                try {
                    while (true) {

                        Protocol protocol = null;
                        if (center == null) {
                            protocol = new Protocol(Protocol.Type.GET_MARKER, null, Ultil.user);
                        } else {
                            protocol = new Protocol(Protocol.Type.GET_MARKER, new MyLatLong(center.latitude, center.longitude), Ultil.user);
                        }
//                        LatLng center = mMap.getCameraPosition().target;
//                        Protocol protocol =  new Protocol(Protocol.Type.GET_MARKER, new MyLatLong(center.latitude, center.longitude), Ultil.user);
                        Ultil.MY_CLIENT.send(protocol);
                        Thread.sleep(2000);
                    }
                } catch (InterruptedException e) {
                    GMessage.showMessage(TAG, "Stop thread update jam marker");
                }
            }
        });

        updateJamMarker.start();
        // Set listener for client
        Ultil.MY_CLIENT.setListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        GMessage.showMessage(TAG, "onSaveInstanceState");
        outState.putInt(KEY_PAGE, mCurrentPage);
    }

    @Override
    public void onDataReceive(String str) {
        GMessage.showMessage(TAG, "onMessageReceived: " + str);
        if (mMap == null) {
            return;
        }
        BaseResult baseResult = new Gson().fromJson(str, BaseResult.class);
//        if (baseResult.getType().equals(Protocol.Type.GET_MARKER)) {
//            mHandler.sendMessage(mHandler.obtainMessage(Global.MSG_SUCCESS, baseResult.getObject()));
//
//        }

        mHandler.sendMessage(mHandler.obtainMessage(baseResult.getResult(), baseResult));
    }


    @Override
    public boolean onMarkerClick(com.google.android.gms.maps.model.Marker m) {

        Marker marker = listMarker.get(m);
        if (marker == null) return false;
        String add = getAddress(marker.getLatLong().getLatitude(), marker.getLatLong().getLongitude());
        String title = null;
        int icon = 0;

        switch (marker.getLevel()) {
            case Good:
                title = "Good Point";
                icon = R.drawable.ic_good24;
                break;
            case Light_Jam:
                title = "Light Jam Point";
                icon = R.drawable.ic_lightjam24;
                break;
            case Jam:
                title = "Jam Point";
                icon = R.drawable.ic_jam24;
                break;
            case Heavy_Jam:
                title = "Heavy Jam Point";
                icon = R.drawable.ic_heavyjam24;
                break;
            default:
                break;
        }

        BottomSheet.Builder bottomSheet = new BottomSheet.Builder(this).title(title)
                .icon(icon).sheet(R.menu.list);
        bottomSheet.getMenu().findItem(R.id.po_location).setTitle(add);
        bottomSheet.getMenu().findItem(R.id.po_timereport).setTitle("Time report: " + marker.getTimeReport().split(" ")[1]);
        bottomSheet.getMenu().findItem(R.id.po_comment).setTitle("Comment: " + marker.getComment());
        bottomSheet.show();
        return false;
    }

    private String getAddress(double lat, double lon) {
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(lat, lon, 1);
        } catch (IOException e) {
            GMessage.showMessage(TAG, e.getMessage());
        }
        if (addresses != null && addresses.size() > 0) {
            String add = addresses.get(0).getAddressLine(0) + " - " + addresses.get(0).getAddressLine(1) + " - " + addresses.get(0).getAddressLine(2);
            return add;
        }
        return "";
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        if (query != null && !query.equals("")) {
            new GeocoderTask().execute(query);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        center = cameraPosition.target;
    }


    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if (addresses == null || addresses.size() == 0) {
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            // Clears all the existing markers on the map
            mMap.clear();

            // Adding Markers on Google Map for each matching address
            if (addresses.size() == 0) {
                Toast.makeText(context, "No found location!", Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = (Address) addresses.get(0);

            // Creating an instance of GeoPoint, to display in Google Map
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

            String addressText = String.format("%s, %s",
                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                    address.getCountryName());
            Toast.makeText(context, addressText, Toast.LENGTH_SHORT).show();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        }
    }
}
