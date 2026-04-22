/**
 * Shake Detector — backed by the native ShakeDetectorService.
 *
 * The service is a foreground service that registers a SensorManager listener
 * and runs even when the app is backgrounded or the screen is locked.
 * When a shake is detected, it:
 *   1. Writes to SharedPreferences (durable)
 *   2. Broadcasts com.rahat.SHAKE_DETECTED (picked up by ShakeModule if JS is alive)
 *
 * This module manages the JS-side subscription for the in-foreground path.
 * The background path is handled by useDisasterEffect's AppState listener
 * which calls ShakeModule.consumePendingAction() on every foreground resume.
 */

import { NativeModules, NativeEventEmitter, EmitterSubscription } from 'react-native';

const { ShakeModule } = NativeModules;

let emitterSub: EmitterSubscription | null = null;

export function startShakeDetection(onShake: () => void): void {
    if (!ShakeModule) {
        console.warn('[SHAKE] ShakeModule not found — rebuild the native app');
        return;
    }

    // Start the Android foreground service (survives app backgrounding)
    ShakeModule.startShakeService();

    // Also subscribe to the JS event for immediate in-foreground feedback
    const emitter = new NativeEventEmitter(ShakeModule);
    emitterSub = emitter.addListener('onShakeDetected', onShake);
}

export function stopShakeDetection(): void {
    ShakeModule?.stopShakeService();
    emitterSub?.remove();
    emitterSub = null;
}
