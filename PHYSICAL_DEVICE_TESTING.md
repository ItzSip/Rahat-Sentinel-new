# Physical Android Test Flow: BLE + WebSocket

Follow these exact steps to run the Rahat Mobile App on a real Android device for full dual-hardware integration testing. **Do not use emulators for BLE testing.**

---

### Step 1: Network Configuration (CRITICAL)
Your phone and laptop must be on the **same Wi-Fi network**.
1. Open PowerShell and run `ipconfig`. Find your IPv4 Address (e.g., `192.168.1.10`).
2. Go to `src/config/env.ts` in the codebase.
3. Change `'ws://192.168.1.X:8000/ws/alerts'` to your exact PC IP (e.g., `'ws://192.168.1.10:8000/ws/alerts'`).

### Step 2: Verify USB Device
Plug your Android phone in via USB.
Ensure Developer Options and USB Debugging are enabled on the phone.
```bash
adb devices
```
> *If your phone shows `unauthorized`, look at your phone screen and tap "Allow USB debugging".*

### Step 3: Launch Metro Bundler
Start the local React Native server in a terminal:
```bash
npx react-native start
```

### Step 4: Install to Phone
Open a second terminal and push the compiled APK to your physical device:
```bash
npx react-native run-android
```
> *Verify Metro logs show the device downloading the bundle (100%).*

---

### Step 5: Test Real-Time WebSocket
1. Ensure the FastAPI backend is running via `uvicorn app.main:app --host 0.0.0.0 --port 8000`.
2. On your PC's terminal, blast a fake alert into Redis:
   ```bash
   curl http://localhost:8000/debug/redis-test
   ```
3. Look at your phone! The WebSocket should instantly log `[WS ALERT RECEIVED]` securely over the Wi-Fi network.

---

### Step 6: Test Offline Bluetooth Propagation
You need **Two Android Devices** for this test.
1. **Device A:** Keep connected to Wi-Fi. (Runs the steps above).
2. **Device B:** Turn ON Airplane Mode. Then explicitly turn ON Bluetooth. (Ensure the app is running).
3. Trigger the alert again via backend `curl`.
4. **Result:** Device A intercepts the WebSockets, then instantly executes its `[BLE BROADCAST]`. **Device B displays the incoming threat entirely offline!**
