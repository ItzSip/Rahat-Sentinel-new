package com.rahat.service.ble;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000h\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J(\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020!2\u0006\u0010#\u001a\u00020$H\u0002J(\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020!2\u0006\u0010(\u001a\u00020$H\u0007J4\u0010)\u001a\u00020&2*\u0010*\u001a&\u0012\u0004\u0012\u00020\u001f\u0012\u0004\u0012\u00020!\u0012\u0004\u0012\u00020!\u0012\u0004\u0012\u00020$\u0012\u0004\u0012\u00020$\u0012\u0004\u0012\u00020&0+H\u0007J\b\u0010,\u001a\u00020&H\u0007J\b\u0010-\u001a\u00020&H\u0007R\u0016\u0010\u0005\u001a\n \u0007*\u0004\u0018\u00010\u00060\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\n\u001a\u0004\u0018\u00010\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\f\u0010\rR\u001d\u0010\u0010\u001a\u0004\u0018\u00010\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0014\u0010\u000f\u001a\u0004\b\u0012\u0010\u0013R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0017\u001a\u0004\u0018\u00010\u00188BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001b\u0010\u000f\u001a\u0004\b\u0019\u0010\u001a\u00a8\u0006."}, d2 = {"Lcom/rahat/service/ble/BleBeaconManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "SERVICE_UUID", "Landroid/os/ParcelUuid;", "kotlin.jvm.PlatformType", "advCallback", "Landroid/bluetooth/le/AdvertiseCallback;", "advertiser", "Landroid/bluetooth/le/BluetoothLeAdvertiser;", "getAdvertiser", "()Landroid/bluetooth/le/BluetoothLeAdvertiser;", "advertiser$delegate", "Lkotlin/Lazy;", "bluetoothAdapter", "Landroid/bluetooth/BluetoothAdapter;", "getBluetoothAdapter", "()Landroid/bluetooth/BluetoothAdapter;", "bluetoothAdapter$delegate", "scanCallback", "Landroid/bluetooth/le/ScanCallback;", "scanner", "Landroid/bluetooth/le/BluetoothLeScanner;", "getScanner", "()Landroid/bluetooth/le/BluetoothLeScanner;", "scanner$delegate", "buildPayload", "", "ephId", "", "lat", "", "lng", "bat", "", "startAdvertising", "", "ephemeralId", "battery", "startScanning", "onPeerFound", "Lkotlin/Function5;", "stopAdvertising", "stopScanning", "app_debug"})
public final class BleBeaconManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy bluetoothAdapter$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy advertiser$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy scanner$delegate = null;
    private final android.os.ParcelUuid SERVICE_UUID = null;
    @org.jetbrains.annotations.NotNull()
    private final android.bluetooth.le.AdvertiseCallback advCallback = null;
    @org.jetbrains.annotations.Nullable()
    private android.bluetooth.le.ScanCallback scanCallback;
    
    public BleBeaconManager(@org.jetbrains.annotations.NotNull()
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
    public final void startAdvertising(@org.jetbrains.annotations.NotNull()
    java.lang.String ephemeralId, double lat, double lng, int battery) {
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    public final void stopAdvertising() {
    }
    
    private final byte[] buildPayload(java.lang.String ephId, double lat, double lng, int bat) {
        return null;
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    public final void startScanning(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function5<? super java.lang.String, ? super java.lang.Double, ? super java.lang.Double, ? super java.lang.Integer, ? super java.lang.Integer, kotlin.Unit> onPeerFound) {
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    public final void stopScanning() {
    }
}