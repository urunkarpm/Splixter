/**
 * 02_mode_selection.spec.js
 * ModeSelectionScreen & Global Configuration Test Suite
 */

const { test, expect } = require('@playwright/test');
const { AndroidAppDriver } = require('../driver/AndroidAppDriver');
const { Selectors } = require('../driver/Selectors');
const { DeviceActions } = require('../helpers/DeviceActions');

test.describe('ModeSelectionScreen - Mode Routing & Preferences', () => {
  let driver;
  let actions;

  test.beforeAll(async () => {
    driver = new AndroidAppDriver();
    await driver.init();
    actions = new DeviceActions(driver);
  });

  test.beforeEach(async () => {
    await actions.resetAppState();
    // Complete onboarding to reach Mode Selection
    await driver.hasText(Selectors.Onboarding.welcomeTitle, 6000);
    await driver.typeText('Tester');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.Onboarding.continueButton, 4000);
  });

  test.afterAll(async () => {
    await driver.close();
  });

  test('POSITIVE: Navigate to Single Bill Mode (People Setup Screen)', async () => {
    await driver.hasText(Selectors.ModeSelection.singleBillTitle, 5000);
    await driver.clickByText(Selectors.ModeSelection.singleBillTitle, 4000);

    // Verify PeopleSetupScreen is displayed
    const peopleHeader = await driver.hasText(Selectors.PeopleSetup.headerTitle, 5000);
    expect(peopleHeader).toBeTruthy();
  });

  test('POSITIVE: Navigate to Group Trip Mode (Lobby Hub Screen)', async () => {
    await driver.hasText(Selectors.ModeSelection.tripExpenseTitle, 5000);
    await driver.clickByText(Selectors.ModeSelection.tripExpenseTitle, 4000);

    // Verify LobbyHubScreen or TripExpensesScreen is reached
    const lobbyHeader = await driver.hasText('LOBBY', 5000) || await driver.hasText('Trip', 5000);
    expect(lobbyHeader).toBeTruthy();
  });

  test('PERSISTENCE: App restart remembers completed profile and loads Mode Selection immediately', async () => {
    await driver.hasText(Selectors.ModeSelection.singleBillTitle, 5000);

    // Kill and restart app
    await driver.restartApp();

    // App should skip Splash/Onboarding and directly show Mode Selection
    const modeScreen = await driver.hasText(Selectors.ModeSelection.singleBillTitle, 6000);
    expect(modeScreen).toBeTruthy();
  });
});
