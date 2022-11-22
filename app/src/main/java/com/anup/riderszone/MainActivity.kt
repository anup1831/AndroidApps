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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.anup.riderszone.databinding.ActivityMainBinding
import com.anup.riderszone.utils.EMAIL_ID
import com.anup.riderszone.utils.MOBILE_NUMBER
import com.anup.riderszone.utils.RC_GOOGLE_SIGN_IN
import com.facebook.*
import com.facebook.GraphRequest.GraphJSONObjectCallback
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : LifecycleObserver, AppCompatActivity(), View.OnClickListener {

    val TAG: String = "MainActvity"
    lateinit var mGoogleSigninClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions

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
        //initResources();
    }

    //Check this lifecycle event if it is performing well
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initResources(){
        binding.btnDone.setOnClickListener(this)
        binding.tvBtnGoogleSignup.setOnClickListener(this)
        binding.tvBtnFbSignup.setOnClickListener(this)
        tilMobileNumber = binding.tilMobileNumber
        tilEmail = binding.tilEmail
        actv = binding.filledExposedDropdown;
        setupSpinnerItem(actv)
    }

//    private fun initResources(){
//        binding.btnDone.setOnClickListener(this)
//        binding.tvBtnGoogleSignup.setOnClickListener(this)
//        binding.tvBtnFbSignup.setOnClickListener(this)
//        tilMobileNumber = binding.tilMobileNumber
//        tilEmail = binding.tilEmail
//        actv = binding.filledExposedDropdown;
//        setupSpinnerItem(actv)
//
//    }

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
        initialiseGoogleClient()
    }
    private fun initialiseGoogleClient() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSigninClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnDone -> validateTexField()
            R.id.tvBtnGoogleSignup -> integrateGoogle()
                //Toast.makeText(this, "Google Clicked!", Toast.LENGTH_SHORT).show()
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
        if(requestCode == RC_GOOGLE_SIGN_IN){
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleFirbaseSigninWithGoogle(task)
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFirbaseSigninWithGoogle(completedTask: Task<GoogleSignInAccount>) = try {
        val account = completedTask.getResult(ApiException::class.java)
        updateUI(account)
    } catch (e: ApiException){
        // The ApiException status code indicates the detailed failure reason.
        // Please refer to the GoogleSignInStatusCodes class reference for more information.

        // The ApiException status code indicates the detailed failure reason.
        // Please refer to the GoogleSignInStatusCodes class reference for more information.
        Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        Toast.makeText(this, getString(R.string.google_signin_fail), Toast.LENGTH_LONG).show()
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        Toast.makeText(
            this, "Google -" + account?.displayName
                    + " - " + account?.email
                    + " - " + account?.familyName
                    + " - " + account?.givenName
                    + " - " + account?.id
                    + " - " + account?.idToken
                    + " - " + account?.serverAuthCode
                    + " - " + account?.photoUrl, Toast.LENGTH_LONG
        ).show()
    }

    private fun integrateGoogle() {
        val signInIntent: Intent = mGoogleSigninClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    private fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(baseContext) != null
    }

} //End of MainActivity









private fun AutoCompleteTextView?.onItemSelected(text: Editable?) {
    if(text.toString().equals("Email id")){
        Log.e("Anup", " selectedItem " + text.toString())
    } else if (text.toString().equals("Mobile number")){
        Log.e("Anup", " selectedItem " + text.toString())
    }
}



