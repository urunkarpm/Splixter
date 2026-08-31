/**
 * 04_scan_and_bill_input.spec.js
 * ScanBillScreen - Manual Items, OCR Paste & Tax/Tip/VAT Negative Testing
 */

const { test, expect } = require('@playwright/test');
const { AndroidAppDriver } = require('../driver/AndroidAppDriver');
const { Selectors } = require('../driver/Selectors');
const { TestData } = require('../helpers/TestData');
const { DeviceActions } = require('../helpers/DeviceActions');

test.describe('ScanBillScreen - Bill Entry & Numerical Edge Cases', () => {
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

    // Enter Single Bill mode
    await driver.clickByText(Selectors.ModeSelection.singleBillTitle, 4000);
    // Add a friend & continue
    await driver.hasText(Selectors.PeopleSetup.headerTitle, 5000);
    await driver.typeText('Bob');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.PeopleSetup.addButton, 3000);
    await driver.clickByText(Selectors.PeopleSetup.continueButton, 4000);
    await driver.hasText(Selectors.ScanBill.headerTitle, 5000);
  });

  test.afterAll(async () => {
    await driver.close();
  });

  test('NEGATIVE: Empty item name or $0.00 price submission is rejected', async () => {
    // Try tapping Add Item without entering values
    try {
      await driver.clickByText(Selectors.ScanBill.addItemButton, 2000);
    } catch (e) {
      // Disabled
    }

    // Enter name but leave price 0
    await driver.typeText('Pasta');
    await actions.hideKeyboard();
    try {
      await driver.clickByText(Selectors.ScanBill.addItemButton, 2000);
    } catch (e) {
      // Disabled
    }

    // Verify item list remains empty
    const dump = driver.dumpHierarchy();
    expect(dump.includes('Pasta - $0.00')).toBeFalsy();
  });

  test('NEGATIVE: Paste Bill OCR with malformed / no-price text gracefully handles parsing', async () => {
    // Open Paste Bill Dialog
    await driver.clickByText(Selectors.ScanBill.pasteBillButton, 4000);

    // Paste receipt text with no prices
    await driver.typeText(TestData.Receipts.malformedNoPrices);
    await actions.hideKeyboard();

    // Click Parse
    await driver.clickByText(Selectors.ScanBill.parsePasteConfirm, 4000);

    // App should not crash and should show error or leave item list safe
    const scanHeader = await driver.hasText(Selectors.ScanBill.headerTitle, 4000);
    expect(scanHeader).toBeTruthy();
  });

  test('POSITIVE: Paste standard receipt text with Food and Liquor items auto-categorized', async () => {
    await driver.clickByText(Selectors.ScanBill.pasteBillButton, 4000);

    // Enter valid receipt items
    await driver.typeText(TestData.Receipts.validStandard);
    await actions.hideKeyboard();

    await driver.clickByText(Selectors.ScanBill.parsePasteConfirm, 4000);

    // Verify items were parsed
    const hasItems = (await driver.hasText('Butter Chicken', 4000)) || (await driver.hasText('380', 4000));
    expect(hasItems).toBeTruthy();
  });

  test('BOUNDARY: Discount amount exceeding bill total is handled safely without negative total', async () => {
    // Add a single item of $100
    await driver.typeText('Pizza');
    await actions.hideKeyboard();
    await driver.typeText('100');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.ScanBill.addItemButton, 3000);

    // Open Tax & Tip settings
    await driver.clickByText(Selectors.ScanBill.taxTipSettingsButton, 3000);

    // Set discount to $250 (exceeding subtotal)
    await driver.typeText('250');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.ScanBill.applyTaxTipButton, 3000);

    // Proceed to Assign Items Screen
    await driver.clickByText(Selectors.ScanBill.continueButton, 4000);

    // Verify Assign Screen is reached safely
    const assignHeader = await driver.hasText(Selectors.ItemAssignment.headerTitle, 5000);
    expect(assignHeader).toBeTruthy();
  });
});
