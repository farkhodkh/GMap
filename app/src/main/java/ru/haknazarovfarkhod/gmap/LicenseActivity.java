package ru.haknazarovfarkhod.gmap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LicenseActivity extends AppCompatActivity {
    @BindView(R.id.licenseTextView)
    TextView licenseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        ButterKnife.bind(this);

        licenseTextView.setText(R.string.licenseText);
    }

}
