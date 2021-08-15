package com.example.mabia.smartpoultrymanagement;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.R.attr.name;

public class LoginActivity extends AppCompatActivity {

    TextView txtForSignUp;

    String userName;
    String userPass;
    String registeredUserName;
    String registeredUserPass;
    EditText etName;
    EditText etPass;
    Button btnSignIn;

    String TABLE_NAME="tbl_user";
    String DB_NAME="my_db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        createDatabase();

        btnSignIn = (Button)findViewById(R.id.button_login);
        etName = (EditText)findViewById(R.id.editNameText);
        etPass=(EditText)findViewById(R.id.editPassText);

        txtForSignUp = (TextView) findViewById(R.id.tv_sign_up);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              String userName=  etName.getText().toString();
                String userPass=  etPass.getText().toString();

//                if(etName.getText().toString().equals("a") && etPass.getText().toString().equals("a")){
//                    //Toast.makeText(LoginActivity.this, "Successfully log in", Toast.LENGTH_LONG).show();
//                       Intent intent1 = new Intent(LoginActivity.this,DashboardActivity.class);
//                    startActivity(intent1);
//                }else{
//                    Toast.makeText(LoginActivity.this, "Can not log in", Toast.LENGTH_LONG).show();
//               }
                if (userName.equals("") || userPass.equals(""))
                {
                    Toast.makeText(LoginActivity.this, "Feilds can not be empty", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    search(userName,userPass);
                }

            }
        });


        txtForSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,
                        SignUpActivity.class);
                startActivity(intent);
            }
        });

    }

    ////////////method for database------------------------
    public void createDatabase() {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE,null);

        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + " (col_id INTEGER PRIMARY KEY, col_name VARCHAR, col_pass VARCHAR);";
        db.execSQL(createTableQuery);
        db.close();
        // Toast.makeText(getApplicationContext(), "Table Created", Toast.LENGTH_LONG).show();
    }

    ////search for sign in-----------------
    public void search(String sName,String sPass)
    {
        SQLiteDatabase db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        Cursor cursor=db.rawQuery("SELECT * FROM "+TABLE_NAME+" order by col_id desc;", null);

        int rowCount=cursor.getCount();

        if(rowCount<=0)
        {
            Toast.makeText(getApplicationContext(), "No data available please sign up first", Toast.LENGTH_SHORT).show();
        }
        else
        {
            cursor.moveToFirst();
            String allData="";


            do {

                String name=cursor.getString(cursor.getColumnIndex("col_name"));
                String pass=cursor.getString(cursor.getColumnIndex("col_pass"));

                if (name.equals(sName) && pass.equals(sPass))
                {
                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(LoginActivity.this,DashboardActivity.class);
                    startActivity(intent1);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Wrong name or pass", Toast.LENGTH_SHORT).show();
                }

            } while (cursor.moveToNext());

           // Toast.makeText(getApplicationContext(), allData, Toast.LENGTH_LONG).show();
        }
        db.close();


    }
}
