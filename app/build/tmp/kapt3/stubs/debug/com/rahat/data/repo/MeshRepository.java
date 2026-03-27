package com.rahat.data.repo;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0014\u0010\u000b\u001a\u00020\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005J\u000e\u0010\u000e\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u0006R\u001a\u0010\u0003\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\n\u00a8\u0006\u0010"}, d2 = {"Lcom/rahat/data/repo/MeshRepository;", "", "()V", "_nearbyPeers", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/rahat/data/model/PeerState;", "nearbyPeers", "Lkotlinx/coroutines/flow/StateFlow;", "getNearbyPeers", "()Lkotlinx/coroutines/flow/StateFlow;", "setPeers", "", "peers", "updatePeer", "peer", "app_debug"})
public final class MeshRepository {
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rahat.data.model.PeerState>> _nearbyPeers = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rahat.data.model.PeerState>> nearbyPeers = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.rahat.data.repo.MeshRepository INSTANCE = null;
    
    private MeshRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rahat.data.model.PeerState>> getNearbyPeers() {
        return null;
    }
    
    public final void updatePeer(@org.jetbrains.annotations.NotNull()
    com.rahat.data.model.PeerState peer) {
    }
    
    public final void setPeers(@org.jetbrains.annotations.NotNull()
    java.util.List<com.rahat.data.model.PeerState> peers) {
    }
}