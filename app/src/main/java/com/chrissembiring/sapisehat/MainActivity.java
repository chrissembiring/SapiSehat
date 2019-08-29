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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI initializations
    EditText cowName, cowAge, cowWeight;
    Button readTag, writeTag;

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
        readTag = findViewById(R.id.button);
        writeTag = findViewById(R.id.button2);

        NfcManager manager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
        nfcAdapter = manager.getDefaultAdapter();
        pendingIntent = PendingIntent.getActivity
                (this,
                        0,
                        new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);

        readTag.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(getApplicationContext(), "Button 1", Toast.LENGTH_LONG).show();
            }
        });

        writeTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (cowName.getText().toString().equals("") || cowAge.getText().toString().equals("") || cowWeight.getText().toString().equals(""))
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
                }

                else
                {
                    Cow cow = new Cow();
                    cow.setName(cowName.getText().toString());
                    cow.setAge(Integer.parseInt(cowAge.getText().toString()));
                    cow.setWeight(Integer.parseInt(cowWeight.getText().toString()));

                    Toast.makeText(getApplicationContext(), "Sapi: " + cow.getName() + " sudah tersimpan!", Toast.LENGTH_LONG).show();
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
    protected void onNewIntent(Intent intent)
    {
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Toast.makeText(getBaseContext(), "Tag terdeteksi!", Toast.LENGTH_LONG).show();
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
}