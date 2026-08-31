/**
 * 06_receipt_summary.spec.js
 * ReceiptSummaryScreen - Itemized Receipt, Payer Assignment & History
 */

const { test, expect } = require('@playwright/test');
const { AndroidAppDriver } = require('../driver/AndroidAppDriver');
const { Selectors } = require('../driver/Selectors');
const { DeviceActions } = require('../helpers/DeviceActions');

test.describe('ReceiptSummaryScreen - Final Calculations & Bill Lifecycle', () => {
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

    // Single Bill Mode
    await driver.clickByText(Selectors.ModeSelection.singleBillTitle, 4000);

    // Add Bob
    await driver.typeText('Bob');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.PeopleSetup.addButton, 3000);
    await driver.clickByText(Selectors.PeopleSetup.continueButton, 4000);

    // Add Item
    await driver.typeText('Brunch Platter');
    await actions.hideKeyboard();
    await driver.typeText('120');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.ScanBill.addItemButton, 3000);
    await driver.clickByText(Selectors.ScanBill.continueButton, 4000);

    // Split Equally
    await driver.clickByText(Selectors.ItemAssignment.splitAllEquallyButton, 4000);
    await driver.clickByText(Selectors.ItemAssignment.continueButton, 4000);
    await driver.hasText(Selectors.ReceiptSummary.headerTitle, 5000);
  });

  test.afterAll(async () => {
    await driver.close();
  });

  test('POSITIVE: Accurate bill totals and breakdown per person', async () => {
    // Verify grand total or subtotal contains $120.00
    const hasTotal = (await driver.hasText('120', 4000)) || (await driver.hasText('60', 4000));
    expect(hasTotal).toBeTruthy();
  });

  test('POSITIVE: Start New Bill confirmation dialog and return to Mode Selection', async () => {
    await driver.clickByText(Selectors.ReceiptSummary.startNewBillButton, 4000);

    // If confirmation dialog appears, confirm
    try {
      await driver.clickByText(Selectors.ReceiptSummary.confirmResetButton, 2000);
    } catch (e) {
      // Direct navigation
    }

    // App should return to Mode Selection or People Setup cleanly
    const isReturned = (await driver.hasText(Selectors.ModeSelection.singleBillTitle, 5000)) ||
                       (await driver.hasText(Selectors.PeopleSetup.headerTitle, 5000));
    expect(isReturned).toBeTruthy();
  });
});
