/**
 * DeviceActions.js
 * Hardware & System-level actions for Android device automation
 */

class DeviceActions {
  constructor(driver) {
    this.driver = driver;
  }

  /**
   * Grant all essential runtime permissions
   */
  async grantPermissions() {
    await this.driver.grantPermissions();
  }

  /**
   * Reset application state to clean initial state
   */
  async resetAppState() {
    await this.driver.clearData();
    await this.driver.launchApp();
    await this.driver.sleep(1000);
  }

  /**
   * Rotate screen to Landscape and back to Portrait
   */
  async rotateLandscape() {
    console.log('[DeviceActions] Rotating to Landscape');
    this.driver.execAdb('shell settings put system accelerometer_rotation 0');
    this.driver.execAdb('shell settings put system user_rotation 1'); // 1 = 90 deg
    await this.driver.sleep(1000);
  }

  async rotatePortrait() {
    console.log('[DeviceActions] Rotating to Portrait');
    this.driver.execAdb('shell settings put system accelerometer_rotation 0');
    this.driver.execAdb('shell settings put system user_rotation 0'); // 0 = 0 deg
    await this.driver.sleep(1000);
  }

  /**
   * Simulate sending app to background and returning (Process life-cycle testing)
   */
  async backgroundAndResume(durationMs = 2000) {
    console.log(`[DeviceActions] Backgrounding app for ${durationMs}ms`);
    this.driver.execAdb('shell input keyevent 3'); // KEYCODE_HOME
    await this.driver.sleep(durationMs);
    await this.driver.launchApp();
  }

  /**
   * Simulate low-memory process death & relaunch
   */
  async simulateProcessDeath() {
    console.log('[DeviceActions] Simulating process kill and state restoration');
    this.driver.execAdb(`shell am kill ${this.driver.packageName}`);
    await this.driver.sleep(800);
    await this.driver.launchApp();
  }

  /**
   * Hide on-screen virtual keyboard
   */
  async hideKeyboard() {
    this.driver.execAdb('shell input keyevent 111'); // KEYCODE_ESCAPE
    await this.driver.sleep(300);
  }

  /**
   * Scroll down the screen
   */
  async scrollDown() {
    this.driver.execAdb('shell input swipe 500 1500 500 500 300');
    await this.driver.sleep(600);
  }

  /**
   * Scroll up the screen
   */
  async scrollUp() {
    this.driver.execAdb('shell input swipe 500 500 500 1500 300');
    await this.driver.sleep(600);
  }
}

module.exports = { DeviceActions };
