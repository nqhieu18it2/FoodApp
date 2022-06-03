package com.example.eatlt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eatlt.Commom.Commom;
import com.example.eatlt.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    Button btnSignIn, btnSignUp;
    TextView txtSlogan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = (Button) findViewById(R.id.buttonSignIn);
        btnSignUp = (Button) findViewById(R.id.buttonSignUp);

        txtSlogan = (TextView) findViewById(R.id.txtSlogan);
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        txtSlogan.setTypeface(face);

        //Init paper
        Paper.init(this);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SignUp.class);
                startActivity(intent);

            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SignIn.class);
                startActivity(intent);
            }
        });
        //check remember
        String user = Paper.book().read(Commom.USER_KEY);
        String pwd = Paper.book().read(Commom.PWD_KEY);
        if(user !=null && pwd !=null){
            if(!user.isEmpty() && !pwd.isEmpty())
                login(user,pwd);
        }
    }

    private void login(String phone, String pwd) {
        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        // sign in method here
        if (Commom.isConnectedToInternet(getBaseContext())) {
            final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("Please Wait....");
            mDialog.show();
            mDialog.setCancelable(false);

            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Check if user not exist in database
                    // if yes then check for password
                    if (dataSnapshot.child(phone).exists()) {
                        //get user information
                        mDialog.dismiss();

                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone); //set the phone of user
                        // if pswd equals then send to home class and declare current user
                        if (user.getPassword().equals(pwd)) {

                            Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
                            Commom.currentUser = user;
                            startActivity(homeIntent);
                            finish();

                        } else {
                            Toast.makeText(MainActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mDialog.dismiss();
                        Toast.makeText(MainActivity.this, "User not exists", Toast.LENGTH_SHORT).show();

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Please Check Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}