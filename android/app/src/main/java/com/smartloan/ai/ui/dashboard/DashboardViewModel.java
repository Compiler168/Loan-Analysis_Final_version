package com.smartloan.ai.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.smartloan.ai.data.models.DashboardData;
import com.google.firebase.auth.FirebaseAuth;
import com.smartloan.ai.data.models.AuthData;
import com.smartloan.ai.utils.TokenManager;
import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.smartloan.ai.data.api.ApiClient;
import com.smartloan.ai.data.models.ApiResponse;

public class DashboardViewModel extends AndroidViewModel {

    private final MutableLiveData<DashboardData> dashboardData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(true);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    private ListenerRegistration dashboardListener;

    public DashboardViewModel(Application application) {
        super(application);
    }

    public LiveData<DashboardData> getDashboardData() { return dashboardData; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void loadDashboard() {
        loading.setValue(true);
        String userId = TokenManager.getInstance(getApplication()).getUserId();
        
        if (userId == null || userId.isEmpty()) {
            error.postValue("User not logged in");
            loading.postValue(false);
            return;
        }

        // Check if we are authenticated with Firebase
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            String firebaseToken = TokenManager.getInstance(getApplication()).getFirebaseToken();
            if (firebaseToken != null && !firebaseToken.isEmpty()) {
                FirebaseAuth.getInstance().signInWithCustomToken(firebaseToken)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startFirestoreListener(userId);
                        } else {
                            fetchInitialDashboard();
                        }
                    });
            } else {
                fetchInitialDashboard();
            }
            return;
        }

        startFirestoreListener(userId);
    }

    private void startFirestoreListener(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        if (dashboardListener != null) {
            dashboardListener.remove();
        }

        dashboardListener = db.collection("dashboards").document(userId)
            .addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    error.postValue("Sync failed: " + e.getMessage());
                    // Don't set loading false yet, try fallback
                    fetchInitialDashboard();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        DashboardData data = snapshot.toObject(DashboardData.class);
                        if (data != null) {
                            dashboardData.postValue(data);
                            loading.postValue(false);
                        } else {
                            fetchInitialDashboard();
                        }
                    } catch (Exception ex) {
                        fetchInitialDashboard();
                    }
                } else {
                    fetchInitialDashboard();
                }
            });
    }

    private void fetchInitialDashboard() {
        ApiClient.getService().getDashboard().enqueue(new retrofit2.Callback<ApiResponse<DashboardData>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<DashboardData>> call, retrofit2.Response<ApiResponse<DashboardData>> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    dashboardData.postValue(response.body().getData());
                } else {
                    error.postValue("Failed to load dashboard");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<DashboardData>> call, Throwable t) {
                loading.postValue(false);
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (dashboardListener != null) {
            dashboardListener.remove();
        }
    }
}
