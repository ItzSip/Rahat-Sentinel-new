package com.rahat.data.repo;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\n\u001a\u00020\t2\u0006\u0010\u000b\u001a\u00020\tJ\u0016\u0010\f\u001a\u00020\r2\u0006\u0010\u000b\u001a\u00020\t2\u0006\u0010\u000e\u001a\u00020\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/rahat/data/repo/PeerResolver;", "", "context", "Landroid/content/Context;", "identityManager", "Lcom/rahat/security/IdentityManager;", "(Landroid/content/Context;Lcom/rahat/security/IdentityManager;)V", "nameCache", "", "", "resolvePeerName", "rId", "updateCache", "", "name", "app_debug"})
public final class PeerResolver {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.rahat.security.IdentityManager identityManager = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.String> nameCache = null;
    
    @javax.inject.Inject()
    public PeerResolver(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.rahat.security.IdentityManager identityManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String resolvePeerName(@org.jetbrains.annotations.NotNull()
    java.lang.String rId) {
        return null;
    }
    
    public final void updateCache(@org.jetbrains.annotations.NotNull()
    java.lang.String rId, @org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
}