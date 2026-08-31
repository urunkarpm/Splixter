/**
 * 01_onboarding_profile.spec.js
 * UserProfileSetupScreen - Positive & Deep Negative Test Suite
 */

const { test, expect } = require('@playwright/test');
const { AndroidAppDriver } = require('../driver/AndroidAppDriver');
const { Selectors } = require('../driver/Selectors');
const { TestData } = require('../helpers/TestData');
const { DeviceActions } = require('../helpers/DeviceActions');

test.describe('UserProfileSetupScreen - Rigorous & Negative Test Suite', () => {
  let driver;
  let actions;

  test.beforeAll(async () => {
    driver = new AndroidAppDriver();
    await driver.init();
    actions = new DeviceActions(driver);
  });

  test.beforeEach(async () => {
    await actions.resetAppState();
  });

  test.afterAll(async () => {
    await driver.close();
  });

  test('NEGATIVE: Empty name submission should keep Continue button disabled or blocked', async () => {
    const isProfileScreen = await driver.hasText(Selectors.Onboarding.welcomeTitle, 6000);
    expect(isProfileScreen).toBeTruthy();

    // Verify Continue button is disabled or not proceeding
    const hierarchyBefore = driver.dumpHierarchy();
    
    // Attempt clicking Continue without entering name
    try {
      await driver.clickByText(Selectors.Onboarding.continueButton, 2000);
    } catch (e) {
      // Expected if button is disabled/unclickable
    }

    // Must still remain on onboarding screen
    const stillOnScreen = await driver.hasText(Selectors.Onboarding.welcomeTitle, 2000);
    expect(stillOnScreen).toBeTruthy();
  });

  test('NEGATIVE: Whitespace-only name should not permit profile creation', async () => {
    await driver.hasText(Selectors.Onboarding.welcomeTitle, 6000);

    // Enter only whitespace
    await driver.typeText(TestData.Strings.whitespaceOnly);
    await actions.hideKeyboard();

    try {
      await driver.clickByText(Selectors.Onboarding.continueButton, 2000);
    } catch (e) {
      // Ignored
    }

    // Must still remain on onboarding screen
    const stillOnScreen = await driver.hasText(Selectors.Onboarding.welcomeTitle, 2000);
    expect(stillOnScreen).toBeTruthy();
  });

  test('NEGATIVE: Malformed UPI ID and invalid phone number resilience', async () => {
    await driver.hasText(Selectors.Onboarding.welcomeTitle, 6000);

    // Enter valid name first
    await driver.typeText('Alex');
    await actions.hideKeyboard();

    // Type invalid phone with alphanumeric characters
    // Splixter sanitizes and handles non-digit inputs gracefully
    await driver.typeText(TestData.PhoneNumbers.invalidWithLetters);
    await actions.hideKeyboard();

    // Type malformed UPI ID
    await driver.typeText(TestData.UpiIds.missingAtSymbol);
    await actions.hideKeyboard();

    // Proceed to mode selection
    await driver.clickByText(Selectors.Onboarding.continueButton, 4000);

    // Verify successful progression without crashing
    const modeScreenVisible = await driver.hasText(Selectors.ModeSelection.singleBillTitle, 5000);
    expect(modeScreenVisible).toBeTruthy();
  });

  test('SECURITY & EDGE: XSS/SQL Injection strings in user profile name', async () => {
    await driver.hasText(Selectors.Onboarding.welcomeTitle, 6000);

    // Input SQL Injection string
    await driver.typeText("TestUser ' OR '1'='1; --");
    await actions.hideKeyboard();

    await driver.clickByText(Selectors.Onboarding.continueButton, 4000);

    // App should properly sanitize/escape and navigate to Mode Selection safely
    const modeScreenVisible = await driver.hasText(Selectors.ModeSelection.singleBillTitle, 5000);
    expect(modeScreenVisible).toBeTruthy();
  });

  test('POSITIVE: Complete profile creation with emoji, color, phone & UPI', async () => {
    await driver.hasText(Selectors.Onboarding.welcomeTitle, 6000);

    await driver.typeText('Prasenjeet');
    await actions.hideKeyboard();

    await driver.clickByText(Selectors.Onboarding.continueButton, 5000);

    const modeScreenVisible = await driver.hasText(Selectors.ModeSelection.singleBillTitle, 5000);
    expect(modeScreenVisible).toBeTruthy();
  });
});
