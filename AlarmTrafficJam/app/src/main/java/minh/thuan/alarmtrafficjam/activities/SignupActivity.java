package minh.thuan.alarmtrafficjam.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private final Context context = this;
    @InjectView(R.id.input_name)
    EditText _nameText;
    @InjectView(R.id.input_full_name)
    EditText _fullNameText;
    @InjectView(R.id.input_email)
    EditText _emailText;
    @InjectView(R.id.input_password)
    EditText _passwordText;
    @InjectView(R.id.input_password_confirm)
    EditText _passwordConfirmText;
    @InjectView(R.id.btn_signup)
    Button _signupButton;
    @InjectView(R.id.link_login)
    TextView _loginLink;
    @InjectView(R.id.input_birthday)
    EditText _birthday;
    private ProgressDialog progressDialog;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Global.MSG_SUCCESS:
                    removeCallbacksAndMessages(null);
                    onSignupSuccess();
                    break;
                case Global.MSG_ERROR:
                    String reason = (String) msg.obj;
                    removeCallbacksAndMessages(null);
                    onSignupFailed(reason);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.inject(this);

        MY_CLIENT.setListener(new ISocketStringEvent() {
            @Override
            public void onDataReceive(String str) {
                GMessage.showMessage(TAG, str);
                BaseResult baseResult = new Gson().fromJson(str, BaseResult.class);
                if (baseResult.getType().equals(Protocol.Type.SIGUP)) {
                    if (baseResult.getResult() == Global.MSG_SUCCESS) {
                        mHandler.sendEmptyMessage(Global.MSG_SUCCESS);
                        Ultil.user = new Gson().fromJson(baseResult.getObject(), User.class);
                    } else {
                        if (baseResult.getResult() == Global.MSG_ERROR) {
                            mHandler.sendMessage(mHandler.obtainMessage(Global.MSG_ERROR, baseResult.getReason()));
                        }
                    }
                }
            }
        });

        _birthday.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                GMessage.showMessage(TAG, "_birthday onClick");
                DatePickerDialog mDatePicker = new DatePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_MinWidth, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        String str = "" + selectedmonth + "/" + selectedday + "/" + selectedyear;
                        _birthday.setText(str);
                    }
                }, 1991, 10, 23);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();
            }
        });

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        GMessage.showMessage(TAG, "Signup");
        if (!Ultil.isNetwork(context)) {
            GMessage.showMessage(context, "Network not available");
            return;
        }
        if (!validate()) {
            onSignupFailed("Invalid input!");
            return;
        }

        _signupButton.setEnabled(false);

        progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String fullName = _fullNameText.getText().toString();
        String birthday = _birthday.getText().toString();
        // TODO: Implement your own signup logic here.
        mHandler.sendEmptyMessageDelayed(Global.MSG_ERROR, Global.DEFAULT_TIME_LIMIT_MS);
        User user = new User(name, password, fullName, email, birthday);
        Protocol protocol = new Protocol(Protocol.Type.SIGUP, user,null);
        MY_CLIENT.send(protocol);
    }


    public void onSignupSuccess() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        GMessage.showMessage(context, "Create account successfully");
        SharedPreferences.Editor editor = getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putBoolean(PRE_IS_LOGIN, true);
        editor.putString(PRE_ID_USER, _nameText.getText().toString());
        editor.putString(PRE_PASS_USER, _passwordText.getText().toString());
        editor.commit();
        mHandler.removeCallbacksAndMessages(null);
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed(String mes) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        mHandler.removeCallbacksAndMessages(null);
        GMessage.showMessage(context, "Login fail: " + mes);
        _signupButton.setEnabled(true);
    }

    public boolean isAlphaNumber(String name) {
        char[] chars = name.toCharArray();
        for (char c : chars) {
            if (!Character.isLetterOrDigit(c)) {
                if (c != '-' && c != '_')
                    return false;
            }
        }
        return true;
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String confirmPass = _passwordConfirmText.getText().toString();

        if (name.isEmpty() || name.length() < 3 || !isAlphaNumber(name)) {
            _nameText.setError("At least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Enter a valid email address");
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

        if (!password.equals(confirmPass)) {
            _passwordConfirmText.setError("Password is not same other");
            valid = false;
        } else {
            _passwordConfirmText.setError(null);
        }

        return valid;
    }
}
