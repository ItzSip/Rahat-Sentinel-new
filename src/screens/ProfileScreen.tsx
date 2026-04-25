import React, { useState, useEffect } from 'react';
import { View, StyleSheet, Text, TextInput, Modal, FlatList, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useUserStore } from '../store/userStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/ui/GlassCard';
import { ActionButton } from '../components/ui/ActionButton';
import { useStrings } from '../i18n/strings';
import { useNarrator } from '../hooks/useNarrator';

export default function ProfileScreen() {
    const navigation = useNavigation();
    const { profile, contacts, setProfile, addContact, removeContact } = useUserStore();
    const [modalVisible, setModalVisible] = useState(false);
    const s = useStrings();
    const { speak } = useNarrator();

    useEffect(() => { speak(s.screenProfile); }, []);
    
    // Modal states
    const [editMode, setEditMode] = useState<'profile' | 'contact'>('profile');
    const [tempName, setTempName] = useState('');
    const [tempPhone, setTempPhone] = useState('');

    const openEditProfile = () => {
        setEditMode('profile');
        setTempName(profile.name);
        setTempPhone(profile.phone);
        setModalVisible(true);
    };

    const openAddContact = () => {
        setEditMode('contact');
        setTempName('');
        setTempPhone('');
        setModalVisible(true);
    };

    const handleSave = () => {
        if (editMode === 'profile') {
            setProfile({ name: tempName, phone: tempPhone });
        } else {
            addContact({ id: Date.now().toString(), name: tempName, phone: tempPhone });
        }
        setModalVisible(false);
    };

    return (
        <View style={styles.container}>
            <View style={styles.header}>
                <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
                    <Text style={styles.backText}>{s.back}</Text>
                </TouchableOpacity>
                <Text style={styles.headerTitle}>{s.profile}</Text>
            </View>
            
            <View style={styles.section}>
                <GlassCard>
                    <View style={styles.row}>
                        <View>
                            <Text style={styles.label}>Name</Text>
                            <Text style={styles.value}>{profile.name}</Text>
                        </View>
                    </View>
                    <View style={[styles.row, { marginTop: 15 }]}>
                        <View>
                            <Text style={styles.label}>Phone</Text>
                            <Text style={styles.value}>{profile.phone}</Text>
                        </View>
                    </View>
                    <ActionButton style={styles.editBtn} label="Edit Info" onPress={openEditProfile} />
                </GlassCard>
            </View>

            <View style={styles.sectionHeader}>
                <Text style={styles.sectionTitle}>Emergency Contacts</Text>
                <TouchableOpacity onPress={openAddContact}>
                    <Text style={styles.addText}>+ Add</Text>
                </TouchableOpacity>
            </View>

            <FlatList
                data={contacts}
                keyExtractor={c => c.id}
                renderItem={({ item }) => (
                    <GlassCard style={styles.contactCard}>
                        <View style={styles.contactInfo}>
                            <Text style={styles.contactName}>{item.name}</Text>
                            <Text style={styles.contactPhone}>{item.phone}</Text>
                        </View>
                        <TouchableOpacity onPress={() => removeContact(item.id)}>
                            <Text style={styles.removeText}>✕</Text>
                        </TouchableOpacity>
                    </GlassCard>
                )}
            />

            {/* Offline Modal */}
            <Modal visible={modalVisible} transparent={true} animationType="slide">
                <View style={styles.modalOverlay}>
                    <GlassCard style={styles.modalContent}>
                        <Text style={styles.modalTitle}>{editMode === 'profile' ? 'Edit Profile' : 'Add Contact'}</Text>
                        
                        <TextInput 
                            style={styles.input} 
                            placeholder="Name" 
                            placeholderTextColor={Colors.textSecondary}
                            value={tempName} 
                            onChangeText={setTempName} 
                        />
                        <TextInput 
                            style={styles.input} 
                            placeholder="Phone Number" 
                            placeholderTextColor={Colors.textSecondary}
                            keyboardType="phone-pad"
                            value={tempPhone} 
                            onChangeText={setTempPhone} 
                        />

                        <View style={styles.modalActions}>
                            <ActionButton style={styles.modalBtn} label="Cancel" onPress={() => setModalVisible(false)} />
                            <ActionButton style={styles.modalBtn} label="Save" variant="primary" onPress={handleSave} />
                        </View>
                    </GlassCard>
                </View>
            </Modal>
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: Colors.background, paddingTop: 52, paddingHorizontal: 20 },
    header: { flexDirection: 'row', alignItems: 'center', marginBottom: 20 },
    backBtn: { paddingRight: 16, paddingVertical: 4 },
    backText: { color: Colors.primary, fontSize: 16 },
    headerTitle: { color: Colors.textPrimary, fontSize: 22, fontWeight: 'bold' },
    section: { marginBottom: 30 },
    row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
    label: { color: Colors.textSecondary, fontSize: 14, marginBottom: 4 },
    value: { color: Colors.textPrimary, fontSize: 18, fontWeight: 'bold' },
    editBtn: { marginTop: 20 },
    sectionHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 15 },
    sectionTitle: { color: Colors.textPrimary, fontSize: 18, fontWeight: 'bold' },
    addText: { color: Colors.primary, fontSize: 16, fontWeight: 'bold' },
    contactCard: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10, paddingVertical: 12 },
    contactInfo: { flex: 1 },
    contactName: { color: Colors.textPrimary, fontSize: 16, fontWeight: 'bold', marginBottom: 2 },
    contactPhone: { color: Colors.textSecondary, fontSize: 14 },
    removeText: { color: Colors.red, fontSize: 20, paddingHorizontal: 10 },
    modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.7)', justifyContent: 'center', padding: 20 },
    modalContent: { backgroundColor: Colors.background, padding: 24 },
    modalTitle: { color: Colors.textPrimary, fontSize: 20, fontWeight: 'bold', marginBottom: 20 },
    input: { 
        backgroundColor: Colors.glassBackground, borderColor: Colors.glassBorder, borderWidth: 1,
        borderRadius: 8, color: Colors.textPrimary, padding: 12, marginBottom: 15, fontSize: 16
    },
    modalActions: { flexDirection: 'row', gap: 10, marginTop: 10 },
    modalBtn: { flex: 1 }
});
