package minh.thuan.alarmtrafficjam.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.minhthuan.lib.maps.Marker;
import com.minhthuan.lib.maps.MyLatLong;
import com.minhthuan.lib.network.ISocketStringEvent;
import com.minhthuan.lib.result.BaseResult;
import com.minhthuan.lib.result.Protocol;
import com.minhthuan.lib.ultil.Global;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import minh.thuan.alarmtrafficjam.R;
import minh.thuan.alarmtrafficjam.location.MyLocation;
import minh.thuan.alarmtrafficjam.ulities.GMessage;
import minh.thuan.alarmtrafficjam.ulities.Ultil;

public class SendReport extends AppCompatActivity implements OnMapReadyCallback {

    private final String TAG = "SendReport";
    private final Context context = this;
    @InjectView(R.id.txtUser)
    EditText txtUser;
    @InjectView(R.id.txtLocation)
    EditText txtLocation;
    @InjectView(R.id.spLevel)
    Spinner spLevel;
    @InjectView(R.id.txtComment)
    EditText txtComment;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private MyLocation myLocation;
    private LatLng mCurrentLatLng;
    private MarkerOptions myLocationMarker;
    private ProgressDialog progressDialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            GMessage.showMessage(TAG, "handleMessage: " + msg.what + "");
            switch (msg.what) {
                case Global.MSG_SUCCESS:
                    removeCallbacksAndMessages(null);
                    onSendSuccess();
                    break;
                case Global.MSG_ERROR:
                    String reason = (String) msg.obj;
                    removeCallbacksAndMessages(null);
                    onSendFailed(reason);
                    break;
                default:
                    break;
            }
        }
    };

    private void onSendSuccess() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        finish();
    }

    private void onSendFailed(String reason) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        GMessage.showMessage(context, TAG, reason);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_report);

        // Fix issue Caused by: android.os.NetworkOnMainThreadException
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_white_24dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ButterKnife.inject(this);
        // Get Map
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fgmap);
        mMapFragment.getMapAsync(this);


        Spinner spinner = (Spinner) findViewById(R.id.spLevel);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, R.layout.my_spinner_style);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Ultil.MY_CLIENT.setListener(new ISocketStringEvent() {
            @Override
            public void onDataReceive(String str) {
                GMessage.showMessage(TAG, str);
                BaseResult baseResult = new Gson().fromJson(str, BaseResult.class);
                if (baseResult.getType().equals(Protocol.Type.ADD_MARKER)) {
                    if (baseResult.getResult() == Global.MSG_SUCCESS) {
                        mHandler.sendEmptyMessage(Global.MSG_SUCCESS);
                    } else {
                        if (baseResult.getResult() == Global.MSG_ERROR) {
                            mHandler.sendMessage(mHandler.obtainMessage(Global.MSG_ERROR, baseResult.getReason()));
                        }
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send:
                GMessage.showMessage(TAG, "menu_send");
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                String date = dateFormat.format(new Date());

                Marker.Level level = null;
                switch (spLevel.getSelectedItemPosition()) {
                    case Marker.GOOD:
                        level = Marker.Level.Good;
                        break;
                    case Marker.LIGHT_JAM:
                        level = Marker.Level.Light_Jam;
                        break;
                    case Marker.JAM:
                        level = Marker.Level.Jam;
                        break;
                    case Marker.HEAVY_JAM:
                        level = Marker.Level.Heavy_Jam;
                        break;
                    default:
                        break;
                }

                Marker marker = new Marker(new MyLatLong(mCurrentLatLng.latitude, mCurrentLatLng.longitude), level, Ultil.user.getId(), date, txtComment.getText().toString());
                Protocol protocol = new Protocol(Protocol.Type.ADD_MARKER, marker,Ultil.user);
                Ultil.MY_CLIENT.send(protocol);
                progressDialog = new ProgressDialog(SendReport.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Send to server...");
                progressDialog.show();
                mHandler.sendMessageDelayed(mHandler.obtainMessage(Global.MSG_ERROR, new String("Time out!")), Global.DEFAULT_TIME_LIMIT_MS);
                break;
            default:
                break;
        }


        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GMessage.showMessage(TAG, "onMapReady");
        mMap = googleMap;
        Bundle bundle = getIntent().getExtras();
        mCurrentLatLng = new Gson().fromJson(bundle.getString(Ultil.AC_LOCATION), LatLng.class);
        myLocationMarker = new MarkerOptions().position(mCurrentLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_thislocation)).title("My location");
        mMap.addMarker(myLocationMarker);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 13));

        String add = getAddress(mCurrentLatLng.latitude, mCurrentLatLng.longitude);
            txtLocation.setText(add);
        txtUser.setText(Ultil.user.getFullName());
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
}
