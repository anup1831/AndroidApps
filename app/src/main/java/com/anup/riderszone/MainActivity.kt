package com.anup.riderszone

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.anup.riderszone.databinding.ActivityMainBinding
import com.anup.riderszone.utils.EMAIL_ID
import com.anup.riderszone.utils.MOBILE_NUMBER
import com.facebook.*
import com.facebook.GraphRequest.GraphJSONObjectCallback
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.material.textfield.TextInputLayout
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener {

    val TAG: String = "MainActvity"
    lateinit var binding: ActivityMainBinding
    lateinit var actv: AutoCompleteTextView
    lateinit var tilMobileNumber: TextInputLayout
    lateinit var tilEmail: TextInputLayout
    //lateinit var accessToken: AccessToken
    lateinit var callbackManager: CallbackManager
    lateinit var graphRequest: GraphRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initResources();
    }

    private fun initResources(){
        binding.btnDone.setOnClickListener(this)
        binding.tvBtnGoogleSignup.setOnClickListener(this)
        binding.tvBtnFbSignup.setOnClickListener(this)
        tilMobileNumber = binding.tilMobileNumber
        tilEmail = binding.tilEmail
        actv = binding.filledExposedDropdown;
        setupSpinnerItem(actv)

    }

    private fun setupSpinnerItem(actv: AutoCompleteTextView) {
        val signupOptionsList = ArrayList<String>()
        signupOptionsList.add("Mobile Number")
        signupOptionsList.add("Email Id")
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this, R.layout.drop_down_item, signupOptionsList
        )
        actv.setAdapter(adapter)
        actv.onItemClickListener = AdapterView.OnItemClickListener{ adapterView: AdapterView<*>, view1: View, pos: Int, l: Long ->

            if(pos == 0 && signupOptionsList.get(pos).contentEquals(MOBILE_NUMBER)){
                tilMobileNumber.visibility = View.VISIBLE
                tilEmail.visibility = View.GONE
            } else if(pos == 1&& signupOptionsList.get(pos).contentEquals(EMAIL_ID)){
                tilEmail.visibility = View.VISIBLE
                tilMobileNumber.visibility = View.GONE
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnDone -> validateTexField()
            R.id.tvBtnGoogleSignup -> Toast.makeText(this, "Google Clicked!", Toast.LENGTH_SHORT)
                .show()
            R.id.tvBtnFbSignup -> integrateFB()
        }
    }

    private fun validateTexField() {
        if(actv.text.contains(MOBILE_NUMBER, false)){
            val mobileNumber = binding.etMobileNumber.text.toString()
            if(mobileNumber!!.length > 0 && mobileNumber!!.length == 10){
                // perform server communication then move to OTP screen
            } else {
                Toast.makeText(this, "Please enter 10 digits number", Toast.LENGTH_LONG).show();
            }
        } else if (actv.text.contains(EMAIL_ID, false)){
            if(isValidEmail(binding.etEmail.text.toString())){
                // perform server communication then move to OTP screen
            } else {
                Toast.makeText(this, "Please enter proper email id", Toast.LENGTH_LONG).show();
            }
        }
    }

    private fun isValidEmail(email: String?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun integrateFB() {
        var accessToken = AccessToken.getCurrentAccessToken()
        if(accessToken != null){
            getFbData(accessToken)
            return
        }

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(
            this, Arrays.asList(
                "public_profile",
                "email"
            )
        )
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    Log.e(TAG, "FB: login success")
                    val token: String = result?.accessToken?.token!!
                    Log.e(TAG, "FB: login success" + result?.accessToken?.userId)
                    Log.e(TAG, "FB: login success $token")
                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,email,last_name,first_name")
                    getFbData(result.accessToken)

                }

                override fun onCancel() {
                    Log.e(TAG, "FB Login cancel")
                    LoginManager.getInstance().logOut()
                }

                override fun onError(error: FacebookException?) {
                    Log.e(TAG, "FB: login error " + error?.message);
                }

            })
    }

    private fun getFbData(accessToken: AccessToken) {
        val parameters = Bundle()
        parameters.putString("fields", "id,name,email,last_name,first_name")

         graphRequest = GraphRequest(
            accessToken, "me", parameters, null
        ) { response ->
            if (response != null) {
                val callbackEmail =
                    GraphJSONObjectCallback { me, response ->
                        if (response.error != null) {
                            Log.e(TAG, "FB: cannot parse email")
                        } else run {
                            val id: String = me.optString("id")
                            val first_name = me.optString("first_name")
                            val last_name = me.optString("last_name")
                            val email = me.optString("email")
                            val name = me.optString("name")
                            Log.e(
                                TAG,
                                "FB: data " + id + " - " + first_name + " - " + last_name + " - " + email + " - " + name + " - " + accessToken.token + me.toString()
                            );
                            //send above data to server
                        }

                    }
                callbackEmail.onCompleted(response.jsonObject, response)
            }
        }
        graphRequest.executeAsync()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

} //End of MainActivity









private fun AutoCompleteTextView?.onItemSelected(text: Editable?) {
    if(text.toString().equals("Email id")){
        Log.e("Anup", " selectedItem " + text.toString())
    } else if (text.toString().equals("Mobile number")){
        Log.e("Anup", " selectedItem " + text.toString())
    }
}



