from fastapi.testclient import TestClient
from backend.main import app

client = TestClient(app)

print('--- Test 1: GET /api/events ---')
r = client.get('/api/events')
assert r.status_code == 200, f'Expected 200, got {r.status_code}'
events = r.json()
print(f'Retrieved {len(events)} events. First event: {events[0]["title"]} (available seats: {events[0]["available_seats"]})')
assert len(events) >= 14

print('\n--- Test 2: Filter by category (Technical) ---')
r = client.get('/api/events?category=Technical')
assert r.status_code == 200
tech_events = r.json()
print(f'Found {len(tech_events)} Technical events.')
assert all(e['category'] == 'Technical' for e in tech_events)

print('\n--- Test 3: Search title (Sprint) ---')
r = client.get('/api/events?search=Sprint')
assert r.status_code == 200
search_events = r.json()
print(f'Found {len(search_events)} events matching Sprint.')
assert any('Sprint' in e['title'] for e in search_events)

print('\n--- Test 4: GET /api/events/1 ---')
r = client.get('/api/events/1')
assert r.status_code == 200
print(f'Event 1: {r.json()["title"]}')

print('\n--- Test 5: GET /api/events/9999 (404 Not Found) ---')
r = client.get('/api/events/9999')
assert r.status_code == 404
print(f'Correctly returned 404: {r.json()}')

print('\n--- Test 6: POST /api/events/1/register (201 Created) ---')
import time
unique_email = f"student_{int(time.time())}@college.edu"
payload = {
    'student_name': 'Nishant Mandhare',
    'roll_number': '24PM-082',
    'email': unique_email,
    'phone': '+91 9876543210'
}
r = client.post('/api/events/1/register', json=payload)
assert r.status_code == 201
reg_id = r.json()['id']
print(f'Registered successfully! Reg ID: {reg_id}')

print('\n--- Test 7: Duplicate registration with same email (409 Conflict) ---')
r = client.post('/api/events/1/register', json=payload)
assert r.status_code == 409
print(f'Correctly returned 409 Conflict: {r.json()}')

print('\n--- Test 8: Missing student_name (400 Bad Request) ---')
r = client.post('/api/events/1/register', json={'student_name': '', 'roll_number': '123', 'email': 'a@b.com'})
assert r.status_code == 400
print(f'Correctly returned 400: {r.json()}')

print('\n--- Test 9: Invalid email without @ (400 Bad Request) ---')
r = client.post('/api/events/1/register', json={'student_name': 'Test', 'roll_number': '123', 'email': 'no-at-sign'})
assert r.status_code == 400
print(f'Correctly returned 400: {r.json()}')

print('\n--- Test 10: GET /api/registrations ---')
r = client.get('/api/registrations')
assert r.status_code == 200
regs = r.json()
print(f'Total registrations in DB: {len(regs)}. First: {regs[0]["student_name"]} for {regs[0]["event_title"]}')

print('\n--- Test 11: GET /api/stats ---')
r = client.get('/api/stats')
assert r.status_code == 200
stats = r.json()
print('Live Stats:', stats)

print('\n--- Test 12: Serve frontend static files ---')
r_index = client.get('/')
assert r_index.status_code == 200
assert 'ZENITH' in r_index.text
print('GET / served index.html successfully')

r_admin = client.get('/admin.html')
assert r_admin.status_code == 200
assert 'Admin Console' in r_admin.text
print('GET /admin.html served admin.html successfully')

r_js = client.get('/js/app.js')
assert r_js.status_code == 200
print('GET /js/app.js served successfully')

print('\n--- Test 13: Stretch Goal DELETE /api/registrations/{id} ---')
r_del = client.delete(f'/api/registrations/{reg_id}')
assert r_del.status_code == 200
print(f'Cancelled registration {reg_id}:', r_del.json())

print('\nALL 13 TESTS PASSED PERFECTLY!')
