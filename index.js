const express = require('express');
const bodyParser = require('body-parser');
const twilio = require('twilio');

const app = express();
app.use(bodyParser.json());

const accountSid = process.env.TWILIO_ACCOUNT_SID;  // Set in Render environment variables
const authToken = process.env.TWILIO_AUTH_TOKEN;    // Set in Render environment variables
const serviceSid = process.env.TWILIO_SERVICE_SID;  // (Optional) For Twilio Verify Service

const client = twilio(accountSid, authToken);

app.post('/send-otp', (req, res) => {
    const { phoneNumber } = req.body;
    if (!phoneNumber) return res.status(400).send({ error: 'Phone number is required' });

    // Using Twilio Verify service to send OTP
    client.verify.services(serviceSid)
        .verifications
        .create({ to: phoneNumber, channel: 'sms' })
        .then(verification => res.send({ success: true, sid: verification.sid }))
        .catch(err => res.status(500).send({ error: err.message }));
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}`);
});
