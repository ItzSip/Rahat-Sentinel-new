package com.rahat.ui.alert;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\"\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\u001a$\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006H\u0007\u001a\u0016\u0010\u0007\u001a\u00020\u00012\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0003H\u0007\u001a\u0010\u0010\n\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\tH\u0007\u001a\u0010\u0010\f\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u0004H\u0007\u001a\u0016\u0010\u000e\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u0007\u00a8\u0006\u000f"}, d2 = {"AlertFeedScreen", "", "peers", "", "Lcom/rahat/data/model/PeerState;", "onBackClick", "Lkotlin/Function0;", "ClimateForecastContent", "alerts", "Lcom/rahat/ui/alert/ClimateRisk;", "ClimateRiskCard", "alert", "PeerAlertCard", "peer", "PeerListContent", "app_debug"})
public final class AlertFeedScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void AlertFeedScreen(@org.jetbrains.annotations.NotNull()
    java.util.List<com.rahat.data.model.PeerState> peers, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBackClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ClimateForecastContent(@org.jetbrains.annotations.NotNull()
    java.util.List<com.rahat.ui.alert.ClimateRisk> alerts) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ClimateRiskCard(@org.jetbrains.annotations.NotNull()
    com.rahat.ui.alert.ClimateRisk alert) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void PeerListContent(@org.jetbrains.annotations.NotNull()
    java.util.List<com.rahat.data.model.PeerState> peers) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void PeerAlertCard(@org.jetbrains.annotations.NotNull()
    com.rahat.data.model.PeerState peer) {
    }
}