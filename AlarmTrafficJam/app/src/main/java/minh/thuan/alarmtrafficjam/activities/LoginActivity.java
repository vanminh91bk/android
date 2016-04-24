package minh.thuan.alarmtrafficjam.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.minhthuan.lib.network.ISocketStringEvent;
import com.minhthuan.lib.result.BaseResult;
import com.minhthuan.lib.result.Protocol;
import com.minhthuan.lib.ultil.Global;
import com.minhthuan.lib.user.User;

import butterknife.ButterKnife;
import butterknife.InjectView;
import minh.thuan.alarmtrafficjam.R;
import minh.thuan.alarmtrafficjam.ulities.GMessage;
import minh.thuan.alarmtrafficjam.ulities.Ultil;

import static minh.thuan.alarmtrafficjam.ulities.Ultil.MY_CLIENT;
import static minh.thuan.alarmtrafficjam.ulities.Ultil.PRE_ID_USER;
import static minh.thuan.alarmtrafficjam.ulities.Ultil.PRE_IS_LOGIN;
import static minh.thuan.alarmtrafficjam.ulities.Ultil.PRE_PASS_USER;


public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LoginActivity";
    private final int REQUEST_SIGNUP = 0;

    private final Context context = this;

    @InjectView(R.id.input_email_login)
    EditText _emailText;
    @InjectView(R.id.input_password)
    EditText _passwordText;
    @InjectView(R.id.btn_login)
    Button _loginButton;
    @InjectView(R.id.link_signup)
    TextView _signupLink;

    @InjectView(R.id.cbSigedin)
    CheckBox _cbSigedin;
    private ProgressDialog progressDialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        // TODO: App start activity, start listener
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        MY_CLIENT.setListener(new ISocketStringEvent() {
            @Override
            public void onDataReceive(String str) {
                GMessage.showMessage(TAG, str);
                BaseResult baseResult = new Gson().fromJson(str, BaseResult.class);
                if (baseResult.getType().equals(Protocol.Type.LOGIN)) {
                    if (baseResult.getResult() == Global.MSG_SUCCESS) {
                        mHandler.sendEmptyMessage(Global.MSG_SUCCESS);
                        Ultil.user = new Gson().fromJson(baseResult.getObject(), User.class);
                        GMessage.showMessage(TAG, Ultil.user.toString());
                    } else {
                        if (baseResult.getResult() == Global.MSG_ERROR) {
                            mHandler.sendMessage(mHandler.obtainMessage(Global.MSG_ERROR, baseResult.getReason()));
                        }
                    }
                }
            }
        });


        SharedPreferences preference = getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        _emailText.setText(preference.getString(Ultil.PRE_ID_USER, ""));
        _passwordText.setText(preference.getString(Ultil.PRE_PASS_USER, ""));
        if (preference.getBoolean(PRE_IS_LOGIN, false)) {
            login();
            _cbSigedin.setChecked(true);
        }
        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(context, SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {
        GMessage.showMessage(TAG, "Login");
        if (!Ultil.isNetwork(context)) {
            GMessage.showMessage(context, "Network not available");
            return;
        }
        if (!validate()) {
            onLoginFailed("Enter a valid account, password");
            return;
        }
        _loginButton.setEnabled(false);
        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        User user = new User(email, password);
        login(user);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(Global.MSG_ERROR, new String("Time out!")), Global.DEFAULT_TIME_LIMIT_MS);
    }


    private void login(User user) {
        Protocol protocol = new Protocol(Protocol.Type.LOGIN, user,null);
        MY_CLIENT.send(protocol);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                // TODO: Implement successful signup logic here
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                this.finish();
            }
        }
    }


    @Override
    public void onBackPressed() {
        // Disable going back to the minh.thuan.alarmtrafficjam.MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        SharedPreferences.Editor editor = getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        GMessage.showMessage(TAG, "onLoginSuccess " + _cbSigedin.isChecked());
        editor.putBoolean(PRE_IS_LOGIN, _cbSigedin.isChecked());
        editor.putString(PRE_ID_USER, _emailText.getText().toString());
        editor.putString(PRE_PASS_USER, _passwordText.getText().toString());
        editor.commit();

        mHandler.removeCallbacksAndMessages(null);

        GMessage.showMessage(context, "Login successfully!");
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed(String mes) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
//        mHandler.removeCallbacksAndMessages(null);
        GMessage.showMessage(context, TAG, mes);
        _loginButton.setEnabled(true);
    }


    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty()) {
            _emailText.setError("Enter a valid account");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("Between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            GMessage.showMessage(TAG, "handleMessage: " + msg.what + "");
            switch (msg.what) {
                case Global.MSG_SUCCESS:
                    removeCallbacksAndMessages(null);
                    onLoginSuccess();
                    break;
                case Global.MSG_ERROR:
                    String reason = (String) msg.obj;
                    removeCallbacksAndMessages(null);
                    onLoginFailed(reason);
                    break;
                default:
                    break;
            }
        }
    };
}
