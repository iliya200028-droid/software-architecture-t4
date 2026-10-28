"""Запуск: python smoke_test.py [http://localhost:8080/UserAuthApp]. Только стандартная библиотека."""
import http.client
import sys
from urllib.parse import urlencode, urlsplit
from uuid import uuid4

url = urlsplit(sys.argv[1] if len(sys.argv) > 1 else 'http://localhost:8080/UserAuthApp')
checks = 0

def call(path, data=None, cookie=''):
    conn = http.client.HTTPConnection(url.hostname, url.port or 80, timeout=10)
    headers = {'Cookie': cookie}
    body = None
    if data is not None:
        body = urlencode(data)
        headers['Content-Type'] = 'application/x-www-form-urlencoded'
    conn.request('POST' if data is not None else 'GET', url.path.rstrip('/') + path, body, headers)
    r = conn.getresponse()
    result = r.status, r.getheaders(), r.read().decode('utf-8')
    conn.close()
    return result

def check(condition, title):
    global checks
    assert condition, title
    checks += 1
    print('PASS:', title)

check(call('/login')[0] == 200, 'GET login')
for path in ['/welcome', '/userinfo']:
    check(call(path)[0] == 302, path + ' requires session')
check(call('/userinfo', cookie='username=ilya')[0] == 302, 'Username cookie alone cannot authorize')
check(call('/login', {'username':'ilya','password':'bad'})[0] == 401, 'Wrong password')
check(call('/login', {})[0] == 401, 'Empty credentials')
s, headers, body = call('/login', {'username':'ilya','password':'12345'})
check(s == 302 and dict(headers).get('Location', '').endswith('/welcome'), 'Successful login redirect')
cookies = [v.split(';')[0] for k,v in headers if k.lower() == 'set-cookie']
cookie = '; '.join(cookies)
check('username=ilya' in cookie and 'JSESSIONID=' in cookie, 'Username and session cookies created')
check('ilya' in call('/welcome', cookie=cookie)[2], 'Welcome reads cookie')
check('ilya' in call('/userinfo', cookie=cookie)[2], 'User info reads session')
session_cookie = next(c for c in cookies if c.startswith('JSESSIONID='))
check('отсутствует' in call('/welcome', cookie=session_cookie)[2], 'Missing username cookie handled')
check('ilya' in call('/userinfo', cookie=session_cookie + '; username=other')[2], 'Session data independent of username cookie')
check('&lt;script&gt;' in call('/welcome', cookie=session_cookie + '; username=<script>')[2], 'Cookie HTML escaped')
check(call('/register')[0] == 200, 'Registration form')
check(call('/register', {'username':'x','password':'1'})[0] == 400, 'Registration validation')
name = 'test_' + uuid4().hex[:12]
data = {'username':name, 'password':'test123'}
check(call('/register', data)[0] == 302, 'Registration succeeds')
check(call('/register', data)[0] == 409, 'Duplicate rejected')
check(call('/login', data)[0] == 302, 'Registered user can log in')
print(f'{checks} checks passed')
