import { create } from 'zustand';

export interface EmergencyContact {
    id: string;
    name: string;
    phone: string;
}

export interface UserProfile {
    name: string;
    phone: string;
}

interface UserState {
    profile: UserProfile;
    contacts: EmergencyContact[];
    setProfile: (profile: UserProfile) => void;
    addContact: (contact: EmergencyContact) => void;
    updateContact: (id: string, contact: Partial<EmergencyContact>) => void;
    removeContact: (id: string) => void;
}

export const useUserStore = create<UserState>((set) => ({
    profile: {
        name: 'John Doe',
        phone: '+91 9876543210'
    },
    contacts: [
        { id: 'c1', name: 'Jane Doe', phone: '+91 9876543211' }
    ],
    setProfile: (profile) => set({ profile }),
    addContact: (contact) =>
        set((state) => ({ contacts: [...state.contacts, contact] })),
    updateContact: (id, contact) =>
        set((state) => ({
            contacts: state.contacts.map((c) => (c.id === id ? { ...c, ...contact } : c)),
        })),
    removeContact: (id) =>
        set((state) => ({
            contacts: state.contacts.filter((c) => c.id !== id),
        })),
}));
