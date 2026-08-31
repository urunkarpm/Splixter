#!/usr/bin/env node
/**
 * run_tests.js
 * CLI Test Runner for Splixter Playwright + JS Suite
 */

const { execSync, spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

const args = process.argv.slice(2);
let targetSpec = null;

for (let i = 0; i < args.length; i++) {
  if (args[i] === '--spec' && args[i + 1]) {
    targetSpec = args[i + 1];
    break;
  }
}

console.log('=====================================================');
console.log('   SPLIXTER PLAYWRIGHT + JS RIGOROUS TEST SUITE');
console.log('=====================================================\n');

// 1. Check ADB Devices
console.log('[1/3] Checking connected Android devices via ADB...');
let isHardwareDeviceConnected = false;
try {
  const devicesOutput = execSync('adb devices', { encoding: 'utf8' }).trim();
  console.log(devicesOutput);
  const lines = devicesOutput.split('\n').filter((l) => l.includes('\tdevice'));
  if (lines.length > 0) {
    isHardwareDeviceConnected = true;
    console.log(`✅ Found ${lines.length} connected physical/emulator device(s).\n`);
  } else {
    console.log('ℹ️  No active hardware device found. Running in Headless/Mock Verification Mode.\n');
  }
} catch (e) {
  console.log('ℹ️  ADB CLI not found. Running in Headless/Mock Verification Mode.\n');
}

// 2. Determine target specs
const specsDir = path.join(__dirname, 'src', 'specs');
let specFiles = fs.readdirSync(specsDir).filter((f) => f.endsWith('.spec.js'));

if (targetSpec) {
  specFiles = specFiles.filter((f) => f.includes(targetSpec));
  if (specFiles.length === 0) {
    console.error(`❌ Spec matching "${targetSpec}" not found in ${specsDir}`);
    process.exit(1);
  }
  console.log(`[2/3] Target Spec selected: ${specFiles.join(', ')}\n`);
} else {
  console.log(`[2/3] Running full test suite (${specFiles.length} spec files):\n - ${specFiles.join('\n - ')}\n`);
}

// 3. Execute with Playwright Runner
console.log('[3/3] Launching Playwright Test Suite...\n');
const cmd = 'npx';
const playwrightArgs = ['playwright', 'test'];
if (targetSpec) {
  playwrightArgs.push(targetSpec);
}

const child = spawn(cmd, playwrightArgs, {
  cwd: __dirname,
  stdio: 'inherit',
  shell: true
});

child.on('close', (code) => {
  console.log('\n=====================================================');
  if (code === 0) {
    console.log('✅ ALL TEST SCENARIOS PASSED SUCCESSFULLY!');
  } else {
    console.log(`⚠️ Test execution finished with exit code: ${code}`);
  }
  console.log('=====================================================');
  process.exit(code);
});
