package com.rahat.service.nearby;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000h\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u0003J\u0010\u0010$\u001a\u00020!2\u0006\u0010%\u001a\u00020&H\u0002J0\u0010\'\u001a\u00020!2\u0006\u0010(\u001a\u00020#2\u0006\u0010)\u001a\u00020#2\u0006\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020+2\u0006\u0010-\u001a\u00020#H\u0007J\b\u0010.\u001a\u00020!H\u0007J\b\u0010/\u001a\u00020!H\u0007J\b\u00100\u001a\u00020!H\u0003J\b\u00101\u001a\u00020!H\u0007R\u0016\u0010\u0005\u001a\n \u0007*\u0004\u0018\u00010\u00060\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\b\u001a\u0004\u0018\u00010\t8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\f\u0010\r\u001a\u0004\b\n\u0010\u000bR\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0010\u001a\u0004\u0018\u00010\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0014\u0010\r\u001a\u0004\b\u0012\u0010\u0013R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0019\u001a\u0004\u0018\u00010\u001a8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001d\u0010\r\u001a\u0004\b\u001b\u0010\u001cR\u000e\u0010\u001e\u001a\u00020\u001fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00062"}, d2 = {"Lcom/rahat/service/nearby/NearbyManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "SERVICE_UUID", "Landroid/os/ParcelUuid;", "kotlin.jvm.PlatformType", "advertiser", "Landroid/bluetooth/le/BluetoothLeAdvertiser;", "getAdvertiser", "()Landroid/bluetooth/le/BluetoothLeAdvertiser;", "advertiser$delegate", "Lkotlin/Lazy;", "advertisingJob", "Lkotlinx/coroutines/Job;", "bluetoothAdapter", "Landroid/bluetooth/BluetoothAdapter;", "getBluetoothAdapter", "()Landroid/bluetooth/BluetoothAdapter;", "bluetoothAdapter$delegate", "currentAdvCallback", "Landroid/bluetooth/le/AdvertiseCallback;", "scanCallback", "Landroid/bluetooth/le/ScanCallback;", "scanner", "Landroid/bluetooth/le/BluetoothLeScanner;", "getScanner", "()Landroid/bluetooth/le/BluetoothLeScanner;", "scanner$delegate", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "broadcastPayload", "", "payload", "", "processScanResult", "result", "Landroid/bluetooth/le/ScanResult;", "startAdvertisingLoop", "rId", "name", "lat", "", "lng", "severity", "startScanning", "stopAdvertising", "stopCurrentAdvertisement", "stopScanning", "app_debug"})
public final class NearbyManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy bluetoothAdapter$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy advertiser$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy scanner$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final android.bluetooth.le.ScanCallback scanCallback = null;
    private final android.os.ParcelUuid SERVICE_UUID = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job advertisingJob;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    @org.jetbrains.annotations.Nullable()
    private android.bluetooth.le.AdvertiseCallback currentAdvCallback;
    
    public NearbyManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final android.bluetooth.BluetoothAdapter getBluetoothAdapter() {
        return null;
    }
    
    private final android.bluetooth.le.BluetoothLeAdvertiser getAdvertiser() {
        return null;
    }
    
    private final android.bluetooth.le.BluetoothLeScanner getScanner() {
        return null;
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    public final void startAdvertisingLoop(@org.jetbrains.annotations.NotNull()
    java.lang.String rId, @org.jetbrains.annotations.NotNull()
    java.lang.String name, double lat, double lng, @org.jetbrains.annotations.NotNull()
    java.lang.String severity) {
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    private final void broadcastPayload(java.lang.String payload) {
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    public final void stopAdvertising() {
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    private final void stopCurrentAdvertisement() {
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    public final void startScanning() {
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    public final void stopScanning() {
    }
    
    private final void processScanResult(android.bluetooth.le.ScanResult result) {
    }
}