/**
 * 10_edge_cases_resilience.spec.js
 * App Resilience, State Persistence & Lifecycle Edge Cases
 */

const { test, expect } = require('@playwright/test');
const { AndroidAppDriver } = require('../driver/AndroidAppDriver');
const { Selectors } = require('../driver/Selectors');
const { DeviceActions } = require('../helpers/DeviceActions');

test.describe('Splixter App Resilience & Lifecycle Testing', () => {
  let driver;
  let actions;

  test.beforeAll(async () => {
    driver = new AndroidAppDriver();
    await driver.init();
    actions = new DeviceActions(driver);
  });

  test.beforeEach(async () => {
    await actions.resetAppState();
    // Setup Profile
    await driver.hasText(Selectors.Onboarding.welcomeTitle, 6000);
    await driver.typeText('Alice');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.Onboarding.continueButton, 4000);
  });

  test.afterAll(async () => {
    // Restore orientation
    await actions.rotatePortrait();
    await driver.close();
  });

  test('LIFECYCLE: App survives backgrounding and resume without losing state', async () => {
    await driver.hasText(Selectors.ModeSelection.singleBillTitle, 5000);
    await driver.clickByText(Selectors.ModeSelection.singleBillTitle, 4000);
    await driver.hasText(Selectors.PeopleSetup.headerTitle, 5000);

    // Background app for 2 seconds
    await actions.backgroundAndResume(2000);

    // Verify still on PeopleSetupScreen
    const isStillOnPeople = await driver.hasText(Selectors.PeopleSetup.headerTitle, 5000);
    expect(isStillOnPeople).toBeTruthy();
  });

  test('ORIENTATION: UI adapts gracefully to Landscape rotation and back', async () => {
    await driver.hasText(Selectors.ModeSelection.singleBillTitle, 5000);

    // Rotate Landscape
    await actions.rotateLandscape();
    const isLandscapeVisible = await driver.hasText(Selectors.ModeSelection.singleBillTitle, 5000);
    expect(isLandscapeVisible).toBeTruthy();

    // Rotate Portrait
    await actions.rotatePortrait();
    const isPortraitVisible = await driver.hasText(Selectors.ModeSelection.singleBillTitle, 5000);
    expect(isPortraitVisible).toBeTruthy();
  });

  test('NAVIGATION: Back-stack traversal from Scan -> People -> Mode Selection', async () => {
    // Navigate Mode -> People -> Scan
    await driver.clickByText(Selectors.ModeSelection.singleBillTitle, 4000);
    await driver.hasText(Selectors.PeopleSetup.headerTitle, 5000);
    await driver.clickByText(Selectors.PeopleSetup.continueButton, 4000);
    await driver.hasText(Selectors.ScanBill.headerTitle, 5000);

    // Press Back once -> Returns to People
    await driver.pressBack();
    const onPeople = await driver.hasText(Selectors.PeopleSetup.headerTitle, 4000);
    expect(onPeople).toBeTruthy();

    // Press Back again -> Returns to Mode Selection
    await driver.pressBack();
    const onMode = await driver.hasText(Selectors.ModeSelection.singleBillTitle, 4000);
    expect(onMode).toBeTruthy();
  });
});
