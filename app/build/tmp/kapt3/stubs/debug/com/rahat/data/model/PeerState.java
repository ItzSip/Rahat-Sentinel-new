package com.rahat.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u001e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B_\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u000f\u00a2\u0006\u0002\u0010\u0011J\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\t\u0010#\u001a\u00020\u0003H\u00c6\u0003J\t\u0010$\u001a\u00020\u0003H\u00c6\u0003J\t\u0010%\u001a\u00020\u0007H\u00c6\u0003J\t\u0010&\u001a\u00020\tH\u00c6\u0003J\t\u0010\'\u001a\u00020\u000bH\u00c6\u0003J\t\u0010(\u001a\u00020\rH\u00c6\u0003J\u0010\u0010)\u001a\u0004\u0018\u00010\u000fH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0015J\u0010\u0010*\u001a\u0004\u0018\u00010\u000fH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0015Jl\u0010+\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\r2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u000fH\u00c6\u0001\u00a2\u0006\u0002\u0010,J\u0013\u0010-\u001a\u00020.2\b\u0010/\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00100\u001a\u000201H\u00d6\u0001J\t\u00102\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0015\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u00a2\u0006\n\n\u0002\u0010\u0016\u001a\u0004\b\u0014\u0010\u0015R\u0015\u0010\u0010\u001a\u0004\u0018\u00010\u000f\u00a2\u0006\n\n\u0002\u0010\u0016\u001a\u0004\b\u0017\u0010\u0015R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0019R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0019R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!\u00a8\u00063"}, d2 = {"Lcom/rahat/data/model/PeerState;", "", "rId", "", "name", "severity", "signalLevel", "Lcom/rahat/data/model/SignalLevel;", "signalTrend", "Lcom/rahat/data/model/SignalTrend;", "lastSeen", "", "source", "Lcom/rahat/data/model/PeerSource;", "latitude", "", "longitude", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/rahat/data/model/SignalLevel;Lcom/rahat/data/model/SignalTrend;JLcom/rahat/data/model/PeerSource;Ljava/lang/Double;Ljava/lang/Double;)V", "getLastSeen", "()J", "getLatitude", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getLongitude", "getName", "()Ljava/lang/String;", "getRId", "getSeverity", "getSignalLevel", "()Lcom/rahat/data/model/SignalLevel;", "getSignalTrend", "()Lcom/rahat/data/model/SignalTrend;", "getSource", "()Lcom/rahat/data/model/PeerSource;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/rahat/data/model/SignalLevel;Lcom/rahat/data/model/SignalTrend;JLcom/rahat/data/model/PeerSource;Ljava/lang/Double;Ljava/lang/Double;)Lcom/rahat/data/model/PeerState;", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class PeerState {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String rId = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String name = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String severity = null;
    @org.jetbrains.annotations.NotNull()
    private final com.rahat.data.model.SignalLevel signalLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final com.rahat.data.model.SignalTrend signalTrend = null;
    private final long lastSeen = 0L;
    @org.jetbrains.annotations.NotNull()
    private final com.rahat.data.model.PeerSource source = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double latitude = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double longitude = null;
    
    public PeerState(@org.jetbrains.annotations.NotNull()
    java.lang.String rId, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String severity, @org.jetbrains.annotations.NotNull()
    com.rahat.data.model.SignalLevel signalLevel, @org.jetbrains.annotations.NotNull()
    com.rahat.data.model.SignalTrend signalTrend, long lastSeen, @org.jetbrains.annotations.NotNull()
    com.rahat.data.model.PeerSource source, @org.jetbrains.annotations.Nullable()
    java.lang.Double latitude, @org.jetbrains.annotations.Nullable()
    java.lang.Double longitude) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSeverity() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rahat.data.model.SignalLevel getSignalLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rahat.data.model.SignalTrend getSignalTrend() {
        return null;
    }
    
    public final long getLastSeen() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rahat.data.model.PeerSource getSource() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getLatitude() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getLongitude() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rahat.data.model.SignalLevel component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rahat.data.model.SignalTrend component5() {
        return null;
    }
    
    public final long component6() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rahat.data.model.PeerSource component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rahat.data.model.PeerState copy(@org.jetbrains.annotations.NotNull()
    java.lang.String rId, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String severity, @org.jetbrains.annotations.NotNull()
    com.rahat.data.model.SignalLevel signalLevel, @org.jetbrains.annotations.NotNull()
    com.rahat.data.model.SignalTrend signalTrend, long lastSeen, @org.jetbrains.annotations.NotNull()
    com.rahat.data.model.PeerSource source, @org.jetbrains.annotations.Nullable()
    java.lang.Double latitude, @org.jetbrains.annotations.Nullable()
    java.lang.Double longitude) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}