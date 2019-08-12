package com.chrissembiring.sapisehat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // UI initializations
    EditText cowName, cowAge, cowWeight;
    Button readTag, writeTag;

    // Cow setter-getter
    public class Cow
    {
        private String name;
        private int age;
        private int weight;

        public String getName()
        {
            return this.name;
        }

        public int getAge()
        {
            return this.age;
        }

        public int getWeight()
        {
            return this.weight;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public void setAge(int age)
        {
            this.age = age;
        }

        public void setWeight(int weight)
        {
            this.weight = weight;
        }
    }


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
}