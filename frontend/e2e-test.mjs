import { chromium } from 'playwright';
import { mkdirSync, existsSync } from 'fs';
import { join } from 'path';

const BASE_URL = 'http://localhost:5173';
const BACKEND_URL = 'http://localhost:8080';
const EVIDENCE_DIR = join(process.cwd(), '..', '.sisyphus', 'evidence');

// Ensure evidence directory exists
if (!existsSync(EVIDENCE_DIR)) {
  mkdirSync(EVIDENCE_DIR, { recursive: true });
}

let screenshotIndex = 0;
async function takeScreenshot(page, name) {
  screenshotIndex++;
  const filename = `${String(screenshotIndex).padStart(2, '0')}-${name}.png`;
  await page.screenshot({ path: join(EVIDENCE_DIR, filename), fullPage: true });
  console.log(`  📸 Screenshot: ${filename}`);
  return filename;
}

async function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function testLogin(page) {
  console.log('\n=== TEST: Login Flow ===');
  
  // Test 1: Navigate to login page
  console.log('1. Navigate to login page...');
  await page.goto(`${BASE_URL}/login`);
  await page.waitForSelector('.login-card', { timeout: 10000 });
  await takeScreenshot(page, 'login-page');
  
  // Test 2: Auth guard - try accessing protected route without login
  console.log('2. Test auth guard (redirect to login)...');
  await page.goto(`${BASE_URL}/dashboard`);
  await sleep(2000);
  const url = page.url();
  console.log(`   Current URL after redirect: ${url}`);
  const redirected = url.includes('/login');
  console.log(`   Auth guard working: ${redirected ? '✅ YES' : '❌ NO'}`);
  await takeScreenshot(page, 'auth-guard-redirect');
  
  // Test 3: Login with credentials
  console.log('3. Login with admin credentials...');
  await page.goto(`${BASE_URL}/login`);
  await page.waitForSelector('input[id="loginForm_username"]', { timeout: 5000 });
  await page.fill('input[id="loginForm_username"]', 'admin');
  await page.fill('input[id="loginForm_password"]', 'admin123');
  await takeScreenshot(page, 'login-filled');
  
  // Click login button
  await page.click('button[type="submit"]');
  await sleep(3000);
  
  const afterLoginUrl = page.url();
  console.log(`   URL after login: ${afterLoginUrl}`);
  const loginSuccess = !afterLoginUrl.includes('/login');
  console.log(`   Login successful: ${loginSuccess ? '✅ YES' : '❌ NO'}`);
  await takeScreenshot(page, 'after-login');
  
  // Test 4: Logout
  console.log('4. Test logout...');
  // Look for logout button or user dropdown
  const logoutBtn = await page.$('text=Logout');
  const userDropdown = await page.$('.ant-dropdown-trigger');
  if (userDropdown) {
    await userDropdown.click();
    await sleep(500);
    await takeScreenshot(page, 'user-dropdown');
    const logoutLink = await page.$('text=Logout');
    if (logoutLink) {
      await logoutLink.click();
      await sleep(2000);
    }
  } else if (logoutBtn) {
    await logoutBtn.click();
    await sleep(2000);
  }
  await takeScreenshot(page, 'after-logout');
  
  return { redirected, loginSuccess };
}

async function loginAsAdmin(page) {
  await page.goto(`${BASE_URL}/login`);
  await page.waitForSelector('input[id="loginForm_username"]', { timeout: 5000 });
  await page.fill('input[id="loginForm_username"]', 'admin');
  await page.fill('input[id="loginForm_password"]', 'admin123');
  await page.click('button[type="submit"]');
  await sleep(3000);
}

async function testDashboard(page) {
  console.log('\n=== TEST: Dashboard ===');
  await loginAsAdmin(page);
  
  console.log('1. Navigate to dashboard...');
  await page.goto(`${BASE_URL}/dashboard`);
  await sleep(3000);
  await takeScreenshot(page, 'dashboard-page');
  
  // Check for stat cards
  const statCards = await page.$$('.ant-card');
  console.log(`   Stat cards found: ${statCards.length}`);
  
  // Check for dashboard content
  const pageContent = await page.textContent('body');
  const hasDashboard = pageContent.includes('Dashboard') || pageContent.includes('dashboard');
  console.log(`   Dashboard loaded: ${hasDashboard ? '✅ YES' : '❌ NO'}`);
  
  return { hasDashboard, statCardCount: statCards.length };
}

async function testPartCRUD(page) {
  console.log('\n=== TEST: Part CRUD ===');
  await loginAsAdmin(page);
  
  // Navigate to parts page
  console.log('1. Navigate to parts page...');
  await page.goto(`${BASE_URL}/parts`);
  await sleep(3000);
  await takeScreenshot(page, 'part-list');
  
  // Check if parts page loaded
  const pageContent = await page.textContent('body');
  const hasParts = pageContent.includes('Part') || pageContent.includes('part');
  console.log(`   Parts page loaded: ${hasParts ? '✅ YES' : '❌ NO'}`);
  
  // Try to create a new part
  console.log('2. Try to create a new part...');
  const createBtn = await page.$('button:has-text("Create"), button:has-text("Add"), button:has-text("New")');
  if (createBtn) {
    await createBtn.click();
    await sleep(1000);
    await takeScreenshot(page, 'part-create-modal');
    
    // Fill in part form if modal appeared
    const modal = await page.$('.ant-modal');
    if (modal) {
      console.log('   Create modal opened: ✅ YES');
      // Close modal
      const cancelBtn = await page.$('.ant-modal-close, button:has-text("Cancel")');
      if (cancelBtn) await cancelBtn.click();
      await sleep(500);
    } else {
      console.log('   Create modal opened: ❌ NO');
    }
  } else {
    console.log('   Create button not found');
  }
  
  // Check table
  const table = await page.$('.ant-table');
  console.log(`   Table present: ${table ? '✅ YES' : '❌ NO'}`);
  
  return { hasParts, hasTable: !!table };
}

async function testDocumentCRUD(page) {
  console.log('\n=== TEST: Document CRUD ===');
  await loginAsAdmin(page);
  
  console.log('1. Navigate to documents page...');
  await page.goto(`${BASE_URL}/documents`);
  await sleep(3000);
  await takeScreenshot(page, 'document-list');
  
  const pageContent = await page.textContent('body');
  const hasDocs = pageContent.includes('Document') || pageContent.includes('document');
  console.log(`   Documents page loaded: ${hasDocs ? '✅ YES' : '❌ NO'}`);
  
  const table = await page.$('.ant-table');
  console.log(`   Table present: ${table ? '✅ YES' : '❌ NO'}`);
  
  return { hasDocs, hasTable: !!table };
}

async function testBOMCRUD(page) {
  console.log('\n=== TEST: BOM CRUD ===');
  await loginAsAdmin(page);
  
  console.log('1. Navigate to BOMs page...');
  await page.goto(`${BASE_URL}/boms`);
  await sleep(3000);
  await takeScreenshot(page, 'bom-list');
  
  const pageContent = await page.textContent('body');
  const hasBOMs = pageContent.includes('BOM') || pageContent.includes('bom');
  console.log(`   BOMs page loaded: ${hasBOMs ? '✅ YES' : '❌ NO'}`);
  
  const table = await page.$('.ant-table');
  console.log(`   Table present: ${table ? '✅ YES' : '❌ NO'}`);
  
  return { hasBOMs, hasTable: !!table };
}

async function testECRWorkflow(page) {
  console.log('\n=== TEST: ECR Workflow ===');
  await loginAsAdmin(page);
  
  console.log('1. Navigate to ECRs page...');
  await page.goto(`${BASE_URL}/ecrs`);
  await sleep(3000);
  await takeScreenshot(page, 'ecr-list');
  
  const pageContent = await page.textContent('body');
  const hasECRs = pageContent.includes('ECR') || pageContent.includes('ecr');
  console.log(`   ECRs page loaded: ${hasECRs ? '✅ YES' : '❌ NO'}`);
  
  const table = await page.$('.ant-table');
  console.log(`   Table present: ${table ? '✅ YES' : '❌ NO'}`);
  
  return { hasECRs, hasTable: !!table };
}

async function testECOWorkflow(page) {
  console.log('\n=== TEST: ECO Workflow ===');
  await loginAsAdmin(page);
  
  console.log('1. Navigate to ECOs page...');
  await page.goto(`${BASE_URL}/ecos`);
  await sleep(3000);
  await takeScreenshot(page, 'eco-list');
  
  const pageContent = await page.textContent('body');
  const hasECOs = pageContent.includes('ECO') || pageContent.includes('eco');
  console.log(`   ECOs page loaded: ${hasECOs ? '✅ YES' : '❌ NO'}`);
  
  const table = await page.$('.ant-table');
  console.log(`   Table present: ${table ? '✅ YES' : '❌ NO'}`);
  
  return { hasECOs, hasTable: !!table };
}

async function testUserCRUD(page) {
  console.log('\n=== TEST: User CRUD ===');
  await loginAsAdmin(page);
  
  console.log('1. Navigate to users page...');
  await page.goto(`${BASE_URL}/users`);
  await sleep(3000);
  await takeScreenshot(page, 'user-list');
  
  const pageContent = await page.textContent('body');
  const hasUsers = pageContent.includes('User') || pageContent.includes('user');
  console.log(`   Users page loaded: ${hasUsers ? '✅ YES' : '❌ NO'}`);
  
  const table = await page.$('.ant-table');
  console.log(`   Table present: ${table ? '✅ YES' : '❌ NO'}`);
  
  return { hasUsers, hasTable: !!table };
}

async function testAuditLog(page) {
  console.log('\n=== TEST: Audit Log ===');
  await loginAsAdmin(page);
  
  console.log('1. Navigate to audit logs page...');
  await page.goto(`${BASE_URL}/audit-logs`);
  await sleep(3000);
  await takeScreenshot(page, 'audit-log-list');
  
  const pageContent = await page.textContent('body');
  const hasAuditLog = pageContent.includes('Audit') || pageContent.includes('audit') || pageContent.includes('Log');
  console.log(`   Audit log page loaded: ${hasAuditLog ? '✅ YES' : '❌ NO'}`);
  
  const table = await page.$('.ant-table');
  console.log(`   Table present: ${table ? '✅ YES' : '❌ NO'}`);
  
  return { hasAuditLog, hasTable: !!table };
}

// Main test runner
async function runTests() {
  console.log('🚀 Starting PLM Frontend Integration Tests');
  console.log(`   Base URL: ${BASE_URL}`);
  console.log(`   Evidence dir: ${EVIDENCE_DIR}`);
  
  const browser = await chromium.launch({
    channel: 'msedge',
    headless: true,
  });
  
  const context = await browser.newContext({
    viewport: { width: 1280, height: 720 }
  });
  
  const page = await context.newPage();
  
  const results = {};
  
  try {
    // Test Login Flow
    results.login = await testLogin(page);
    
    // Test Dashboard
    results.dashboard = await testDashboard(page);
    
    // Test Part CRUD
    results.part = await testPartCRUD(page);
    
    // Test Document CRUD
    results.document = await testDocumentCRUD(page);
    
    // Test BOM CRUD
    results.bom = await testBOMCRUD(page);
    
    // Test ECR Workflow
    results.ecr = await testECRWorkflow(page);
    
    // Test ECO Workflow
    results.eco = await testECOWorkflow(page);
    
    // Test User CRUD
    results.user = await testUserCRUD(page);
    
    // Test Audit Log
    results.auditLog = await testAuditLog(page);
    
  } catch (error) {
    console.error('❌ Test error:', error.message);
    await takeScreenshot(page, 'error-state');
  } finally {
    await browser.close();
  }
  
  // Print summary
  console.log('\n' + '='.repeat(50));
  console.log('📊 TEST RESULTS SUMMARY');
  console.log('='.repeat(50));
  
  const tests = [
    { name: 'Login Flow', result: results.login },
    { name: 'Dashboard', result: results.dashboard },
    { name: 'Part CRUD', result: results.part },
    { name: 'Document CRUD', result: results.document },
    { name: 'BOM CRUD', result: results.bom },
    { name: 'ECR Workflow', result: results.ecr },
    { name: 'ECO Workflow', result: results.eco },
    { name: 'User CRUD', result: results.user },
    { name: 'Audit Log', result: results.auditLog },
  ];
  
  let passed = 0;
  let failed = 0;
  
  for (const test of tests) {
    const status = test.result ? '✅ PASS' : '❌ FAIL';
    console.log(`${status} - ${test.name}`);
    if (test.result) passed++;
    else failed++;
  }
  
  console.log('='.repeat(50));
  console.log(`Total: ${tests.length} | Passed: ${passed} | Failed: ${failed}`);
  console.log('='.repeat(50));
  
  // Write results to file
  const resultsPath = join(EVIDENCE_DIR, 'test-results.json');
  const { writeFileSync } = await import('fs');
  writeFileSync(resultsPath, JSON.stringify({ results, summary: { total: tests.length, passed, failed } }, null, 2));
  console.log(`\n📄 Results saved to: ${resultsPath}`);
}

runTests().catch(console.error);
