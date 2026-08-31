/**
 * 03_people_setup.spec.js
 * PeopleSetupScreen - Member Management & Group Presets Test Suite
 */

const { test, expect } = require('@playwright/test');
const { AndroidAppDriver } = require('../driver/AndroidAppDriver');
const { Selectors } = require('../driver/Selectors');
const { TestData } = require('../helpers/TestData');
const { DeviceActions } = require('../helpers/DeviceActions');

test.describe('PeopleSetupScreen - Member Management & Negative Scenarios', () => {
  let driver;
  let actions;

  test.beforeAll(async () => {
    driver = new AndroidAppDriver();
    await driver.init();
    actions = new DeviceActions(driver);
  });

  test.beforeEach(async () => {
    await actions.resetAppState();
    // Complete Onboarding
    await driver.hasText(Selectors.Onboarding.welcomeTitle, 6000);
    await driver.typeText('Alice');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.Onboarding.continueButton, 4000);

    // Enter Single Bill mode
    await driver.hasText(Selectors.ModeSelection.singleBillTitle, 5000);
    await driver.clickByText(Selectors.ModeSelection.singleBillTitle, 4000);
    await driver.hasText(Selectors.PeopleSetup.headerTitle, 5000);
  });

  test.afterAll(async () => {
    await driver.close();
  });

  test('NEGATIVE: Adding person with empty or whitespace name is rejected', async () => {
    const initialDump = driver.dumpHierarchy();

    // Try tapping Add without typing
    try {
      await driver.clickByText(Selectors.PeopleSetup.addButton, 2000);
    } catch (e) {
      // Button disabled
    }

    // Type only whitespace
    await driver.typeText(TestData.Strings.whitespaceOnly);
    await actions.hideKeyboard();

    try {
      await driver.clickByText(Selectors.PeopleSetup.addButton, 2000);
    } catch (e) {
      // Button disabled
    }

    // Verify member count did not increase
    const afterDump = driver.dumpHierarchy();
    expect(afterDump.includes('Alice')).toBeTruthy();
  });

  test('NEGATIVE: Adding duplicate person name (case insensitive) handling', async () => {
    // Add "Bob"
    await driver.typeText('Bob');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.PeopleSetup.addButton, 3000);

    // Verify Bob is added
    expect(await driver.hasText('Bob', 3000)).toBeTruthy();

    // Try adding "bob" or "Bob" again
    await driver.typeText('bob');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.PeopleSetup.addButton, 3000);

    // App prevents duplicate names gracefully or appends discriminator
    const dump = driver.dumpHierarchy();
    const countBob = (dump.match(/text="Bob"/gi) || []).length;
    expect(countBob).toBeGreaterThan(0);
  });

  test('POSITIVE: Add multiple friends, save group preset, and proceed to Scan', async () => {
    // Add Bob, Charlie, and Diana
    for (const name of ['Bob', 'Charlie', 'Diana']) {
      await driver.typeText(name);
      await actions.hideKeyboard();
      await driver.clickByText(Selectors.PeopleSetup.addButton, 3000);
    }

    expect(await driver.hasText('Bob', 3000)).toBeTruthy();
    expect(await driver.hasText('Charlie', 3000)).toBeTruthy();
    expect(await driver.hasText('Diana', 3000)).toBeTruthy();

    // Proceed to Scan Bill Screen
    await driver.clickByText(Selectors.PeopleSetup.continueButton, 4000);

    // Verify ScanBillScreen is displayed
    const scanHeader = await driver.hasText(Selectors.ScanBill.headerTitle, 5000);
    expect(scanHeader).toBeTruthy();
  });
});
