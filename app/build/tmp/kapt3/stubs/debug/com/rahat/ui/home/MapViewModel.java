package com.rahat.ui.home;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019R\u001a\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001d\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\t0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0010R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0010R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/rahat/ui/home/MapViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/rahat/data/repo/AlertRepository;", "userRepo", "Lcom/rahat/data/firebase/FirestoreUserRepository;", "(Lcom/rahat/data/repo/AlertRepository;Lcom/rahat/data/firebase/FirestoreUserRepository;)V", "_alerts", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/rahat/data/model/Alert;", "_userLocation", "Lorg/osmdroid/util/GeoPoint;", "alerts", "Lkotlinx/coroutines/flow/StateFlow;", "getAlerts", "()Lkotlinx/coroutines/flow/StateFlow;", "nearbyPeers", "Lcom/rahat/data/model/PeerState;", "getNearbyPeers", "userLocation", "getUserLocation", "onLocationUpdated", "", "location", "Landroid/location/Location;", "app_debug"})
public final class MapViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.rahat.data.repo.AlertRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.rahat.data.firebase.FirestoreUserRepository userRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<org.osmdroid.util.GeoPoint> _userLocation = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<org.osmdroid.util.GeoPoint> userLocation = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rahat.data.model.Alert>> _alerts = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rahat.data.model.Alert>> alerts = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rahat.data.model.PeerState>> nearbyPeers = null;
    
    public MapViewModel(@org.jetbrains.annotations.NotNull()
    com.rahat.data.repo.AlertRepository repository, @org.jetbrains.annotations.NotNull()
    com.rahat.data.firebase.FirestoreUserRepository userRepo) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<org.osmdroid.util.GeoPoint> getUserLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rahat.data.model.Alert>> getAlerts() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rahat.data.model.PeerState>> getNearbyPeers() {
        return null;
    }
    
    public final void onLocationUpdated(@org.jetbrains.annotations.NotNull()
    android.location.Location location) {
    }
}