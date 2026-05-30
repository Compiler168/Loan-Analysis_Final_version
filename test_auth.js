const baseUrl = 'http://127.0.0.1:5000/api';

async function testAuth() {
  console.log('Testing Authentication Flow...');
  
  const testEmail = `test${Date.now()}@smartloan.ai`;
  const testPassword = 'Password123!';

  try {
    // 1. Test Register
    console.log(`\n1. Registering user: ${testEmail}`);
    const registerRes = await fetch(`${baseUrl}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: 'Test User',
        email: testEmail,
        password: testPassword
      })
    });
    
    const registerData = await registerRes.json();
    console.log('Register Response:', registerData);

    if (!registerData.success) {
      console.error('Registration failed! Stopping test.');
      return;
    }

    // 2. Test Login
    console.log(`\n2. Logging in with: ${testEmail}`);
    const loginRes = await fetch(`${baseUrl}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: testEmail,
        password: testPassword
      })
    });

    const loginData = await loginRes.json();
    console.log('Login Response:', loginData);

    if (loginData.success && loginData.data.token) {
      console.log('\n✅ SUCCESS: Registration and Login are working perfectly!');
      console.log('Token received:', loginData.data.token.substring(0, 20) + '...');
      
      // 3. Test Dashboard
      console.log(`\n3. Fetching Dashboard data...`);
      const dashRes = await fetch(`${baseUrl}/financial/dashboard`, {
        method: 'GET',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${loginData.data.token}`
        }
      });
      
      const dashData = await dashRes.json();
      console.log('Dashboard Response:', JSON.stringify(dashData, null, 2));
      
      if (dashData.success) {
        console.log('\n✅ SUCCESS: Dashboard endpoint is working correctly!');
      } else {
        console.error('\n❌ FAILED: Dashboard did not return success.');
      }
      
    } else {
      console.error('\n❌ FAILED: Login did not return a token.');
    }

  } catch (err) {
    console.error('\n❌ ERROR during testing:', err.message);
  }
}

testAuth();
