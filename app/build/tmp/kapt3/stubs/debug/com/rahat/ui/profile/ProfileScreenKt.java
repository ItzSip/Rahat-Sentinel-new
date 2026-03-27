package com.rahat.ui.profile;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000>\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a.\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u00032\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a.\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a&\u0010\u0010\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015H\u0002\u00a8\u0006\u0016"}, d2 = {"EmergencyContactItem", "", "name", "", "relation", "phone", "onDelete", "Lkotlin/Function0;", "ProfileScreen", "userRepo", "Lcom/rahat/data/firebase/FirestoreUserRepository;", "narrator", "Lcom/rahat/service/Narrator;", "accessibilityPrefs", "Lcom/rahat/data/AccessibilityPreferences;", "onBack", "getContactInfo", "Lkotlin/Pair;", "context", "Landroid/content/Context;", "contactUri", "Landroid/net/Uri;", "app_debug"})
public final class ProfileScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ProfileScreen(@org.jetbrains.annotations.NotNull()
    com.rahat.data.firebase.FirestoreUserRepository userRepo, @org.jetbrains.annotations.NotNull()
    com.rahat.service.Narrator narrator, @org.jetbrains.annotations.NotNull()
    com.rahat.data.AccessibilityPreferences accessibilityPrefs, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    private static final kotlin.Pair<java.lang.String, java.lang.String> getContactInfo(android.content.Context context, android.net.Uri contactUri) {
        return null;
    }
    
    @androidx.compose.runtime.Composable()
    public static final void EmergencyContactItem(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String relation, @org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDelete) {
    }
}