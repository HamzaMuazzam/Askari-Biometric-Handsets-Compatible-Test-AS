package com.trackingworld.handsets;

import com.fgtit.data.ConversionsEx;

import com.fgtit.fpcore.FPMatch;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MatchActivity extends AppCompatActivity {

    private TextView tvStatu;
    private EditText etText1, etText2;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        initView();

    }

    private void initView() {

        tvStatu = (TextView) findViewById(R.id.textView3);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
        etText1 = (EditText) findViewById(R.id.editText1);
        etText2 = (EditText) findViewById(R.id.editText2);


        //just for test
        etText1.setText("AwFbKwAA4ALAAoACgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgAAUBwAAAAAAAAAAAAAAABjDkZ+UZhHnjkYg14Rn49edqRePls7n/47vA3eJr+mXm/Anh5bCVpfK4pTnx0Oql9uFV2fJReRHxmjZd8ipqS/MSdNfzerzL8hLA4/YS7fnzsxTJ9FuIn/LDwPf0s/oB89wIxfIh/PPFOqyfxIJopdUotYm0gPwtMoIE5bQCXK+1Cn3ztQjkVYTxCYODGiDjhnKJ7YYyiIeTsgh9Y5IY9XOaOMFGMZXpJmmQhRAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMBWiQAAPgO8A7gAuAA4ADgAIAAgACAAAAAAAAAAAAAAAAAAAAAAAAAACUdAAAAAAAAAAAAAAAAJY7rHl2P2J46kMAeaRlaXjka1D5dqEe+RCiCvhyuDz5rvl9+Kx2q324fRj8xJtFfEChm3xIvzl8kMqZfDzXkvy02pJ88ts0/Q7rMXyy7jn9HwEwfYDqJ3FQ2Ch1gnFj7VKBC0y6vj1szL85bXLffW3U4HjtvuMhbXp9FmFuhGDhwKh2YcqmIWUw1Svk/Mc4RAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
        etText2.setText("AwFWHgAAgB4AHgAOAA4ABgAGAAYABgAGAAIAAgACAAIAAgACAAYABgAAOgwAAAAAAAAAAAAAAAA6Bhg+OY/YXkYYmZ4YnBN+O6mHXhYS7L8KH+o3TJ/F31klHN8QKhDfIqwCPwyxzp9oM5zfDzpknx46zP8lPoufDr/N30s/3z9ZkcX8QTyI3FuOG10/HNidNCDC8hU0Dno0OMlbPToeuzwfxXg6IhlyLDiKFxy0DVMAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");


        byte[] temp_origin = Base64.decode(etText1.getText().toString(), 0);
        byte[] temp_convt = new byte[512];
//        Conversions.getInstance().IsoToStd(2,temp_origin,temp_convt);
        ConversionsEx.getInstance().AnsiIsoToStd(temp_origin, temp_convt, ConversionsEx.ISO_19794_2005);
        Log.d("check", "convt: " + bytesToHexString(temp_convt));
        etText1.setText(Base64.encodeToString(temp_convt,0));
        Log.d("check", "convt2: " + Base64.encodeToString(temp_convt,0));
        final String tempConvt = ConversionsEx.getInstance().ToAnsiIso(temp_convt, ConversionsEx.ISO_19794_2005, 0);
        Log.d("check", "check: " + tempConvt);

//		byte[] temp_4000 = Base64.decode(etText2.getText().toString(),0);
//		byte[] temp_convt4 = new byte[512];
//		ConversionsEx.getInstance().AnsiIsoToStd(temp_4000,temp_convt4,ConversionsEx.ISO_19794_2005);
//		final String tempConvt2 = ConversionsEx.getInstance().ToAnsiIso(temp_convt4,ConversionsEx.ISO_19794_2005,0);
//		Log.d("test", "check: "+tempConvt);

        final Button btn_enrol = (Button) findViewById(R.id.button1);

        btn_enrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String s1 = "AwFbKwAA4ALAAoACgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgAAUBwAAAAAAAAAAAAAAABjDkZ+UZhHnjkYg14Rn49edqRePls7n/47vA3eJr+mXm/Anh5bCVpfK4pTnx0Oql9uFV2fJReRHxmjZd8ipqS/MSdNfzerzL8hLA4/YS7fnzsxTJ9FuIn/LDwPf0s/oB89wIxfIh/PPFOqyfxIJopdUotYm0gPwtMoIE5bQCXK+1Cn3ztQjkVYTxCYODGiDjhnKJ7YYyiIeTsgh9Y5IY9XOaOMFGMZXpJmmQhRAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMBWiQAAPgO8A7gAuAA4ADgAIAAgACAAAAAAAAAAAAAAAAAAAAAAAAAACUdAAAAAAAAAAAAAAAAJY7rHl2P2J46kMAeaRlaXjka1D5dqEe+RCiCvhyuDz5rvl9+Kx2q324fRj8xJtFfEChm3xIvzl8kMqZfDzXkvy02pJ88ts0/Q7rMXyy7jn9HwEwfYDqJ3FQ2Ch1gnFj7VKBC0y6vj1szL85bXLffW3U4HjtvuMhbXp9FmFuhGDhwKh2YcqmIWUw1Svk/Mc4RAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
                String s2 = "AwFWHgAAgB4AHgAOAA4ABgAGAAYABgAGAAIAAgACAAIAAgACAAYABgAAOgwAAAAAAAAAAAAAAAA6Bhg+OY/YXkYYmZ4YnBN+O6mHXhYS7L8KH+o3TJ/F31klHN8QKhDfIqwCPwyxzp9oM5zfDzpknx46zP8lPoufDr/N30s/3z9ZkcX8QTyI3FuOG10/HNidNCDC8hU0Dno0OMlbPToeuzwfxXg6IhlyLDiKFxy0DVMAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
                int sc = MatchIsoTemplateStr(s1, s2);
                tvStatu.setText(String.valueOf(sc));
                //int sc = MatchIsoTemplateStr(tempConvt, etText2.getText().toString());
                /*int sc = MatchIsoTemplateStr(etText1.getText().toString(), etText2.getText().toString());
                tvStatu.setText(String.valueOf(sc));*/
            }
        });

        radioGroup.check(R.id.radio3);
    }

    public int MatchIsoTemplateByte(byte[] piFeatureA, byte[] piFeatureB) {
        byte adat[] = new byte[512];
        byte bdat[] = new byte[512];
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radio1:
                ConversionsEx.getInstance().AnsiIsoToStd(piFeatureA, adat, ConversionsEx.ANSI_378_2004);
                ConversionsEx.getInstance().AnsiIsoToStd(piFeatureB, bdat, ConversionsEx.ANSI_378_2004);
                return MatchTemplateAll(adat, bdat);
            case R.id.radio2:
                ConversionsEx.getInstance().AnsiIsoToStd(piFeatureA, adat, ConversionsEx.ISO_19794_2005);
                ConversionsEx.getInstance().AnsiIsoToStd(piFeatureB, bdat, ConversionsEx.ISO_19794_2005);
                return MatchTemplateAll(adat, bdat);
            case R.id.radio3:
                return FPMatch.getInstance().MatchTemplate(piFeatureA, piFeatureB);
        }
        return 0;
    }

    public int MatchTemplateAll(byte[] adat, byte[] bdat) {
        int score = 0;
        //adat[2]=99;
        //bdat[2]=99;
        for (int i = 0; i < 4; i++) {
            byte tmpdat[] = new byte[512];
            ConversionsEx.getInstance().StdChangeCoord(bdat, 256, tmpdat, i);
            int sc = FPMatch.getInstance().MatchTemplate(adat, tmpdat);
            if (sc >= score)
                score = sc;
        }
        return score;
    }

    public int MatchIsoTemplateStr(String strFeatureA, String strFeatureB) {
        byte piFeatureA[] = Base64.decode(strFeatureA, Base64.DEFAULT);
        byte piFeatureB[] = Base64.decode(strFeatureB, Base64.DEFAULT);

        return MatchIsoTemplateByte(piFeatureA, piFeatureB);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
