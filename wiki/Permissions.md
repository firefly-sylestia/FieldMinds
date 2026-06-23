# Permissions Guide

This guide explains all permissions requested by FieldMind, why they're needed, and how they're used.

## 🔒 Privacy First

FieldMind is **100% FOSS-compliant** and respects your privacy:
- ✅ No analytics or tracking
- ✅ No personal data collection
- ✅ No internet requirement for core functionality
- ✅ All permissions used solely for app functionality
- ✅ No background data transmission

---

## 📱 Required Permissions

### 📁 Storage Access

**Permissions:**
- `READ_EXTERNAL_STORAGE` (Android ≤12)
- `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` (Android 13+)

**Why Needed:**
- To access and attach media files to your notes.

**Where Used:**
- Attaching images and videos to field notes.

### 📸 Camera

**Permission:** `CAMERA`

**Why Needed:**
- To take photos and videos to attach to your notes.

**Where Used:**
- In the note editor to capture new images and videos.

### 📍 Location

**Permissions:**
- `ACCESS_COARSE_LOCATION`
- `ACCESS_FINE_LOCATION`

**Why Needed:**
- To get your location for weather data and to tag observations with a location.

**Where Used:**
- When creating a new observation to automatically add the current location.
- To fetch weather data for your current location.

---

## 🔔 Optional Permissions

### 📢 Notifications

**Permission:** `POST_NOTIFICATIONS` (Android 13+)

**Why Needed:**
- To display notifications for reminders or background tasks.

**Where Used:**
- Reminders for scheduled observations.
- Notifications for long-running tasks like data exports.

**Can be disabled:** Yes, but you'll miss reminders and task notifications.

### 📶 Internet Access

**Permissions:**
- `INTERNET`
- `ACCESS_NETWORK_STATE`

**Why Needed:**
- To fetch weather data from Open-Meteo.
- To identify plant species from images using the Perenual API.

**Where Used:**
- When fetching weather data for a location.
- When using the species identification feature.

**Data Usage:** Minimal, only for the specific features you use.

**Can be disabled:** Yes, but you'll lose online features.

### ⚙️ Foreground Service

**Permissions:**
- `FOREGROUND_SERVICE`

**Why Needed:**
- To run background tasks like timers or data synchronization.

**Where Used:**
- Timers for observations.
- Background data export/import.

**Can be disabled:** No, required for background tasks to function correctly.

---

## 🚫 Removed Permissions

FieldMind **does NOT** request these permissions:

❌ `MANAGE_EXTERNAL_STORAGE` - Broad file access (not needed)
❌ `ACCESS_MEDIA_LOCATION` - GPS coordinates in photos (not needed)
❌ `CONTACTS` - Contact list (never needed)
❌ `PHONE` - Phone calls/SMS (never needed)

---

## ❓ FAQ

### Why does FieldMind need internet access?
For optional online features like weather data (Open-Meteo) and plant species identification (Perenual). The app works fully offline without internet.

### Can I use FieldMind without granting location permissions?
Yes, but you won't be able to automatically tag observations with your location or get local weather data. You can still manually enter locations.

### Does FieldMind access my photos?
Only when you specifically choose to attach an image to a note. It does not access your photo gallery otherwise.

### Is my research data sent anywhere?
**No.** FieldMind stores all data locally on your device. Nothing is uploaded or shared unless you explicitly export it.

### Can I audit the code?
**Yes!** FieldMind is fully open-source. You can find the code on our GitHub repository.
