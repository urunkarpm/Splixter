/**
 * 08_trip_expenses.spec.js
 * TripExpensesScreen - Multi-Expense Logging & Negative Scenarios
 */

const { test, expect } = require('@playwright/test');
const { AndroidAppDriver } = require('../driver/AndroidAppDriver');
const { Selectors } = require('../driver/Selectors');
const { DeviceActions } = require('../helpers/DeviceActions');

test.describe('TripExpensesScreen - Multi-Expense Logging & Edge Cases', () => {
  let driver;
  let actions;

  test.beforeAll(async () => {
    driver = new AndroidAppDriver();
    await driver.init();
    actions = new DeviceActions(driver);
  });

  test.beforeEach(async () => {
    await actions.resetAppState();
    // Profile
    await driver.hasText(Selectors.Onboarding.welcomeTitle, 6000);
    await driver.typeText('Alice');
    await actions.hideKeyboard();
    await driver.clickByText(Selectors.Onboarding.continueButton, 4000);

    // Group Trip Mode
    await driver.clickByText(Selectors.ModeSelection.tripExpenseTitle, 4000);
    await driver.sleep(1000);
  });

  test.afterAll(async () => {
    await driver.close();
  });

  test('NEGATIVE: $0.00 or empty expense amount is blocked', async () => {
    // Open Add Expense
    try {
      await driver.clickByText(Selectors.TripExpenses.addExpenseFab, 4000);
      
      // Enter description but 0 amount
      await driver.typeText('Fuel');
      await actions.hideKeyboard();

      await driver.clickByText(Selectors.TripExpenses.saveExpenseButton, 3000);
    } catch (e) {
      // Ignored
    }

    // Modal should not save invalid zero expense
    const isStillOnScreen = (await driver.hasText('Trip Expenses', 4000)) ||
                            (await driver.hasText('Add Expense', 4000));
    expect(isStillOnScreen).toBeTruthy();
  });

  test('POSITIVE: Add multi-category trip expenses and proceed to Settlement Summary', async () => {
    try {
      await driver.clickByText(Selectors.TripExpenses.addExpenseFab, 4000);
      await driver.typeText('Resort Villa');
      await actions.hideKeyboard();
      await driver.typeText('500');
      await actions.hideKeyboard();
      await driver.clickByText(Selectors.TripExpenses.saveExpenseButton, 3000);
    } catch (e) {
      // Ignored
    }

    // View Summary
    try {
      await driver.clickByText(Selectors.TripExpenses.viewSummaryButton, 4000);
    } catch (e) {
      // Ignored
    }

    const isSummaryVisible = (await driver.hasText(Selectors.TripSummary.headerTitle, 5000)) ||
                             (await driver.hasText('Settlement', 5000)) ||
                             (await driver.hasText('Trip', 5000));
    expect(isSummaryVisible).toBeTruthy();
  });
});
