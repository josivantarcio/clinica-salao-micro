// Stress test script for the Saas-CS API
// This script uses k6 (https://k6.io) to perform load testing on the API
// To run: k6 run stress_test.js

import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 10 }, // Ramp up to 10 users over 30 seconds
    { duration: '1m', target: 50 },  // Ramp up to 50 users over 1 minute
    { duration: '1m', target: 50 },  // Stay at 50 users for 1 minute
    { duration: '30s', target: 0 },  // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests should complete below 500ms
    http_req_failed: ['rate<0.01'],   // Less than 1% of requests should fail
  },
};

// Replace with actual token and tenant ID for testing
const TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBjbGluaWNzYWxvbi5jb20iLCJ0ZW5hbnRJZCI6IjEiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3MTY4MjMxNDgsImV4cCI6MTcxNjgyNjc0OH0.sample-token';
const TENANT_ID = '1';

function getRandomDateTimeInFuture() {
  const now = new Date();
  // Random date in next 30 days
  const futureDate = new Date(now.getTime() + Math.floor(Math.random() * 30) * 24 * 60 * 60 * 1000);
  
  // Set random hour between 9 AM and 5 PM
  futureDate.setHours(9 + Math.floor(Math.random() * 8), 0, 0, 0);
  
  // ISO string with local timezone
  return futureDate.toISOString().slice(0, 19);
}

function getEndTime(startTime) {
  // Parse the start time and add a random duration (30-90 minutes)
  const start = new Date(startTime);
  const durationMinutes = 30 + Math.floor(Math.random() * 4) * 15; // 30, 45, 60, 75, or 90 minutes
  const end = new Date(start.getTime() + durationMinutes * 60 * 1000);
  
  return end.toISOString().slice(0, 19);
}

export default function () {
  // Select a random professional ID (assuming IDs 1-5 exist)
  const professionalId = 1 + Math.floor(Math.random() * 5);
  
  // Generate random appointment times
  const startTime = getRandomDateTimeInFuture();
  const endTime = getEndTime(startTime);
  
  // Check availability
  const availabilityUrl = `http://localhost:8080/api/appointments/check-availability?professionalId=${professionalId}&startTime=${startTime}&endTime=${endTime}`;
  
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${TOKEN}`,
    'X-Tenant-ID': TENANT_ID
  };
  
  const availabilityResponse = http.get(availabilityUrl, { headers });
  
  // Verify the response
  check(availabilityResponse, {
    'availability check status is 200': (r) => r.status === 200,
    'availability response is boolean': (r) => typeof JSON.parse(r.body) === 'boolean',
  });
  
  // Try to create an appointment if availability check passes
  if (availabilityResponse.status === 200 && JSON.parse(availabilityResponse.body) === true) {
    const createAppointmentUrl = 'http://localhost:8080/api/appointments';
    
    const payload = JSON.stringify({
      clientId: 1 + Math.floor(Math.random() * 5), // Random client ID between 1-5
      professionalId: professionalId,
      startTime: startTime,
      endTime: endTime,
      status: 'SCHEDULED',
      notes: 'Stress test appointment',
      serviceIds: [1], // Assuming service ID 1 exists
      discountAmount: 0
    });
    
    const createResponse = http.post(createAppointmentUrl, payload, { headers });
    
    check(createResponse, {
      'create appointment status is 201': (r) => r.status === 201,
      'response has appointment ID': (r) => JSON.parse(r.body).id !== undefined,
    });
  }
  
  sleep(1); // Sleep for 1 second between iterations
}
