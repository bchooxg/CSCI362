package com.example.secureFolderManagement.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.secureFolderManagement.LoggingManager
import com.example.secureFolderManagement.PreferenceManager
import com.example.secureFolderManagement.R
import com.example.secureFolderManagement.database.AppDatabase
import com.example.secureFolderManagement.interfaces.ApiInterface
import com.example.secureFolderManagement.models.UserPollRequest
import com.example.secureFolderManagement.models.UserPollResponse
import com.example.secureFolderManagement.models.UserRequest
import com.example.secureFolderManagement.models.UserResponse
import com.example.secureFolderManagement.services.ServiceBuilder
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory


class loginActivity : AppCompatActivity() {

    private lateinit var appDb : AppDatabase
    private val loggingManager = LoggingManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<MaterialButton>(R.id.loginBtn)

        loginBtn.setOnClickListener(View.OnClickListener {
            // Get username and password from views
            val userNameEntered = username.text.toString()
            val passwordEntered = password.text.toString()
            verifyUser(userNameEntered, passwordEntered)
        })



        //
    }

    fun getUsers(){
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(resources.getString(R.string.server_url))
            .build()
            .create(ApiInterface::class.java)
        val retrofitData = retrofit.getData()
        retrofitData.enqueue(object : Callback<List<UserResponse>?> {
            override fun onResponse(
                call: Call<List<UserResponse>?>,
                response: Response<List<UserResponse>?>
            ) {
                val body = response.body()
                for (data in body!!){
                    Log.v("TEST", "Username: ${data.username}}")
                }
            }

            override fun onFailure(call: Call<List<UserResponse>?>, t: Throwable) {
                Log.v("TEST", "Error: ${t.message}")
            }
        })
    }

    fun verifyUser(username:String, password:String){
        val response = ServiceBuilder.buildService(ApiInterface::class.java)
        response.verifyUser(UserRequest(username,password,true)).enqueue(
            object : Callback<UserResponse> {
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.v("TEST", "Response: ${response.body()}")
                        val userResponse = response.body()

                        // Get additional user info from response and save to shared preferences
                        val sp = getSharedPreferences(resources.getString(R.string.shared_prefs), MODE_PRIVATE)
                        PreferenceManager(sp).setUserGroupSettings(
                            usergroup = userResponse!!.usergroup,
                            minPass = userResponse.min_pass,
                            requireBiometrics = userResponse.require_biometrics,
                            requireEncryption = userResponse.require_encryption,
                            companyID = userResponse.company_id,
                            pinType = userResponse.pin_type,
                            pinMaxTries = userResponse.pin_max_tries,
                            pinLockoutTime = userResponse.pin_lockout_time
                        )



                        authenticateUser(username)
                        Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()
                        // go to main activity
                        val intent = Intent(this@loginActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
                        Log.v("TEST", "Response: ${response.body()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Toast.makeText(this@loginActivity, t.toString(), Toast.LENGTH_LONG).show()
                    Log.v("TEST", "Error: ${t.message}")
                }

            }
        )
    }

    fun authenticateUser(username: String){
        val sp = getSharedPreferences(resources.getString(R.string.shared_prefs), MODE_PRIVATE)
        PreferenceManager(sp).setAuth(username)

        loggingManager.insertLog("Login")
    }



}