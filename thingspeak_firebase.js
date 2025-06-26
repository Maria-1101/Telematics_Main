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

let lastWriteTime = 0;

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
 
    // ⏱ Update cache
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
    const { handle_lock, seat_lock, sos ,vehicle_lock } = firebaseData;
    
    const params = new URLSearchParams({
      api_key: THINGSPEAK_WRITE_API_KEY,
      field1: safeParse(handle_lock),
      field2: safeParse(seat_lock),
      field3: safeParse(sos),
      field4: safeParse(vehicle_lock)
    });

   // Log the parameters being sent
    console.log("📤 Writing to ThingSpeak with params:", {
      field1: safeParse(handle_lock),
      field2: safeParse(seat_lock),
      field3: safeParse(sos),
      field4: safeParse(vehicle_lock)
    });
   
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

// 🔥 Realtime Firebase listener
const setupFirebaseListener = () => {
  db.ref(FIREBASE_PATH).on('value', (snapshot) => {
    const firebaseData = snapshot.val();
    if (!firebaseData) return;
    
    const { handle_lock, seat_lock, sos, vehicle_lock } = firebaseData;
    
    // Detect lock changes
    if (
      handle_lock !== lastLockStatus.handle_lock ||
      seat_lock !== lastLockStatus.seat_lock ||
      sos !== lastLockStatus.sos ||
      vehicle_lock !== lastLockStatus.vehicle_lock
    ) {
      const now = Date.now();
      
      // Enforce 15s write interval
      if (now - lastWriteTime >= THINGSPEAK_WRITE_DEBOUNCE) {
        console.log("🔔 Lock status changed - writing to ThingSpeak");
        lastLockStatus = { handle_lock, seat_lock, sos, vehicle_lock };
        lastWriteTime = now;
        fetchAndPushToThingSpeak(firebaseData);
      } else {
        const nextWrite = Math.ceil((THINGSPEAK_WRITE_DEBOUNCE - (now - lastWriteTime))/1000);
        console.log(`⏳ Change detected (next write in ${nextWrite}s)`);
      }
    }
  });
};

function startSync() {
  setInterval(fetchAndPush, 1000); // ThingSpeak → Firebase (every 1s)
  //setInterval(fetchAndPushToThingSpeak,500); // Firebase → ThingSpeak (every 15s)
 setupFirebaseListener();
}
 
module.exports = { startSync };
