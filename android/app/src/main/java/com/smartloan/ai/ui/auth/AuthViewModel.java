package com.smartloan.ai.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.smartloan.ai.data.api.ApiClient;
import com.smartloan.ai.data.models.ApiResponse;
import com.smartloan.ai.data.models.AuthData;
import com.smartloan.ai.data.models.LoginRequest;
import com.smartloan.ai.data.models.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {

    private final MutableLiveData<AuthResult> authResult = new MutableLiveData<>();

    public LiveData<AuthResult> getAuthResult() {
        return authResult;
    }

    public void login(String email, String password) {
        ApiClient.getService().login(new LoginRequest(email, password))
                .enqueue(new Callback<ApiResponse<AuthData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            AuthData data = response.body().getData();
                            if (data.getFirebaseCustomToken() != null && !data.getFirebaseCustomToken().isEmpty()) {
                                com.google.firebase.auth.FirebaseAuth.getInstance().signInWithCustomToken(data.getFirebaseCustomToken())
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                authResult.postValue(AuthResult.success(
                                                        data.getToken(),
                                                        data.getUser().getId(),
                                                        data.getUser().getName(),
                                                        data.getUser().getEmail(),
                                                        data.getUser().getRole(),
                                                        data.getFirebaseCustomToken()
                                                ));
                                            } else {
                                                authResult.postValue(AuthResult.error("Firebase Authentication failed"));
                                            }
                                        });
                            } else {
                                authResult.postValue(AuthResult.success(
                                        data.getToken(),
                                        data.getUser().getId(),
                                        data.getUser().getName(),
                                        data.getUser().getEmail(),
                                        data.getUser().getRole(),
                                        data.getFirebaseCustomToken()
                                ));
                            }
                        } else {
                            String error = "Invalid credentials";
                            if (response.body() != null && response.body().getError() != null) {
                                error = response.body().getError();
                            }
                            authResult.postValue(AuthResult.error(error));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                        authResult.postValue(AuthResult.error("Connection failed. Check your network."));
                    }
                });
    }

    public void register(String name, String email, String phone, String password) {
        ApiClient.getService().register(new RegisterRequest(name, email, password, phone))
                .enqueue(new Callback<ApiResponse<AuthData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            AuthData data = response.body().getData();
                            if (data.getFirebaseCustomToken() != null && !data.getFirebaseCustomToken().isEmpty()) {
                                com.google.firebase.auth.FirebaseAuth.getInstance().signInWithCustomToken(data.getFirebaseCustomToken())
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                authResult.postValue(AuthResult.success(
                                                        data.getToken(),
                                                        data.getUser().getId(),
                                                        data.getUser().getName(),
                                                        data.getUser().getEmail(),
                                                        data.getUser().getRole(),
                                                        data.getFirebaseCustomToken()
                                                ));
                                            } else {
                                                authResult.postValue(AuthResult.error("Firebase Authentication failed"));
                                            }
                                        });
                            } else {
                                authResult.postValue(AuthResult.success(
                                        data.getToken(),
                                        data.getUser().getId(),
                                        data.getUser().getName(),
                                        data.getUser().getEmail(),
                                        data.getUser().getRole(),
                                        data.getFirebaseCustomToken()
                                ));
                            }
                        } else {
                            String error = "Registration failed";
                            if (response.body() != null && response.body().getError() != null) {
                                error = response.body().getError();
                            }
                            authResult.postValue(AuthResult.error(error));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                        authResult.postValue(AuthResult.error("Connection failed. Check your network."));
                    }
                });
    }

    /**
     * Result wrapper for auth operations.
     */
    public static class AuthResult {
        private final boolean success;
        private final String token;
        private final String userId;
        private final String userName;
        private final String userEmail;
        private final String userRole;
        private final String firebaseToken;
        private final String error;

        private AuthResult(boolean success, String token, String userId, String userName,
                           String userEmail, String userRole, String firebaseToken, String error) {
            this.success = success;
            this.token = token;
            this.userId = userId;
            this.userName = userName;
            this.userEmail = userEmail;
            this.userRole = userRole;
            this.firebaseToken = firebaseToken;
            this.error = error;
        }

        public static AuthResult success(String token, String id, String name, String email, String role, String firebaseToken) {
            return new AuthResult(true, token, id, name, email, role, firebaseToken, null);
        }

        public static AuthResult error(String error) {
            return new AuthResult(false, null, null, null, null, null, null, error);
        }

        public boolean isSuccess() { return success; }
        public String getToken() { return token; }
        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getUserEmail() { return userEmail; }
        public String getUserRole() { return userRole; }
        public String getFirebaseToken() { return firebaseToken; }
        public String getError() { return error; }
    }
}
