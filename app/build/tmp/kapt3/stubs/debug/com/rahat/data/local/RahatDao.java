package com.rahat.data.local;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0013\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\f2\u0006\u0010\u000f\u001a\u00020\u0005H\'J\u001c\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\r0\f2\u0006\u0010\u0012\u001a\u00020\u0013H\'J\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u0010\u0010\u0017\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00180\fH\'J\u0010\u0010\u0019\u001a\u0004\u0018\u00010\u0018H\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u0018\u0010\u001a\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001b0\f2\u0006\u0010\u001c\u001a\u00020\u0013H\'J\u0016\u0010\u001d\u001a\u00020\u00032\u0006\u0010\u001e\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u001fJ\u0016\u0010 \u001a\u00020\u00032\u0006\u0010!\u001a\u00020\u0018H\u00a7@\u00a2\u0006\u0002\u0010\"J\u0016\u0010#\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u0015H\u00a7@\u00a2\u0006\u0002\u0010$J\u0016\u0010%\u001a\u00020\u00032\u0006\u0010&\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\'J\u0016\u0010(\u001a\u00020\u00032\u0006\u0010)\u001a\u00020\u001bH\u00a7@\u00a2\u0006\u0002\u0010*J\u001e\u0010+\u001a\u00020\u00032\u0006\u0010\u001c\u001a\u00020\u00132\u0006\u0010,\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010-\u00a8\u0006."}, d2 = {"Lcom/rahat/data/local/RahatDao;", "", "cleanOldPeers", "", "timestamp", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteContact", "id", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getActivePeers", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/rahat/data/local/entity/SeenPeerEntity;", "minTimestamp", "getContacts", "Lcom/rahat/data/local/entity/EmergencyContactEntity;", "ownerId", "", "getCurrentEphemeralId", "Lcom/rahat/data/local/entity/EphemeralIdEntity;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDevice", "Lcom/rahat/data/local/entity/DeviceEntity;", "getDeviceOneShot", "getUserProfile", "Lcom/rahat/data/local/entity/UserProfileEntity;", "rId", "insertContact", "contact", "(Lcom/rahat/data/local/entity/EmergencyContactEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertDevice", "device", "(Lcom/rahat/data/local/entity/DeviceEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertEphemeralId", "(Lcom/rahat/data/local/entity/EphemeralIdEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertSeenPeer", "peer", "(Lcom/rahat/data/local/entity/SeenPeerEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertUserProfile", "profile", "(Lcom/rahat/data/local/entity/UserProfileEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateUserName", "name", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface RahatDao {
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertDevice(@org.jetbrains.annotations.NotNull()
    com.rahat.data.local.entity.DeviceEntity device, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM device_table LIMIT 1")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.rahat.data.local.entity.DeviceEntity> getDevice();
    
    @androidx.room.Query(value = "SELECT * FROM device_table LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getDeviceOneShot(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.rahat.data.local.entity.DeviceEntity> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertEphemeralId(@org.jetbrains.annotations.NotNull()
    com.rahat.data.local.entity.EphemeralIdEntity id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM ephemeral_id_table ORDER BY generatedAt DESC LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCurrentEphemeralId(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.rahat.data.local.entity.EphemeralIdEntity> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertSeenPeer(@org.jetbrains.annotations.NotNull()
    com.rahat.data.local.entity.SeenPeerEntity peer, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM seen_peers_table WHERE lastSeen > :minTimestamp")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.rahat.data.local.entity.SeenPeerEntity>> getActivePeers(long minTimestamp);
    
    @androidx.room.Query(value = "DELETE FROM seen_peers_table WHERE lastSeen < :timestamp")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object cleanOldPeers(long timestamp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertUserProfile(@org.jetbrains.annotations.NotNull()
    com.rahat.data.local.entity.UserProfileEntity profile, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM user_profile_table WHERE rId = :rId")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.rahat.data.local.entity.UserProfileEntity> getUserProfile(@org.jetbrains.annotations.NotNull()
    java.lang.String rId);
    
    @androidx.room.Query(value = "UPDATE user_profile_table SET name = :name WHERE rId = :rId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateUserName(@org.jetbrains.annotations.NotNull()
    java.lang.String rId, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertContact(@org.jetbrains.annotations.NotNull()
    com.rahat.data.local.entity.EmergencyContactEntity contact, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM emergency_contact_table WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteContact(int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM emergency_contact_table WHERE ownerId = :ownerId")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.rahat.data.local.entity.EmergencyContactEntity>> getContacts(@org.jetbrains.annotations.NotNull()
    java.lang.String ownerId);
}