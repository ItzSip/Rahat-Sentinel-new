import { useSettingsStore } from '../store/settingsStore';

type Lang = 'English' | 'Hindi';

const S = {
    English: {
        // App
        appName: 'Rahat',
        // Navigation
        back: '← Back',
        // Sidebar / nav
        alertFeed: 'Alert Feed',
        earlyWarning: 'Early Warning',
        nearbyDevices: 'Nearby Devices',
        safeZones: 'Safe Zones',
        settings: 'Settings',
        profile: 'Profile',
        activeMode: 'Active Mode',
        logout: 'Logout',
        // Home bottom bar
        nearbyHelp: 'Nearby Help',
        nearbyCount: (n: number) => `Nearby · ${n}`,
        scanning: 'Scanning...',
        alerts: 'Alerts',
        // SettingsScreen
        accessibility: 'Accessibility',
        screenNarrator: 'Screen Narrator',
        general: 'General',
        darkMode: 'Dark Mode Theme',
        language: 'Language',
        disasterSimulation: 'Disaster Simulation',
        disasterHint: 'Activating this turns on all disaster features and automatically triggers disaster mode on every connected device via BLE.',
        simulateDisaster: 'Simulate Disaster',
        disasterActive: 'Active — severity timer running, BLE sync broadcasting',
        disasterInactive: 'Inactive — tap to trigger on all connected devices',
        disasterBanner: 'DISASTER SIMULATION ACTIVE — ALL DEVICES NOTIFIED',
        // AuthScreen
        tagline: 'Offline-first disaster response',
        yourName: 'Your name *',
        namePlaceholder: 'Enter your name',
        phoneLabel: 'Phone number (optional)',
        phonePlaceholder: '+91 XXXXXXXXXX',
        continueBtn: 'Continue →',
        storageNote: 'Stored locally on your device. No account needed.',
        // AlertFeedScreen
        activeAlerts: 'Active Alerts',
        noAlerts: 'No active alerts nearby.',
        // EarlyWarningScreen
        earlyWarnings: 'Early Warnings',
        noWarnings: 'No predictive risks currently detected.',
        // SafeZoneGuideScreen
        safeZonesTitle: 'Safe Zones',
        whatToDoNow: 'What to do now',
        // NearbyDevicesScreen
        peerNetwork: 'Peer Network',
        devicesFound: (n: number) => `${n} device${n !== 1 ? 's' : ''} found`,
        bleActive: 'BLE Active',
        bleIdle: 'BLE Idle',
        noPeers: 'No Peers Detected',
        noPeersBleHint: 'BLE mesh is scanning. Other Rahat devices will appear here when in range.',
        noPeersIdleHint: 'BLE mesh is idle. Restart the app to begin scanning.',
        // Narrator announcements
        narratorOn: 'Screen narrator enabled',
        narratorOff: 'Screen narrator disabled',
        langChanged: (lang: string) => `Language changed to ${lang}`,
        screenHome: 'Home screen. Map view with nearby devices and SOS button.',
        screenAlertFeed: 'Alert feed. View active emergency alerts.',
        screenEarlyWarning: 'Early warning screen. View predictive risk alerts.',
        screenNearbyDevices: 'Nearby devices screen. View connected Rahat peers.',
        screenSafeZones: 'Safe zone guide. View nearby safe locations.',
        screenSettings: 'Settings screen.',
        screenProfile: 'Profile screen.',
        disasterActivated: 'Disaster mode activated. Stay calm and check in regularly.',
        disasterDeactivated: 'Disaster mode deactivated.',
    },
    Hindi: {
        // App
        appName: 'राहत',
        // Navigation
        back: '← वापस',
        // Sidebar / nav
        alertFeed: 'अलर्ट फ़ीड',
        earlyWarning: 'प्रारंभिक चेतावनी',
        nearbyDevices: 'नज़दीकी उपकरण',
        safeZones: 'सुरक्षित क्षेत्र',
        settings: 'सेटिंग्स',
        profile: 'प्रोफाइल',
        activeMode: 'सक्रिय मोड',
        logout: 'लॉगआउट',
        // Home bottom bar
        nearbyHelp: 'नज़दीकी मदद',
        nearbyCount: (n: number) => `नज़दीक · ${n}`,
        scanning: 'स्कैनिंग...',
        alerts: 'अलर्ट',
        // SettingsScreen
        accessibility: 'पहुँच',
        screenNarrator: 'स्क्रीन वाचक',
        general: 'सामान्य',
        darkMode: 'डार्क मोड थीम',
        language: 'भाषा',
        disasterSimulation: 'आपदा सिमुलेशन',
        disasterHint: 'इसे सक्रिय करने से सभी आपदा सुविधाएँ चालू होती हैं और BLE के माध्यम से हर जुड़े डिवाइस पर आपदा मोड ट्रिगर होता है।',
        simulateDisaster: 'आपदा सिमुलेट करें',
        disasterActive: 'सक्रिय — गंभीरता टाइमर चल रहा है, BLE सिंक प्रसारण हो रहा है',
        disasterInactive: 'निष्क्रिय — सभी जुड़े उपकरणों पर ट्रिगर करने के लिए टैप करें',
        disasterBanner: 'आपदा सिमुलेशन सक्रिय — सभी उपकरणों को सूचित किया गया',
        // AuthScreen
        tagline: 'ऑफलाइन-फर्स्ट आपदा प्रतिक्रिया',
        yourName: 'आपका नाम *',
        namePlaceholder: 'अपना नाम दर्ज करें',
        phoneLabel: 'फ़ोन नंबर (वैकल्पिक)',
        phonePlaceholder: '+91 XXXXXXXXXX',
        continueBtn: 'जारी रखें →',
        storageNote: 'आपके डिवाइस पर स्थानीय रूप से संग्रहीत। कोई खाता आवश्यक नहीं।',
        // AlertFeedScreen
        activeAlerts: 'सक्रिय अलर्ट',
        noAlerts: 'आस-पास कोई सक्रिय अलर्ट नहीं।',
        // EarlyWarningScreen
        earlyWarnings: 'प्रारंभिक चेतावनियाँ',
        noWarnings: 'अभी कोई पूर्वानुमानित जोखिम नहीं।',
        // SafeZoneGuideScreen
        safeZonesTitle: 'सुरक्षित क्षेत्र',
        whatToDoNow: 'अभी क्या करें',
        // NearbyDevicesScreen
        peerNetwork: 'पीयर नेटवर्क',
        devicesFound: (n: number) => `${n} डिवाइस मिले`,
        bleActive: 'BLE सक्रिय',
        bleIdle: 'BLE निष्क्रिय',
        noPeers: 'कोई पीयर नहीं मिला',
        noPeersBleHint: 'BLE मेश स्कैन कर रहा है। रेंज में आने पर अन्य राहत डिवाइस यहाँ दिखेंगे।',
        noPeersIdleHint: 'BLE मेश निष्क्रिय है। स्कैनिंग शुरू करने के लिए ऐप पुनः आरंभ करें।',
        // Narrator announcements
        narratorOn: 'स्क्रीन वाचक सक्रिय हुआ',
        narratorOff: 'स्क्रीन वाचक बंद हुआ',
        langChanged: (lang: string) => `भाषा ${lang} में बदली गई`,
        screenHome: 'होम स्क्रीन। मानचित्र दृश्य, नज़दीकी उपकरण और SOS बटन।',
        screenAlertFeed: 'अलर्ट फ़ीड। सक्रिय आपातकालीन अलर्ट देखें।',
        screenEarlyWarning: 'प्रारंभिक चेतावनी स्क्रीन। पूर्वानुमानित जोखिम अलर्ट देखें।',
        screenNearbyDevices: 'नज़दीकी उपकरण स्क्रीन। कनेक्टेड राहत पीयर देखें।',
        screenSafeZones: 'सुरक्षित क्षेत्र गाइड। नज़दीकी सुरक्षित स्थान देखें।',
        screenSettings: 'सेटिंग्स स्क्रीन।',
        screenProfile: 'प्रोफाइल स्क्रीन।',
        disasterActivated: 'आपदा मोड सक्रिय। शांत रहें और नियमित रूप से चेक इन करें।',
        disasterDeactivated: 'आपदा मोड निष्क्रिय।',
    },
} as const;

export type Strings = typeof S.English;

export function useStrings(): Strings {
    const language = useSettingsStore(s => s.language);
    return S[language] as unknown as Strings;
}

export default S;
