/**
 * Selectors.js
 * Centralized UI selectors and text matchers across all 10 Splixter screens
 */

const Selectors = {
  // Splash & Onboarding
  Onboarding: {
    welcomeTitle: 'Welcome to Splixter',
    nameInputHint: 'Enter your name',
    phoneInputHint: 'Phone Number (Optional)',
    upiInputHint: 'UPI ID (e.g. name@upi)',
    continueButton: 'Continue',
    colorOption: 'Color',
    emojiOption: 'Emoji'
  },

  // Mode Selection Screen
  ModeSelection: {
    title: 'Choose Mode',
    singleBillTitle: 'Single Bill',
    singleBillSubtitle: 'Scan, itemize & split a restaurant bill',
    tripExpenseTitle: 'Group Trip / Multi-Expense',
    tripExpenseSubtitle: 'Track multiple shared expenses across days',
    historySection: 'Past Calculations',
    savedLobbiesSection: 'Active Lobbies',
    darkModeToggle: 'Dark Mode',
    profileAvatar: 'Your Profile',
    appUpdateBanner: 'Update Available'
  },

  // People Setup Screen
  PeopleSetup: {
    headerTitle: 'Who is splitting?',
    nameInput: 'Friend name',
    addButton: 'Add Person',
    saveGroupButton: 'Save Group',
    loadGroupButton: 'Load Group',
    groupNameInput: 'Group Name',
    savePresetConfirm: 'Save',
    cancelButton: 'Cancel',
    continueButton: 'Next: Add Items',
    deletePersonContentDesc: 'Delete',
    currentUserBadge: 'You'
  },

  // Scan & Bill Input Screen
  ScanBill: {
    headerTitle: 'Bill Items',
    itemNameInput: 'Item name',
    itemPriceInput: 'Price',
    categoryFood: 'Food',
    categoryLiquor: 'Liquor',
    addItemButton: 'Add Item',
    pasteBillButton: 'Paste Bill',
    cameraScanButton: 'Scan with Camera',
    pasteInputHint: 'Paste receipt text here...',
    parsePasteConfirm: 'Parse & Add Items',
    taxTipSettingsButton: 'Tax & Tip Settings',
    taxPercentageInput: 'Tax %',
    tipPercentageInput: 'Tip %',
    discountAmountInput: 'Discount',
    liquorVatPercentageInput: 'Liquor VAT %',
    applyTaxTipButton: 'Apply',
    continueButton: 'Next: Assign Items'
  },

  // Item Assignment Screen
  ItemAssignment: {
    headerTitle: 'Assign Items',
    splitAllEquallyButton: 'Split All Equally',
    unassignAllButton: 'Clear All',
    filterAll: 'All',
    filterFood: 'Food',
    filterLiquor: 'Liquor',
    continueButton: 'Review Receipt',
    unassignedWarning: 'Unassigned Items'
  },

  // Receipt Summary Screen
  ReceiptSummary: {
    headerTitle: 'Receipt Breakdown',
    grandTotalLabel: 'Grand Total',
    subtotalLabel: 'Subtotal',
    taxLabel: 'Tax',
    tipLabel: 'Tip',
    vatLabel: 'Liquor VAT',
    discountLabel: 'Discount',
    paidBySelector: 'Paid By',
    shareBreakdownButton: 'Share Breakdown',
    startNewBillButton: 'Start New Bill',
    confirmResetButton: 'Yes, Start New'
  },

  // Lobby Hub Screen
  LobbyHub: {
    headerTitle: 'Group Lobbies',
    createLobbyButton: 'Create New Lobby',
    joinLobbyButton: 'Join Lobby',
    lobbyNameInput: 'Lobby Name (e.g. Goa Trip)',
    lobbyCodeInput: 'Enter 6-character code (e.g. SPLIX-4892)',
    submitCreate: 'Create',
    submitJoin: 'Join',
    qrCodeScannerButton: 'Scan QR Code',
    claimProfileTitle: 'Claim Your Profile',
    activeLobbyItem: 'LOBBY'
  },

  // Trip Expenses Screen
  TripExpenses: {
    headerTitle: 'Trip Expenses',
    addExpenseFab: 'Add Expense',
    expenseTitleInput: 'Expense Description',
    expenseAmountInput: 'Amount',
    categoryDropdown: 'Category',
    payerDropdown: 'Paid By',
    splitEquallyCheckbox: 'Split with Everyone',
    saveExpenseButton: 'Save Expense',
    viewSummaryButton: 'View Settlement Summary',
    activityLogTab: 'Activities'
  },

  // Trip Summary & Settlement Screen
  TripSummary: {
    headerTitle: 'Settlements & Balances',
    balancesTab: 'Balances',
    settlementsTab: 'Settlements',
    iOweSection: 'You Owe',
    owesMeSection: 'Owed to You',
    otherSettlementsSection: 'Other Settlements',
    settleUpButton: 'Settle Debt',
    markAsPaidButton: 'Mark as Paid',
    transactionRefInput: 'UPI Ref / Notes',
    confirmPaymentButton: 'Confirm Settlement',
    exportReportButton: 'Export Report'
  }
};

module.exports = { Selectors };
