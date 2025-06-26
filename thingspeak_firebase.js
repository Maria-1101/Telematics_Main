const axios = require("axios");
const admin = require("firebase-admin");
const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
 
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: process.env.FIREBASE_DB_URL
});
 
const db = admin.database();
 
const THINGSPEAK_READ_API_KEY = "RFILA6YNLV13M5H7";
const READ_CHANNEL_ID = "2979545";
const THINGSPEAK_WRITE_API_KEY = "E2312877OHC5L9TQ";
const WRITE_CHANNEL_ID = "2997189";
const FIREBASE_PATH = "HomeFragment/Anna Maria James";
const THINGSPEAK_WRITE_DEBOUNCE = 1000; 
 
const safeParse = (value) => {
  const num = parseFloat(value);
  return isNaN(num) ? 0 : num;
};
 
// ⏰ Cache last ThingSpeak timestamp
let lastThingSpeakTimestamp = null;

// 🔒 Cache last lock status
let lastLockStatus = {
  handle_lock: null,
  seat_lock: null,
  sos: null,
  vehicle_lock: null
};

const fetchAndPush = async () => {
  try {
    const response = await axios.get(
      `https://api.thingspeak.com/channels/${READ_CHANNEL_ID}/feeds.json?api_key=${THINGSPEAK_READ_API_KEY}&results=1`
    );
 
    const feed = response.data.feeds[0];
    if (!feed) {
      console.warn("⚠️ No data received from ThingSpeak");
      return;
    }

    const currentTimestamp = feed.created_at;
   
    // ✅ Skip if this entry has already been pushed
    if (currentTimestamp === lastThingSpeakTimestamp) {
      console.log("⏸ No new data in ThingSpeak — skipping push to Firebase");
      return;
    }

    lastThingSpeakTimestamp = currentTimestamp;
   
    const data = {
      avg_speed: safeParse(feed.field1),
      mileage: safeParse(feed.field2),
      total_distance: safeParse(feed.field3),
      co2_saved: safeParse(feed.field4),
      battery_level: safeParse(feed.field5),
      dte_progress: safeParse(feed.field6),
      location: {
          latitude: safeParse(feed.field7),
          longitude: safeParse(feed.field8)
      },
      last_updated: new Date().toISOString()
    };
 
    await db.ref(FIREBASE_PATH).update(data);
    console.log("✅ New ThingSpeak data pushed to Firebase:", data);
 
  } catch (error) {
    console.error("❌ Error fetching or pushing data:", error.message);
  }
};

// 🔄 Fetch from Firebase & push to ThingSpeak
const fetchAndPushToThingSpeak = async () => {
  try {
    const snapshot = await db.ref(FIREBASE_PATH).once('value');
    const firebaseData = snapshot.val();
    
    if (!firebaseData) {
      console.warn("⚠️ No data found in Firebase");
      return;
    }

    // Extract 4 values (modify field names as needed)
 const currentLockStatus = {
      handle_lock: firebaseData.handle_lock,
      seat_lock: firebaseData.seat_lock,
      sos: firebaseData.sos,
      vehicle_lock: firebaseData.vehicle_lock
    };

    // Check if lock status has changed
    if (JSON.stringify(currentLockStatus) === JSON.stringify(lastLockStatus)) {
      console.log("⏸ Lock status unchanged - skipping ThingSpeak update");
      return;
    }

    // Update cache with new values
    lastLockStatus = currentLockStatus;
    
    const params = new URLSearchParams({
      api_key: THINGSPEAK_WRITE_API_KEY,
      field1: safeParse(currentLockStatus.seat_lock),
      field2: safeParse(currentLockStatus.vehicle_lock),
      field3: safeParse(currentLockStatus.handle_lock),
      field4: safeParse(currentLockStatus.sos)
    });
    
   console.log("📤 Writing to ThingSpeak with params:", params.toString());
   
    const response = await axios.post(
      `https://api.thingspeak.com/update`,
      params,
      { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    );

    if (response.data > 0) {
      console.log(`✅ Firebase data pushed to ThingSpeak (Entry ID: ${response.data})`);
    } else {
      console.warn("⚠️ ThingSpeak write failed", response.data);
    }

  } catch (error) {
    console.error("❌ Error processing Firebase data:", error.message);
  }
};

function startSync() {
  setInterval(fetchAndPush, 1000); // ThingSpeak → Firebase (every 1s)
  setInterval(fetchAndPushToThingSpeak,200); // Firebase → ThingSpeak (every 15s)
}
 
module.exports = { startSync };
