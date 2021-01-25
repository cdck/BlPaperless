package com.pa.paperless.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.snackbar.Snackbar;
import com.mogujie.tt.protobuf.InterfaceAdmin;
import com.pa.paperless.activity.offline.OffLineActivity;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.utils.LogUtil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pa.boling.paperless.R;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;


import com.wind.myapplication.NativeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * A login screen that offers login via email/PASSWORD.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known USER_NAME names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "root:123456", "test:123456"
    };
    private String USER_NAME;
    private String PASSWORD;
    private boolean REMEMBER;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private CheckBox login_cb_remember;
    private RadioButton login_rb_admin;
    private RadioButton login_rb_member;
    private RadioButton login_rb_offline;
    private RadioGroup login_rgb;
    private NativeUtil jni;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        jni = NativeUtil.getInstance();
        EventBus.getDefault().register(this);
        // Set up the login form.
        populateAutoComplete();
        getOfflinePassword();
        login_cb_remember.setChecked(REMEMBER);
        if (REMEMBER) {
            mEmailView.setText(USER_NAME);
            mPasswordView.setText(PASSWORD);
        } else {
            mPasswordView.setText(R.string.default_str);
        }
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        findViewById(R.id.action_modifier_password).setOnClickListener(view ->
                showPopup(mEmailView.getText().toString().trim())
        );
        findViewById(R.id.email_sign_in_button).setOnClickListener(view -> attemptLogin());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) {
        if (message.getAction() == EventType.LOGIN_BACK) {
            InterfaceAdmin.pbui_Type_AdminLogonStatus object = (InterfaceAdmin.pbui_Type_AdminLogonStatus) message.getObject();
            int err = object.getErr();
            int adminid = object.getAdminid();
            int sessionid = object.getSessionid();
            String adminName = object.getAdminname().toStringUtf8();
            LogUtil.e(TAG, " 登录返回信息：" + err + ",adminid=" + adminid + ", adminName=" + adminName + ", sessionid=" + sessionid);
            showProgress(false);
            switch (err) {
                case 0:
                    startActivity(new Intent(LoginActivity.this, OffLineActivity.class));
                    break;
                case 1:
                    ToastUtils.showShort( R.string.error_field_password);
                    break;
                case 2:
                    ToastUtils.showShort( R.string.error_server_exception);
                    break;
                case 3:
                    ToastUtils.showShort( R.string.error_database_exception);
                    break;
                default:
                    ToastUtils.showShort( R.string.error_code, err);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        LogUtil.i(TAG, "onDestroy...");
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void getOfflinePassword() {
        SharedPreferences sp = getSharedPreferences("offline", MODE_PRIVATE);
        REMEMBER = sp.getBoolean("remember", false);
        USER_NAME = sp.getString("username", "root");
        PASSWORD = sp.getString("password", "123456");
    }

    private final String TAG = "LoginActivity-->";

    private void showPopup(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View inflate = LayoutInflater.from(this).inflate(R.layout.pop_modify_password, (ViewGroup) findViewById(R.id.pop_modify_password));
        builder.setView(inflate);
        EditText userEdt = inflate.findViewById(R.id.modify_user_edt);
        EditText pwdEdt = inflate.findViewById(R.id.modify_pwd_edt);
        EditText newPwdEdt = inflate.findViewById(R.id.modify_new_pwd_edt);
        userEdt.setText(email);
        builder.setPositiveButton(getString(R.string.ensure), (dialog, which) -> {
            String user = userEdt.getText().toString().trim();
            String pwd = pwdEdt.getText().toString().trim();
            String newPwd = newPwdEdt.getText().toString().trim();
            LogUtil.e(TAG, "showPopup :   --> 用户名= " + user + ", pwd= " + pwd + ", newPwd= " + newPwd);
            if (user.equals(USER_NAME) && pwd.equals(PASSWORD)) {
                if (newPwd.equals(pwd)) {
                    ToastUtils.showShort( R.string.error_password_is_same);
                    return;
                }
                if (newPwd.isEmpty()) {
                    ToastUtils.showShort( R.string.error_password_isEmpty);
                    return;
                }
                if (newPwd.length() < 6) {
                    ToastUtils.showShort( R.string.error_password_too_short);
                    return;
                }
                SharedPreferences sp = getSharedPreferences("offline", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("username", user);
                edit.putString("password", newPwd);
                edit.apply();
                edit.commit();
                ToastUtils.showShort( R.string.modify_password_success);
                mPasswordView.setText(getString(R.string.default_str));
                dialog.dismiss();
            } else {
                ToastUtils.showShort( R.string.error_field_password);
                return;
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        //一：调用这个方法时，按对话框以外的地方不起作用。按返回键还起作用
        alertDialog.setCanceledOnTouchOutside(false);
        //二：调用这个方法时，按对话框以外的地方不起作用。按返回键也不起作用
        //alertDialog.setCanceleable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0新特性
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
        } else {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        alertDialog.show();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid PASSWORD, if the USER_NAME entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } /*else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the USER_NAME login attempt.
            showProgress(true);
            int checkedRadioButtonId = login_rgb.getCheckedRadioButtonId();
            switch (checkedRadioButtonId) {
                case R.id.login_rb_admin:
                    jni.login(1, email, password, 0);
                    break;
                case R.id.login_rb_member:
                    jni.login(1, email, password, 1);
                    break;
                case R.id.login_rb_offline:
                    jni.login(1, email, password, 2);
                    break;
            }
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 5;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device USER_NAME's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,
                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},
                // Show primary email addresses first. Note that there won't be
                // a primary email address if the USER_NAME hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private void initView() {
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        login_cb_remember = (CheckBox) findViewById(R.id.login_cb_remember);
        login_rb_admin = (RadioButton) findViewById(R.id.login_rb_admin);
        login_rb_member = (RadioButton) findViewById(R.id.login_rb_member);
        login_rb_offline = (RadioButton) findViewById(R.id.login_rb_offline);
        login_rgb = (RadioGroup) findViewById(R.id.login_rgb);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the USER_NAME.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return false;
            }
            boolean value = false;
            int checkedRadioButtonId = login_rgb.getCheckedRadioButtonId();
            switch (checkedRadioButtonId) {
                case R.id.login_rb_admin:
                    break;
                case R.id.login_rb_member:
                    break;
                case R.id.login_rb_offline:
                    jni.login(1, mEmail, mPassword, 2);
                    getOfflinePassword();

                    if (mEmail.equals(USER_NAME) && mPassword.equals(PASSWORD)) {
                        startActivity(new Intent(LoginActivity.this, OffLineActivity.class));
                        value = true;
                    } else {
                        value = false;
                    }
                    break;
            }
            return value;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            if (success) {
                SharedPreferences sp = getSharedPreferences("offline", MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putBoolean("remember", login_cb_remember.isChecked());
                edit.apply();
                edit.commit();
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_field_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

