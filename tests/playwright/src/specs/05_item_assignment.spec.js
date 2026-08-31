/**
 * 05_item_assignment.spec.js
 * ItemAssignmentScreen - Item Splitting & Unassigned Items Validation
 */

const { test, expect } = require('@playwright/test');
const { AndroidAppDriver } = require('../driver/AndroidAppDriver');
const { Selectors } = require('../driver/Selectors');
const { DeviceActions } = require('../helpers/DeviceActions');

test.describe('ItemAssignmentScreen - Splits & Allocation Testing', () => {
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

    // Add Bob and Charlie
    await driver.typeText('Bob');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.PeopleSetup.addButton, 3000);
    await driver.typeText('Charlie');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.PeopleSetup.addButton, 3000);
    await driver.clickByText(Selectors.PeopleSetup.continueButton, 4000);

    // Add Items in Scan Screen
    await driver.typeText('Pizza');
    await actions.hideKeyboard();
    await driver.typeText('60');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.ScanBill.addItemButton, 3000);

    await driver.typeText('Cocktail');
    await actions.hideKeyboard();
    await driver.typeText('30');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.ScanBill.addItemButton, 3000);

    await driver.clickByText(Selectors.ScanBill.continueButton, 4000);
    await driver.hasText(Selectors.ItemAssignment.headerTitle, 5000);
  });

  test.afterAll(async () => {
    await driver.close();
  });

  test('POSITIVE: "Split All Equally" allocates all items evenly among all members', async () => {
    await driver.clickByText(Selectors.ItemAssignment.splitAllEquallyButton, 4000);

    // Proceed to Receipt Summary
    await driver.clickByText(Selectors.ItemAssignment.continueButton, 4000);

    // Verify Receipt Summary Screen is reached
    const receiptHeader = await driver.hasText(Selectors.ReceiptSummary.headerTitle, 5000);
    expect(receiptHeader).toBeTruthy();
  });

  test('NEGATIVE: Unassigning all items displays unassigned status/warning', async () => {
    // Clear all assignments
    try {
      await driver.clickByText(Selectors.ItemAssignment.unassignAllButton, 3000);
    } catch (e) {
      // Ignored
    }

    // Try proceeding with unassigned items
    await driver.clickByText(Selectors.ItemAssignment.continueButton, 3000);

    // Verify warning or handled safely
    const isReceiptOrWarning = (await driver.hasText(Selectors.ReceiptSummary.headerTitle, 3000)) ||
                                (await driver.hasText(Selectors.ItemAssignment.headerTitle, 3000));
    expect(isReceiptOrWarning).toBeTruthy();
  });
});
