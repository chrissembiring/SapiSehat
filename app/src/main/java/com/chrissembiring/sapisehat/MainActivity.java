package com.chrissembiring.sapisehat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI initializations
    EditText cowName, cowAge, cowWeight;
    Button formatTag, writeTag;

    //NFC initialisations
    NfcAdapter  nfcAdapter;
    PendingIntent pendingIntent;
    Tag tag;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI ID assignments
        cowName = findViewById(R.id.editText2);
        cowAge = findViewById(R.id.editText3);
        cowWeight = findViewById(R.id.editText4);
        formatTag = findViewById(R.id.button);
        writeTag = findViewById(R.id.button2);

        NfcManager manager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
        nfcAdapter = manager.getDefaultAdapter();
        pendingIntent = PendingIntent.getActivity
                (this,
                        0,
                        new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);

        formatTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tag != null)
                {
                    try
                    {
                        Ndef ndef = Ndef.get(tag);
                        ndef.connect();
                        ndef.writeNdefMessage(new NdefMessage(new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)));
                        ndef.close();
                        cowName.setText("");
                        cowAge.setText("");
                        cowWeight.setText("");
                    }

                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    catch (FormatException e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        });

        writeTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (cowName.getText().toString().equals("") ||
                    cowAge.getText().toString().equals("") ||
                    cowWeight.getText().toString().equals("") ||
                    tag == null)
                {

                    if (TextUtils.isEmpty(cowName.getText().toString()))
                    {
                        cowName.setError("Nama tidak boleh kosong.");
                    }

                    if (TextUtils.isEmpty(cowAge.getText().toString()))
                    {
                        cowAge.setError("Umur tidak boleh kosong.");
                    }

                    if (TextUtils.isEmpty(cowWeight.getText().toString()))
                    {
                        cowWeight.setError("Bobot tidak boleh kosong.");
                    }

                    if (tag == null)
                    {
                        Toast.makeText(getApplicationContext(), "Tag tidak terdeteksi!", Toast.LENGTH_LONG).show();
                    }
                }

                else
                {
                    /*
                    Cow cow = new Cow();
                    cow.setName(cowName.getText().toString());
                    cow.setAge(Integer.parseInt(cowAge.getText().toString()));
                    cow.setWeight(Integer.parseInt(cowWeight.getText().toString()));
                    */
                    writeText(view);
                    Toast.makeText(getApplicationContext(), "Sapi: " + /* cow.getName() */ cowName.getText().toString() + " sudah tersimpan!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        try
        {
            setIntent(intent);
            readIntent(intent);
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Toast.makeText(getBaseContext(), "Tag terdeteksi!", Toast.LENGTH_LONG).show();
        }

        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Tag kosong terdeteksi!", Toast.LENGTH_LONG).show();
        }
    }

    public void writeText(View v)
    {
        try
        {
            if (tag != null)
            {
                NdefRecord txtRecordName = createTextRecord(cowName.getText().toString(), Locale.ENGLISH, true);
                NdefRecord txtRecordAge = createTextRecord(cowAge.getText().toString(), Locale.ENGLISH, true);
                NdefRecord txtRecordWeight = createTextRecord(cowWeight.getText().toString(), Locale.ENGLISH, true);

                NdefRecord[] ndefRecords = {txtRecordName, txtRecordAge, txtRecordWeight};
                write(tag, ndefRecords);
            }

            else Toast.makeText(getBaseContext(), "Tidak ada tag untuk ditulis!", Toast.LENGTH_LONG).show();

        }

        catch (IOException e)
        {
            e.printStackTrace();
        }

        catch (FormatException e)
        {
            e.printStackTrace();
        }

    }

    private void write(Tag tag, NdefRecord[] ndefRecords) throws IOException, FormatException
    {
        NdefMessage ndefMessage = new NdefMessage(ndefRecords);
        Ndef ndef = Ndef.get(tag);

        try
        {
            if (ndef != null)
            {
                ndef.connect();
                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                Toast.makeText(this, "Tag tersimpan!", Toast.LENGTH_LONG).show();
            }

            else Toast.makeText(this, "Tag tidak mendukung NDEF.", Toast.LENGTH_LONG).show();
        }

        catch (Exception e)
        {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public NdefRecord createTextRecord (String payload, Locale locale, boolean encodeInUtf8)
    {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = payload.getBytes(utfEncoding);
        int utfBit = encodeInUtf8 ? 0 : (1<<7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }

    private void readIntent(Intent intent)
    {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
            NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||
            NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action))
        {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;

            if (rawMsgs != null)
            {
                msgs = new NdefMessage[rawMsgs.length];

                for (int i = 0; i < rawMsgs.length; i++)
                {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            tagViews(msgs);
        }
    }

    private void tagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0)
            return;

        String[] textMsgs = new String[3];
        int i = 0;

        do
        {
             byte[] payload = msgs[0].getRecords()[i].getPayload();
             String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
             int languageCodeLength = payload[0] & 0063;

             try
             {
                 textMsgs[i] = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
             }

             catch (UnsupportedEncodingException e)
             {
                 Log.e("Unsupported Encoding", e.toString());
             }

             i++;
        }
        while (i < msgs[0].getRecords().length);

        cowName.setText(textMsgs[0]);
        cowAge.setText(textMsgs[1]);
        cowWeight.setText(textMsgs[2]);
    }


        /*

        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0063;

        try {
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("Unsupported Encoding", e.toString());
        }

        cowName.setText(text);
    }

         */


}