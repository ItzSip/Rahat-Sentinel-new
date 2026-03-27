package com.rahat.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.rahat.data.local.entity.DeviceEntity;
import com.rahat.data.local.entity.EmergencyContactEntity;
import com.rahat.data.local.entity.EphemeralIdEntity;
import com.rahat.data.local.entity.SeenPeerEntity;
import com.rahat.data.local.entity.UserProfileEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class RahatDao_Impl implements RahatDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DeviceEntity> __insertionAdapterOfDeviceEntity;

  private final EntityInsertionAdapter<EphemeralIdEntity> __insertionAdapterOfEphemeralIdEntity;

  private final EntityInsertionAdapter<SeenPeerEntity> __insertionAdapterOfSeenPeerEntity;

  private final EntityInsertionAdapter<UserProfileEntity> __insertionAdapterOfUserProfileEntity;

  private final EntityInsertionAdapter<EmergencyContactEntity> __insertionAdapterOfEmergencyContactEntity;

  private final SharedSQLiteStatement __preparedStmtOfCleanOldPeers;

  private final SharedSQLiteStatement __preparedStmtOfUpdateUserName;

  private final SharedSQLiteStatement __preparedStmtOfDeleteContact;

  public RahatDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDeviceEntity = new EntityInsertionAdapter<DeviceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `device_table` (`rId`,`createdAt`) VALUES (?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DeviceEntity entity) {
        if (entity.getRId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getRId());
        }
        statement.bindLong(2, entity.getCreatedAt());
      }
    };
    this.__insertionAdapterOfEphemeralIdEntity = new EntityInsertionAdapter<EphemeralIdEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `ephemeral_id_table` (`ephemeralId`,`generatedAt`,`expiresAt`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EphemeralIdEntity entity) {
        if (entity.getEphemeralId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getEphemeralId());
        }
        statement.bindLong(2, entity.getGeneratedAt());
        statement.bindLong(3, entity.getExpiresAt());
      }
    };
    this.__insertionAdapterOfSeenPeerEntity = new EntityInsertionAdapter<SeenPeerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `seen_peers_table` (`ephemeralId`,`avgRssi`,`lastSeen`,`coarseLat`,`coarseLon`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SeenPeerEntity entity) {
        if (entity.getEphemeralId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getEphemeralId());
        }
        statement.bindLong(2, entity.getAvgRssi());
        statement.bindLong(3, entity.getLastSeen());
        statement.bindDouble(4, entity.getCoarseLat());
        statement.bindDouble(5, entity.getCoarseLon());
      }
    };
    this.__insertionAdapterOfUserProfileEntity = new EntityInsertionAdapter<UserProfileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_profile_table` (`rId`,`phone`,`name`,`settingsJson`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserProfileEntity entity) {
        if (entity.getRId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getRId());
        }
        if (entity.getPhone() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPhone());
        }
        if (entity.getName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getName());
        }
        if (entity.getSettingsJson() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSettingsJson());
        }
      }
    };
    this.__insertionAdapterOfEmergencyContactEntity = new EntityInsertionAdapter<EmergencyContactEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `emergency_contact_table` (`id`,`ownerId`,`name`,`relation`,`phone`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EmergencyContactEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getOwnerId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getOwnerId());
        }
        if (entity.getName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getName());
        }
        if (entity.getRelation() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getRelation());
        }
        if (entity.getPhone() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getPhone());
        }
      }
    };
    this.__preparedStmtOfCleanOldPeers = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM seen_peers_table WHERE lastSeen < ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateUserName = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE user_profile_table SET name = ? WHERE rId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteContact = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM emergency_contact_table WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertDevice(final DeviceEntity device,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDeviceEntity.insert(device);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertEphemeralId(final EphemeralIdEntity id,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEphemeralIdEntity.insert(id);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSeenPeer(final SeenPeerEntity peer,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSeenPeerEntity.insert(peer);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertUserProfile(final UserProfileEntity profile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserProfileEntity.insert(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertContact(final EmergencyContactEntity contact,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEmergencyContactEntity.insert(contact);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object cleanOldPeers(final long timestamp, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfCleanOldPeers.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfCleanOldPeers.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateUserName(final String rId, final String name,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateUserName.acquire();
        int _argIndex = 1;
        if (name == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, name);
        }
        _argIndex = 2;
        if (rId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, rId);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateUserName.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteContact(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteContact.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteContact.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<DeviceEntity> getDevice() {
    final String _sql = "SELECT * FROM device_table LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"device_table"}, new Callable<DeviceEntity>() {
      @Override
      @Nullable
      public DeviceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRId = CursorUtil.getColumnIndexOrThrow(_cursor, "rId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final DeviceEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpRId;
            if (_cursor.isNull(_cursorIndexOfRId)) {
              _tmpRId = null;
            } else {
              _tmpRId = _cursor.getString(_cursorIndexOfRId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new DeviceEntity(_tmpRId,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getDeviceOneShot(final Continuation<? super DeviceEntity> $completion) {
    final String _sql = "SELECT * FROM device_table LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DeviceEntity>() {
      @Override
      @Nullable
      public DeviceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRId = CursorUtil.getColumnIndexOrThrow(_cursor, "rId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final DeviceEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpRId;
            if (_cursor.isNull(_cursorIndexOfRId)) {
              _tmpRId = null;
            } else {
              _tmpRId = _cursor.getString(_cursorIndexOfRId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new DeviceEntity(_tmpRId,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCurrentEphemeralId(final Continuation<? super EphemeralIdEntity> $completion) {
    final String _sql = "SELECT * FROM ephemeral_id_table ORDER BY generatedAt DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EphemeralIdEntity>() {
      @Override
      @Nullable
      public EphemeralIdEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfEphemeralId = CursorUtil.getColumnIndexOrThrow(_cursor, "ephemeralId");
          final int _cursorIndexOfGeneratedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "generatedAt");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final EphemeralIdEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpEphemeralId;
            if (_cursor.isNull(_cursorIndexOfEphemeralId)) {
              _tmpEphemeralId = null;
            } else {
              _tmpEphemeralId = _cursor.getString(_cursorIndexOfEphemeralId);
            }
            final long _tmpGeneratedAt;
            _tmpGeneratedAt = _cursor.getLong(_cursorIndexOfGeneratedAt);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            _result = new EphemeralIdEntity(_tmpEphemeralId,_tmpGeneratedAt,_tmpExpiresAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SeenPeerEntity>> getActivePeers(final long minTimestamp) {
    final String _sql = "SELECT * FROM seen_peers_table WHERE lastSeen > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, minTimestamp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"seen_peers_table"}, new Callable<List<SeenPeerEntity>>() {
      @Override
      @NonNull
      public List<SeenPeerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfEphemeralId = CursorUtil.getColumnIndexOrThrow(_cursor, "ephemeralId");
          final int _cursorIndexOfAvgRssi = CursorUtil.getColumnIndexOrThrow(_cursor, "avgRssi");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeen");
          final int _cursorIndexOfCoarseLat = CursorUtil.getColumnIndexOrThrow(_cursor, "coarseLat");
          final int _cursorIndexOfCoarseLon = CursorUtil.getColumnIndexOrThrow(_cursor, "coarseLon");
          final List<SeenPeerEntity> _result = new ArrayList<SeenPeerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SeenPeerEntity _item;
            final String _tmpEphemeralId;
            if (_cursor.isNull(_cursorIndexOfEphemeralId)) {
              _tmpEphemeralId = null;
            } else {
              _tmpEphemeralId = _cursor.getString(_cursorIndexOfEphemeralId);
            }
            final int _tmpAvgRssi;
            _tmpAvgRssi = _cursor.getInt(_cursorIndexOfAvgRssi);
            final long _tmpLastSeen;
            _tmpLastSeen = _cursor.getLong(_cursorIndexOfLastSeen);
            final double _tmpCoarseLat;
            _tmpCoarseLat = _cursor.getDouble(_cursorIndexOfCoarseLat);
            final double _tmpCoarseLon;
            _tmpCoarseLon = _cursor.getDouble(_cursorIndexOfCoarseLon);
            _item = new SeenPeerEntity(_tmpEphemeralId,_tmpAvgRssi,_tmpLastSeen,_tmpCoarseLat,_tmpCoarseLon);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<UserProfileEntity> getUserProfile(final String rId) {
    final String _sql = "SELECT * FROM user_profile_table WHERE rId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (rId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, rId);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_profile_table"}, new Callable<UserProfileEntity>() {
      @Override
      @Nullable
      public UserProfileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRId = CursorUtil.getColumnIndexOrThrow(_cursor, "rId");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSettingsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "settingsJson");
          final UserProfileEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpRId;
            if (_cursor.isNull(_cursorIndexOfRId)) {
              _tmpRId = null;
            } else {
              _tmpRId = _cursor.getString(_cursorIndexOfRId);
            }
            final String _tmpPhone;
            if (_cursor.isNull(_cursorIndexOfPhone)) {
              _tmpPhone = null;
            } else {
              _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpSettingsJson;
            if (_cursor.isNull(_cursorIndexOfSettingsJson)) {
              _tmpSettingsJson = null;
            } else {
              _tmpSettingsJson = _cursor.getString(_cursorIndexOfSettingsJson);
            }
            _result = new UserProfileEntity(_tmpRId,_tmpPhone,_tmpName,_tmpSettingsJson);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<EmergencyContactEntity>> getContacts(final String ownerId) {
    final String _sql = "SELECT * FROM emergency_contact_table WHERE ownerId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (ownerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ownerId);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"emergency_contact_table"}, new Callable<List<EmergencyContactEntity>>() {
      @Override
      @NonNull
      public List<EmergencyContactEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRelation = CursorUtil.getColumnIndexOrThrow(_cursor, "relation");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final List<EmergencyContactEntity> _result = new ArrayList<EmergencyContactEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EmergencyContactEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpOwnerId;
            if (_cursor.isNull(_cursorIndexOfOwnerId)) {
              _tmpOwnerId = null;
            } else {
              _tmpOwnerId = _cursor.getString(_cursorIndexOfOwnerId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpRelation;
            if (_cursor.isNull(_cursorIndexOfRelation)) {
              _tmpRelation = null;
            } else {
              _tmpRelation = _cursor.getString(_cursorIndexOfRelation);
            }
            final String _tmpPhone;
            if (_cursor.isNull(_cursorIndexOfPhone)) {
              _tmpPhone = null;
            } else {
              _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            }
            _item = new EmergencyContactEntity(_tmpId,_tmpOwnerId,_tmpName,_tmpRelation,_tmpPhone);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
