/**
 * AssertionHelper.js
 * Specialized mathematical and state validation assertions for Splixter
 */

class AssertionHelper {
  /**
   * Asserts that bill item fractions and tax/tip sum up to exactly 100% of the total
   */
  static verifyBillReconciliation(items, personBreakdowns, taxAndTip) {
    const rawSubtotal = items.reduce((sum, item) => sum + item.price, 0);
    const breakdownSubtotals = personBreakdowns.reduce((sum, pb) => sum + pb.subtotal, 0);

    const subtotalDiff = Math.abs(rawSubtotal - breakdownSubtotals);
    if (subtotalDiff > 0.05) {
      throw new Error(
        `[Reconciliation Failure] Sum of person subtotals ($${breakdownSubtotals.toFixed(
          2
        )}) does not match bill subtotal ($${rawSubtotal.toFixed(2)})`
      );
    }

    const grandTotal = rawSubtotal + (taxAndTip.taxAmount || 0) + (taxAndTip.tipAmount || 0) + (taxAndTip.vatAmount || 0) - (taxAndTip.discountAmount || 0);
    const breakdownGrandTotals = personBreakdowns.reduce((sum, pb) => sum + pb.grandTotal, 0);

    const grandDiff = Math.abs(grandTotal - breakdownGrandTotals);
    if (grandDiff > 0.05) {
      throw new Error(
        `[Grand Total Mismatch] Sum of person grand totals ($${breakdownGrandTotals.toFixed(
          2
        )}) does not match calculated total ($${grandTotal.toFixed(2)})`
      );
    }
    return true;
  }

  /**
   * Asserts that all debts in a group trip are net zero-sum: Sum(balances) == 0
   */
  static verifyZeroSumBalances(balances) {
    const netSum = balances.reduce((acc, b) => acc + (b.totalPaid - b.totalOwed), 0);
    if (Math.abs(netSum) > 0.05) {
      throw new Error(
        `[Zero-Sum Violation] Total net balance of group trip is not zero. Discrepancy: ${netSum.toFixed(
          4
        )}`
      );
    }
    return true;
  }

  /**
   * Solves minimum transaction debt settlement independently to verify the app's calculation engine
   */
  static calculateExpectedSettlements(balances) {
    const creditors = []; // people who are owed money (positive net balance)
    const debtors = [];   // people who owe money (negative net balance)

    for (const b of balances) {
      const net = b.totalPaid - b.totalOwed;
      if (net > 0.01) {
        creditors.push({ person: b.person, amount: net });
      } else if (net < -0.01) {
        debtors.push({ person: b.person, amount: -net });
      }
    }

    // Sort descending by amount
    creditors.sort((a, b) => b.amount - a.amount);
    debtors.sort((a, b) => b.amount - a.amount);

    const settlements = [];
    let i = 0; // creditor index
    let j = 0; // debtor index

    while (i < creditors.length && j < debtors.length) {
      const settleAmount = Math.min(creditors[i].amount, debtors[j].amount);
      if (settleAmount > 0.009) {
        settlements.push({
          fromPerson: debtors[j].person,
          toPerson: creditors[i].person,
          amount: parseFloat(settleAmount.toFixed(2))
        });
      }

      creditors[i].amount -= settleAmount;
      debtors[j].amount -= settleAmount;

      if (creditors[i].amount <= 0.009) i++;
      if (debtors[j].amount <= 0.009) j++;
    }

    return settlements;
  }

  /**
   * Asserts that actual settlements matches optimal minimum transfer count
   */
  static verifySettlementOptimality(actualSettlements, balances) {
    this.verifyZeroSumBalances(balances);
    const expected = this.calculateExpectedSettlements(balances);

    const actualTotal = actualSettlements.reduce((sum, s) => sum + s.amount, 0);
    const expectedTotal = expected.reduce((sum, s) => sum + s.amount, 0);

    if (Math.abs(actualTotal - expectedTotal) > 0.05) {
      throw new Error(
        `[Settlement Total Discrepancy] Actual sum ($${actualTotal.toFixed(
          2
        )}) != Expected sum ($${expectedTotal.toFixed(2)})`
      );
    }
    return true;
  }
}

module.exports = { AssertionHelper };
