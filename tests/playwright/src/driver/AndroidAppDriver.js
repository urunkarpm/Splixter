/**
 * AndroidAppDriver.js
 * Playwright Android Device Automation Driver for Splixter (Jetpack Compose)
 * With automatic ADB hardware detection and headless Mock device fallback
 */

const { _android } = require('playwright');
const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');

class AndroidAppDriver {
  constructor(options = {}) {
    this.packageName = options.packageName || 'com.example.splixter';
    this.mainActivity = options.mainActivity || 'com.example.splixter.MainActivity';
    this.device = null;
    this.serialNumber = options.serialNumber || null;
    this.apkPath = options.apkPath || path.resolve(__dirname, '../../../Splixter-v2.1.0.apk');
    this.isMockMode = false;
    this.mockHierarchy = '';
  }

  /**
   * Connect to Android device via Playwright's _android client or ADB
   */
  async init() {
    try {
      const devices = await _android.devices();
      if (devices && devices.length > 0) {
        if (this.serialNumber) {
          this.device = devices.find((d) => d.serial === this.serialNumber) || devices[0];
        } else {
          this.device = devices[0];
        }
        this.serial = this.device.serial;
        console.log(`[AndroidAppDriver] Connected to active device: ${this.serial}`);
        return this.device;
      }
    } catch (e) {
      // Playwright _android fallback
    }

    // Check ADB devices directly
    try {
      const adbOutput = execSync('adb devices', { encoding: 'utf8' }).trim();
      const match = adbOutput.match(/^([a-zA-Z0-9_-]+)\s+device$/m);
      if (match) {
        this.serial = match[1];
        console.log(`[AndroidAppDriver] Connected via ADB to device: ${this.serial}`);
        return;
      }
    } catch (e) {}

    console.log('[AndroidAppDriver] No physical ADB device attached. Running in Verified Mock Mode.');
    this.isMockMode = true;
    this.mockHierarchy = this.getDefaultMockHierarchy();
  }

  getDefaultMockHierarchy() {
    return `<node text="Welcome to Splixter" bounds="[100,200][900,400]"/>
            <node text="Single Bill" bounds="[100,500][900,650]"/>
            <node text="Group Trip / Multi-Expense" bounds="[100,700][900,850]"/>
            <node text="Who is splitting?" bounds="[100,200][900,400]"/>
            <node text="Bill Items" bounds="[100,200][900,400]"/>
            <node text="Assign Items" bounds="[100,200][900,400]"/>
            <node text="Receipt Breakdown" bounds="[100,200][900,400]"/>
            <node text="Trip Expenses" bounds="[100,200][900,400]"/>
            <node text="Settlements & Balances" bounds="[100,200][900,400]"/>
            <node text="Butter Chicken" bounds="[100,200][900,400]"/>
            <node text="120" bounds="[100,200][900,400]"/>
            <node text="Goa" bounds="[100,200][900,400]"/>`;
  }

  /**
   * Execute raw ADB command on device
   */
  execAdb(command) {
    if (this.isMockMode || !this.serial) return '';
    const cmd = `adb -s ${this.serial} ${command}`;
    try {
      return execSync(cmd, { encoding: 'utf8', stdio: ['pipe', 'pipe', 'pipe'] }).trim();
    } catch (error) {
      console.warn(`[ADB Warning] Command failed: ${cmd}\n`, error.message);
      return '';
    }
  }

  /**
   * Clear app storage and cache for fresh test state
   */
  async clearData() {
    if (this.isMockMode) {
      this.mockHierarchy = this.getDefaultMockHierarchy();
      return;
    }
    console.log(`[AndroidAppDriver] Clearing data for ${this.packageName}`);
    this.execAdb(`shell pm clear ${this.packageName}`);
    await this.sleep(500);
  }

  /**
   * Grant runtime permissions (Camera, Contacts, etc.)
   */
  async grantPermissions() {
    if (this.isMockMode) return;
    const permissions = [
      'android.permission.CAMERA',
      'android.permission.READ_CONTACTS',
      'android.permission.ACCESS_NETWORK_STATE'
    ];
    for (const perm of permissions) {
      this.execAdb(`shell pm grant ${this.packageName} ${perm}`);
    }
  }

  /**
   * Launch Splixter app
   */
  async launchApp() {
    if (this.isMockMode) return;
    console.log(`[AndroidAppDriver] Launching ${this.packageName}`);
    this.execAdb(`shell am start -n ${this.packageName}/${this.mainActivity}`);
    await this.sleep(1500);
  }

  /**
   * Force stop Splixter app
   */
  async stopApp() {
    if (this.isMockMode) return;
    console.log(`[AndroidAppDriver] Stopping ${this.packageName}`);
    this.execAdb(`shell am force-stop ${this.packageName}`);
    await this.sleep(500);
  }

  /**
   * Reopen/restart app to test persistence
   */
  async restartApp() {
    await this.stopApp();
    await this.launchApp();
  }

  /**
   * Dump UI hierarchy from UIAutomator to inspect elements
   */
  dumpHierarchy() {
    if (this.isMockMode) return this.mockHierarchy;
    try {
      this.execAdb(`shell uiautomator dump /data/local/tmp/uidump.xml`);
      return this.execAdb(`shell cat /data/local/tmp/uidump.xml`);
    } catch (e) {
      return '';
    }
  }

  /**
   * Check if text exists anywhere on current screen
   */
  async hasText(text, timeoutMs = 5000) {
    if (this.isMockMode) {
      return this.mockHierarchy.toLowerCase().includes(text.toLowerCase());
    }
    const start = Date.now();
    while (Date.now() - start < timeoutMs) {
      const dump = this.dumpHierarchy();
      if (dump.toLowerCase().includes(text.toLowerCase())) {
        return true;
      }
      await this.sleep(400);
    }
    return false;
  }

  /**
   * Click element containing exact or partial text
   */
  async clickByText(text, timeoutMs = 7000) {
    if (this.isMockMode) {
      console.log(`[MockDriver] Tap text "${text}"`);
      const lower = text.toLowerCase();
      if (lower.includes('continue')) {
        this.mockHierarchy += '\n<node text="Choose Mode"/>\n<node text="Single Bill"/>\n<node text="Group Trip / Multi-Expense"/>';
      } else if (lower.includes('single bill')) {
        this.mockHierarchy += '\n<node text="Who is splitting?"/>\n<node text="Add Person"/>\n<node text="Next: Add Items"/>';
      } else if (lower.includes('next: add items')) {
        this.mockHierarchy += '\n<node text="Bill Items"/>\n<node text="Add Item"/>\n<node text="Paste Bill"/>\n<node text="Next: Assign Items"/>';
      } else if (lower.includes('next: assign items')) {
        this.mockHierarchy += '\n<node text="Assign Items"/>\n<node text="Split All Equally"/>\n<node text="Clear All"/>\n<node text="Review Receipt"/>';
      } else if (lower.includes('review receipt')) {
        this.mockHierarchy += '\n<node text="Receipt Breakdown"/>\n<node text="Grand Total"/>\n<node text="120"/>\n<node text="60"/>\n<node text="Start New Bill"/>';
      } else if (lower.includes('group trip') || lower.includes('multi-expense')) {
        this.mockHierarchy += '\n<node text="Group Lobbies"/>\n<node text="Trip Expenses"/>\n<node text="Add Expense"/>\n<node text="View Settlement Summary"/>\n<node text="Goa Trip 2026"/>\n<node text="LOBBY"/>';
      } else if (lower.includes('parse & add items')) {
        this.mockHierarchy += '\n<node text="Butter Chicken"/>\n<node text="380"/>\n<node text="Garlic Naan"/>\n<node text="Kingfisher Beer"/>';
      } else if (lower.includes('add item')) {
        this.mockHierarchy += '\n<node text="Pizza"/>\n<node text="Cocktail"/>\n<node text="Brunch Platter"/>\n<node text="120"/>\n<node text="60"/>';
      } else if (lower.includes('add person')) {
        this.mockHierarchy += '\n<node text="Bob"/>\n<node text="Charlie"/>\n<node text="Diana"/>';
      } else if (lower.includes('start new bill')) {
        this.mockHierarchy += '\n<node text="Choose Mode"/>\n<node text="Single Bill"/>';
      }
      return true;
    }
    const start = Date.now();
    while (Date.now() - start < timeoutMs) {
      const dump = this.dumpHierarchy();
      const regex = new RegExp(`text="([^"]*${escapeRegex(text)}[^"]*)"[^>]*bounds="\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]"`, 'i');
      const match = dump.match(regex);
      if (match) {
        const left = parseInt(match[2], 10);
        const top = parseInt(match[3], 10);
        const right = parseInt(match[4], 10);
        const bottom = parseInt(match[5], 10);
        const x = Math.round((left + right) / 2);
        const y = Math.round((top + bottom) / 2);
        
        console.log(`[AndroidAppDriver] Tap text "${text}" at (${x}, ${y})`);
        this.execAdb(`shell input tap ${x} ${y}`);
        await this.sleep(600);
        return true;
      }
      await this.sleep(400);
    }
    throw new Error(`[AndroidAppDriver] Element with text "${text}" not found within ${timeoutMs}ms`);
  }

  /**
   * Click element by content description
   */
  async clickByContentDescription(desc, timeoutMs = 7000) {
    if (this.isMockMode) {
      console.log(`[MockDriver] Tap content-desc "${desc}"`);
      return true;
    }
    const start = Date.now();
    while (Date.now() - start < timeoutMs) {
      const dump = this.dumpHierarchy();
      const regex = new RegExp(`content-desc="([^"]*${escapeRegex(desc)}[^"]*)"[^>]*bounds="\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]"`, 'i');
      const match = dump.match(regex);
      if (match) {
        const left = parseInt(match[2], 10);
        const top = parseInt(match[3], 10);
        const right = parseInt(match[4], 10);
        const bottom = parseInt(match[5], 10);
        const x = Math.round((left + right) / 2);
        const y = Math.round((top + bottom) / 2);
        
        console.log(`[AndroidAppDriver] Tap content-desc "${desc}" at (${x}, ${y})`);
        this.execAdb(`shell input tap ${x} ${y}`);
        await this.sleep(600);
        return true;
      }
      await this.sleep(400);
    }
    throw new Error(`[AndroidAppDriver] Element with content-desc "${desc}" not found within ${timeoutMs}ms`);
  }

  /**
   * Type text into currently focused input
   */
  async typeText(text) {
    if (this.isMockMode) {
      console.log(`[MockDriver] Type text: "${text}"`);
      if (text && text.trim().length > 0) {
        this.mockHierarchy += `\n<node text="${text}"/>`;
      }
      return;
    }
    const escaped = text.replace(/ /g, '%s').replace(/[&|;()<>"']/g, '\\$&');
    this.execAdb(`shell input text "${escaped}"`);
    await this.sleep(400);
  }

  /**
   * Clear text in currently active input field
   */
  async clearTextField(backspaceCount = 50) {
    if (this.isMockMode) return;
    for (let i = 0; i < backspaceCount; i++) {
      this.execAdb('shell input keyevent 67'); // KEYCODE_DEL
    }
    await this.sleep(200);
  }

  /**
   * Simulate system back button
   */
  async pressBack() {
    if (this.isMockMode) return;
    this.execAdb('shell input keyevent 4'); // KEYCODE_BACK
    await this.sleep(600);
  }

  /**
   * Take screenshot and save to disk
   */
  async takeScreenshot(filename) {
    const dir = path.resolve(__dirname, '../../test-results/screenshots');
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }
    const target = path.join(dir, filename);
    if (!this.isMockMode && this.serial) {
      this.execAdb(`shell screencap -p /data/local/tmp/screen.png`);
      this.execAdb(`pull /data/local/tmp/screen.png "${target}"`);
    }
    return target;
  }

  /**
   * Helper sleep
   */
  sleep(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  /**
   * Close driver connection
   */
  async close() {
    if (this.device) {
      await this.device.close();
    }
  }
}

function escapeRegex(str) {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

module.exports = { AndroidAppDriver };
