package com.example.biometric;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;


public class MainActivity2 extends AppCompatActivity {


    Button btn_generate;
    TextView tv_token;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private String Id;
    private String code = "";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        FirebaseAuth.AuthStateListener authlistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    Id = user.getUid();

                } else {
                    // User is signed out

                }
                // ...
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(authlistener);

        tv_token = findViewById(R.id.tv_token);
        btn_generate = findViewById(R.id.btn_generate);


        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity2.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result)
            {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();


                List<Integer> numbers = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    numbers.add(i);
                }
                Collections.shuffle(numbers);

                for (int i = 0; i < 4; i++) {
                    code += numbers.get(i).toString();
                }
                tv_token.setText(code);
                btn_generate.setEnabled(false);

                new Timer().schedule(new TimerTask() {
                    public void run() {
                        startActivity(new Intent(MainActivity2.this, MainActivity.class));
                    }
                }, 120000);

                final FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Create a new user with a first, middle, and last name
                final Map<String, Object> auth = new HashMap<>();
                auth.put("code", code);
                auth.put("userID", Id);


// Add a new document with a generated ID
                db.collection("Authentications")
                        .add(auth)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(MainActivity2.this, "Success", Toast.LENGTH_SHORT).show();
                                Log.v("auth",  auth.toString());

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                  Log.w("error", "Error adding document", e);

                            }
                        });

                RequestQueue queue = Volley.newRequestQueue(MainActivity2.this);
                String url ="https://lmsacs.herokuapp.com/delete_auth?id="+code;

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

// Add the request to the RequestQueue.
                queue.add(stringRequest);






// Delete generated code after 5 mins from firestore

//                new Timer().schedule(new TimerTask() {
//                    public void run() {
//                        FirebaseFirestore.getInstance().collection("Authentications")
//                                .whereEqualTo("code", code)
//                                .get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                        WriteBatch batch = FirebaseFirestore.getInstance().batch();
//                                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
//                                        for(DocumentSnapshot snapshot : snapshotList)
//                                        {
//                                            batch.delete(snapshot.getReference());
//
//                                        }
//                                        batch.commit()
//                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                    @Override
//                                                    public void onSuccess(Void aVoid) {
//                                                        Log.d("success", "deleted successfully from firebase");
//                                                    }
//                                                })
//                                                .addOnFailureListener(new OnFailureListener() {
//                                                    @Override
//                                                    public void onFailure(@NonNull Exception e) {
//                                                        Log.e("failure", "Did not delete");
//
//                                                    }
//                                                });
//
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//
//                            }
//                        });
//
//                    }
//                }, 120000);
//


            }


            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric authentication")
                .setSubtitle("authenticate using biometrics ")
                .setNegativeButtonText("Use account password")
                .build();


        btn_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                biometricPrompt.authenticate(promptInfo);


            }

        });
    }



}