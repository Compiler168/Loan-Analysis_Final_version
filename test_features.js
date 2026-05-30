const baseUrl = 'http://127.0.0.1:5000/api';

async function testFeatures() {
  console.log('--- Testing All App Features ---\n');
  const testEmail = `test${Date.now()}@smartloan.ai`;

  // 1. Get Token
  const regRes = await fetch(`${baseUrl}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: 'Test', email: testEmail, password: 'Password123!' })
  });
  const regData = await regRes.json();
  const token = regData.data?.token;

  if (!token) {
    console.error('Failed to get token:', regData);
    return;
  }
  
  const headers = { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` };

  // 2. Test Prediction
  console.log('1. Testing /loans/predict...');
  const predRes = await fetch(`${baseUrl}/loans/predict`, {
    method: 'POST',
    headers,
    body: JSON.stringify({
      age: 35, dependents: 0, employment_status: "salaried", employment_years: 5,
      monthly_income: 6000, monthly_expenses: 3000, credit_score: 720,
      existing_loans: 1, existing_emi: 500, loan_amount: 50000,
      loan_term_months: 36, interest_rate: 10, property_value: 0,
      savings_balance: 20000, missed_payments_last_year: 0, bankruptcies: 0
    })
  });
  console.log('Prediction:', await predRes.json());

  // 3. Test Chat
  console.log('\n2. Testing /chat/message...');
  const chatRes = await fetch(`${baseUrl}/chat/message`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ message: 'Hello, how can I improve my credit score?', session_id: 'default' })
  });
  console.log('Chat:', await chatRes.json());

  // 4. Test Profile Update
  console.log('\n3. Testing /auth/profile...');
  const profileRes = await fetch(`${baseUrl}/auth/profile`, {
    method: 'PUT',
    headers,
    body: JSON.stringify({ profile: { monthly_income: 8000 } })
  });
  console.log('Profile:', await profileRes.json());
}

testFeatures();
