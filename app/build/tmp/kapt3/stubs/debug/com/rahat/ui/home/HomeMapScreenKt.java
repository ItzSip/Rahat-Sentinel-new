package com.rahat.ui.home;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0090\u0001\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\u001aB\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u00052\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0007\u001a8\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0015\u0010\u0016\u001a\u001e\u0010\u0017\u001a\u00020\u00012\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u001a0\u00192\u0006\u0010\u001b\u001a\u00020\u001cH\u0007\u001an\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020!2\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u0006\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020(2\u0006\u0010\u0007\u001a\u00020\bH\u0007\u001a\u001e\u0010)\u001a\u00020\u00012\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0007\u001a\u0080\u0001\u0010*\u001a\u00020\u00012\u000e\u0010+\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010-0,2\f\u0010.\u001a\b\u0012\u0004\u0012\u00020/0\u00192\f\u00100\u001a\b\u0012\u0004\u0012\u00020\u001a0\u00192\f\u00101\u001a\b\u0012\u0004\u0012\u00020\u00110\u00192\u0012\u00102\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u00103\u001a\u000e\u0012\u0004\u0012\u00020/\u0012\u0004\u0012\u00020\u00010\u00052\u0006\u00104\u001a\u00020\n2\f\u00105\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0007\u001a\u0018\u00106\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u00107\u001a\u00020\nH\u0007\u001a\u0018\u00108\u001a\u0002092\u0006\u0010:\u001a\u00020;2\u0006\u0010\u0012\u001a\u00020<H\u0002\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006="}, d2 = {"DrawerContent", "", "onClose", "Lkotlin/Function0;", "onMenuAction", "Lkotlin/Function1;", "Lcom/rahat/state/MenuAction;", "narrator", "Lcom/rahat/service/Narrator;", "isNarratorEnabled", "", "narratorVolume", "", "DrawerItem", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "title", "", "color", "Landroidx/compose/ui/graphics/Color;", "onClick", "DrawerItem-9LQNqLg", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;JLkotlin/jvm/functions/Function0;)V", "EmergencyPanel", "peers", "", "Lcom/rahat/data/model/PeerState;", "modifier", "Landroidx/compose/ui/Modifier;", "HomeMapScreen", "uiState", "Lcom/rahat/state/UiState;", "mapViewModel", "Lcom/rahat/ui/home/MapViewModel;", "onNearbyHelpClick", "onStatusClick", "onOpenAlertFeed", "sosManager", "Lcom/rahat/ui/sos/SosManager;", "accessibilityPrefs", "Lcom/rahat/data/AccessibilityPreferences;", "MapOptionItem", "OsmMapView", "userLocation", "Landroidx/compose/runtime/State;", "Lorg/osmdroid/util/GeoPoint;", "alerts", "Lcom/rahat/data/model/Alert;", "nearbyPeers", "acknowledgedAlertIds", "onAlertAck", "onAlertClick", "shouldCenter", "onCentered", "StatusIcon", "enabled", "createSimpleIcon", "Landroid/graphics/drawable/Drawable;", "context", "Landroid/content/Context;", "", "app_debug"})
public final class HomeMapScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    @androidx.compose.runtime.Composable()
    public static final void HomeMapScreen(@org.jetbrains.annotations.NotNull()
    com.rahat.state.UiState uiState, @org.jetbrains.annotations.NotNull()
    com.rahat.ui.home.MapViewModel mapViewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNearbyHelpClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onStatusClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.rahat.state.MenuAction, kotlin.Unit> onMenuAction, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onOpenAlertFeed, @org.jetbrains.annotations.NotNull()
    com.rahat.ui.sos.SosManager sosManager, @org.jetbrains.annotations.NotNull()
    com.rahat.data.AccessibilityPreferences accessibilityPrefs, @org.jetbrains.annotations.NotNull()
    com.rahat.service.Narrator narrator) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void OsmMapView(@org.jetbrains.annotations.NotNull()
    androidx.compose.runtime.State<? extends org.osmdroid.util.GeoPoint> userLocation, @org.jetbrains.annotations.NotNull()
    java.util.List<com.rahat.data.model.Alert> alerts, @org.jetbrains.annotations.NotNull()
    java.util.List<com.rahat.data.model.PeerState> nearbyPeers, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> acknowledgedAlertIds, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onAlertAck, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.rahat.data.model.Alert, kotlin.Unit> onAlertClick, boolean shouldCenter, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onCentered) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void EmergencyPanel(@org.jetbrains.annotations.NotNull()
    java.util.List<com.rahat.data.model.PeerState> peers, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    private static final android.graphics.drawable.Drawable createSimpleIcon(android.content.Context context, int color) {
        return null;
    }
    
    @androidx.compose.runtime.Composable()
    public static final void DrawerContent(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClose, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.rahat.state.MenuAction, kotlin.Unit> onMenuAction, @org.jetbrains.annotations.NotNull()
    com.rahat.service.Narrator narrator, boolean isNarratorEnabled, float narratorVolume) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void StatusIcon(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.graphics.vector.ImageVector icon, boolean enabled) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void MapOptionItem(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}