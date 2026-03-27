package com.rahat.security;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u0000 \u001f2\u00020\u0001:\u0001\u001fB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0014J\u0006\u0010\u0016\u001a\u00020\u0006J\u0006\u0010\u0017\u001a\u00020\u0012J\b\u0010\u0018\u001a\u0004\u0018\u00010\u0006J\u001e\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0014J\u000e\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u0006R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\n\u001a\u00020\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\f\u0010\r\u00a8\u0006 "}, d2 = {"Lcom/rahat/security/IdentityManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "ANDROID_KEYSTORE", "", "KEY_ALIAS", "KEY_RID", "PREFS_FILENAME", "encryptedPrefs", "Landroid/content/SharedPreferences;", "getEncryptedPrefs", "()Landroid/content/SharedPreferences;", "encryptedPrefs$delegate", "Lkotlin/Lazy;", "generateEphemeralIdAtOffset", "secret", "Ljavax/crypto/SecretKey;", "timeWindowMs", "", "windowOffset", "generateRId", "getOrCreateDeviceSecret", "getPersistentRId", "isEphIdValidForDevice", "", "ephId", "savePersistentRId", "", "rId", "Companion", "app_debug"})
public final class IdentityManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String KEY_ALIAS = "RahatDeviceSecret";
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String ANDROID_KEYSTORE = "AndroidKeyStore";
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String PREFS_FILENAME = "rahat_secure_prefs";
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String KEY_RID = "persistent_rid";
    public static final long TIME_WINDOW_MS = 600000L;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy encryptedPrefs$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.rahat.security.IdentityManager.Companion Companion = null;
    
    public IdentityManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final android.content.SharedPreferences getEncryptedPrefs() {
        return null;
    }
    
    public final void savePersistentRId(@org.jetbrains.annotations.NotNull()
    java.lang.String rId) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPersistentRId() {
        return null;
    }
    
    public final boolean isEphIdValidForDevice(@org.jetbrains.annotations.NotNull()
    java.lang.String ephId, @org.jetbrains.annotations.NotNull()
    javax.crypto.SecretKey secret, long timeWindowMs) {
        return false;
    }
    
    @kotlin.jvm.Synchronized()
    @org.jetbrains.annotations.NotNull()
    public final synchronized javax.crypto.SecretKey getOrCreateDeviceSecret() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String generateEphemeralIdAtOffset(@org.jetbrains.annotations.NotNull()
    javax.crypto.SecretKey secret, long timeWindowMs, long windowOffset) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String generateRId() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/rahat/security/IdentityManager$Companion;", "", "()V", "TIME_WINDOW_MS", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}