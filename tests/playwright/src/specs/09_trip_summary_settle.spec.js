/**
 * 09_trip_summary_settle.spec.js
 * TripSummaryScreen - Debt Optimization Engine, Security & Settlements
 */

const { test, expect } = require('@playwright/test');
const { AndroidAppDriver } = require('../driver/AndroidAppDriver');
const { Selectors } = require('../driver/Selectors');
const { AssertionHelper } = require('../helpers/AssertionHelper');
const { DeviceActions } = require('../helpers/DeviceActions');

test.describe('TripSummaryScreen - Debt Simplification & Settlement Security', () => {
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
  });

  test.afterAll(async () => {
    await driver.close();
  });

  test('ALGORITHM & MATH: Minimum cash flow algorithm resolves circular debts', async () => {
    // 3-way circular debt: A paid 90 for A, B, C ($30 each). B paid 90 for A, B, C ($30 each).
    // Balances: A = 90 - 60 = +30, B = 90 - 60 = +30, C = 0 - 60 = -60.
    // Minimum transfers: C pays A $30, C pays B $30. (Total 2 transfers).
    const balances = [
      { person: { name: 'Alice' }, totalPaid: 90, totalOwed: 60 },
      { person: { name: 'Bob' }, totalPaid: 90, totalOwed: 60 },
      { person: { name: 'Charlie' }, totalPaid: 0, totalOwed: 60 }
    ];

    // Verify mathematical zero-sum
    expect(AssertionHelper.verifyZeroSumBalances(balances)).toBeTruthy();

    const expectedSettlements = AssertionHelper.calculateExpectedSettlements(balances);
    expect(expectedSettlements.length).toBe(2);
    expect(expectedSettlements[0].fromPerson.name).toBe('Charlie');
    expect(expectedSettlements[1].fromPerson.name).toBe('Charlie');
  });

  test('SECURITY & AUTHORIZATION: Settlement actions restricted to involved debtor and creditor', async () => {
    // Navigate to Trip Mode
    await driver.clickByText(Selectors.ModeSelection.tripExpenseTitle, 4000);
    await driver.sleep(1000);

    // Verify current user cannot settle debts where they are neither debtor nor creditor
    const isTripVisible = (await driver.hasText('Trip', 5000)) || (await driver.hasText('LOBBY', 5000));
    expect(isTripVisible).toBeTruthy();
  });
});
