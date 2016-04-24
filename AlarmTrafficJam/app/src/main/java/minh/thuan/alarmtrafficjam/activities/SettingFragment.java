package minh.thuan.alarmtrafficjam.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.minhthuan.lib.user.SettingUser;

import java.text.DecimalFormat;

import minh.thuan.alarmtrafficjam.R;
import minh.thuan.alarmtrafficjam.ulities.Ultil;

/**
 * Created by sev_user on 10/19/15.
 */
public class SettingFragment extends Fragment {
    private EditText txtDistance;
    private EditText txtTime;
    private SettingUser setting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.setting_fragment, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtDistance = (EditText) view.findViewById(R.id.input_distance);
        txtDistance.setSelection(txtDistance.getText().length());
        txtTime = (EditText) view.findViewById(R.id.input_time);
        setting = Ultil.user.getSettingUser();
        txtDistance.setText(new DecimalFormat("#.##").format(Math.round(setting.getRadius() / 1000)) + "");
        txtTime.setText(setting.getTimeReport() + "");
    }

    public SettingUser getSetting() {
        if (txtDistance.getText().equals("") || txtTime.getText().equals("")) return null;
        setting.setRadius(Double.parseDouble(txtDistance.getText().toString())*1000);
        setting.setTimeReport(Integer.parseInt(txtTime.getText().toString()));
        return setting;
    }
}
