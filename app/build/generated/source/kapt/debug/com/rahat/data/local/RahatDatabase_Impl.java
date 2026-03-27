package com.rahat.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class RahatDatabase_Impl extends RahatDatabase {
  private volatile RahatDao _rahatDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `device_table` (`rId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`rId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `ephemeral_id_table` (`ephemeralId` TEXT NOT NULL, `generatedAt` INTEGER NOT NULL, `expiresAt` INTEGER NOT NULL, PRIMARY KEY(`ephemeralId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `seen_peers_table` (`ephemeralId` TEXT NOT NULL, `avgRssi` INTEGER NOT NULL, `lastSeen` INTEGER NOT NULL, `coarseLat` REAL NOT NULL, `coarseLon` REAL NOT NULL, PRIMARY KEY(`ephemeralId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_profile_table` (`rId` TEXT NOT NULL, `phone` TEXT NOT NULL, `name` TEXT NOT NULL, `settingsJson` TEXT NOT NULL, PRIMARY KEY(`rId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `emergency_contact_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ownerId` TEXT NOT NULL, `name` TEXT NOT NULL, `relation` TEXT NOT NULL, `phone` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '0cf3fb87a7921c2fd191517276ab01d4')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `device_table`");
        db.execSQL("DROP TABLE IF EXISTS `ephemeral_id_table`");
        db.execSQL("DROP TABLE IF EXISTS `seen_peers_table`");
        db.execSQL("DROP TABLE IF EXISTS `user_profile_table`");
        db.execSQL("DROP TABLE IF EXISTS `emergency_contact_table`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsDeviceTable = new HashMap<String, TableInfo.Column>(2);
        _columnsDeviceTable.put("rId", new TableInfo.Column("rId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceTable.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDeviceTable = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDeviceTable = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDeviceTable = new TableInfo("device_table", _columnsDeviceTable, _foreignKeysDeviceTable, _indicesDeviceTable);
        final TableInfo _existingDeviceTable = TableInfo.read(db, "device_table");
        if (!_infoDeviceTable.equals(_existingDeviceTable)) {
          return new RoomOpenHelper.ValidationResult(false, "device_table(com.rahat.data.local.entity.DeviceEntity).\n"
                  + " Expected:\n" + _infoDeviceTable + "\n"
                  + " Found:\n" + _existingDeviceTable);
        }
        final HashMap<String, TableInfo.Column> _columnsEphemeralIdTable = new HashMap<String, TableInfo.Column>(3);
        _columnsEphemeralIdTable.put("ephemeralId", new TableInfo.Column("ephemeralId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEphemeralIdTable.put("generatedAt", new TableInfo.Column("generatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEphemeralIdTable.put("expiresAt", new TableInfo.Column("expiresAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEphemeralIdTable = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEphemeralIdTable = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEphemeralIdTable = new TableInfo("ephemeral_id_table", _columnsEphemeralIdTable, _foreignKeysEphemeralIdTable, _indicesEphemeralIdTable);
        final TableInfo _existingEphemeralIdTable = TableInfo.read(db, "ephemeral_id_table");
        if (!_infoEphemeralIdTable.equals(_existingEphemeralIdTable)) {
          return new RoomOpenHelper.ValidationResult(false, "ephemeral_id_table(com.rahat.data.local.entity.EphemeralIdEntity).\n"
                  + " Expected:\n" + _infoEphemeralIdTable + "\n"
                  + " Found:\n" + _existingEphemeralIdTable);
        }
        final HashMap<String, TableInfo.Column> _columnsSeenPeersTable = new HashMap<String, TableInfo.Column>(5);
        _columnsSeenPeersTable.put("ephemeralId", new TableInfo.Column("ephemeralId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeenPeersTable.put("avgRssi", new TableInfo.Column("avgRssi", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeenPeersTable.put("lastSeen", new TableInfo.Column("lastSeen", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeenPeersTable.put("coarseLat", new TableInfo.Column("coarseLat", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeenPeersTable.put("coarseLon", new TableInfo.Column("coarseLon", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSeenPeersTable = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSeenPeersTable = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSeenPeersTable = new TableInfo("seen_peers_table", _columnsSeenPeersTable, _foreignKeysSeenPeersTable, _indicesSeenPeersTable);
        final TableInfo _existingSeenPeersTable = TableInfo.read(db, "seen_peers_table");
        if (!_infoSeenPeersTable.equals(_existingSeenPeersTable)) {
          return new RoomOpenHelper.ValidationResult(false, "seen_peers_table(com.rahat.data.local.entity.SeenPeerEntity).\n"
                  + " Expected:\n" + _infoSeenPeersTable + "\n"
                  + " Found:\n" + _existingSeenPeersTable);
        }
        final HashMap<String, TableInfo.Column> _columnsUserProfileTable = new HashMap<String, TableInfo.Column>(4);
        _columnsUserProfileTable.put("rId", new TableInfo.Column("rId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfileTable.put("phone", new TableInfo.Column("phone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfileTable.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfileTable.put("settingsJson", new TableInfo.Column("settingsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserProfileTable = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserProfileTable = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserProfileTable = new TableInfo("user_profile_table", _columnsUserProfileTable, _foreignKeysUserProfileTable, _indicesUserProfileTable);
        final TableInfo _existingUserProfileTable = TableInfo.read(db, "user_profile_table");
        if (!_infoUserProfileTable.equals(_existingUserProfileTable)) {
          return new RoomOpenHelper.ValidationResult(false, "user_profile_table(com.rahat.data.local.entity.UserProfileEntity).\n"
                  + " Expected:\n" + _infoUserProfileTable + "\n"
                  + " Found:\n" + _existingUserProfileTable);
        }
        final HashMap<String, TableInfo.Column> _columnsEmergencyContactTable = new HashMap<String, TableInfo.Column>(5);
        _columnsEmergencyContactTable.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContactTable.put("ownerId", new TableInfo.Column("ownerId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContactTable.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContactTable.put("relation", new TableInfo.Column("relation", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContactTable.put("phone", new TableInfo.Column("phone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEmergencyContactTable = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEmergencyContactTable = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEmergencyContactTable = new TableInfo("emergency_contact_table", _columnsEmergencyContactTable, _foreignKeysEmergencyContactTable, _indicesEmergencyContactTable);
        final TableInfo _existingEmergencyContactTable = TableInfo.read(db, "emergency_contact_table");
        if (!_infoEmergencyContactTable.equals(_existingEmergencyContactTable)) {
          return new RoomOpenHelper.ValidationResult(false, "emergency_contact_table(com.rahat.data.local.entity.EmergencyContactEntity).\n"
                  + " Expected:\n" + _infoEmergencyContactTable + "\n"
                  + " Found:\n" + _existingEmergencyContactTable);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "0cf3fb87a7921c2fd191517276ab01d4", "85edafb9f505ea62e19178070b5156e4");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "device_table","ephemeral_id_table","seen_peers_table","user_profile_table","emergency_contact_table");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `device_table`");
      _db.execSQL("DELETE FROM `ephemeral_id_table`");
      _db.execSQL("DELETE FROM `seen_peers_table`");
      _db.execSQL("DELETE FROM `user_profile_table`");
      _db.execSQL("DELETE FROM `emergency_contact_table`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(RahatDao.class, RahatDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public RahatDao rahatDao() {
    if (_rahatDao != null) {
      return _rahatDao;
    } else {
      synchronized(this) {
        if(_rahatDao == null) {
          _rahatDao = new RahatDao_Impl(this);
        }
        return _rahatDao;
      }
    }
  }
}
