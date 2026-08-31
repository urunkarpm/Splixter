/**
 * TestData.js
 * Comprehensive fixtures for positive, negative, edge-case, and security testing
 */

const TestData = {
  // Edge Case Strings
  Strings: {
    empty: '',
    whitespaceOnly: '     ',
    singleChar: 'A',
    extremelyLong: 'A'.repeat(300),
    specialCharacters: '!@#$%^&*()_+-=[]{}|;:\'",.<>?/~`',
    unicodeEmojis: '🎉🚀✨🍕🍔🥂🍷🍺🔥',
    sqlInjection: "' OR '1'='1; DROP TABLE users; --",
    htmlXssPayload: '<script>alert("xss")</script><img src=x onerror=alert(1)>',
    newlineInjected: "Line1\nLine2\nLine3"
  },

  // Phone Numbers
  PhoneNumbers: {
    valid10Digit: '9876543210',
    validWithCountryCode: '+919876543210',
    invalidWithLetters: '98765abcde',
    tooShort: '12345',
    tooLong: '12345678901234567890',
    allZeros: '0000000000'
  },

  // UPI IDs
  UpiIds: {
    validGooglePay: 'user@okaxis',
    validPhonePe: 'splixter@ybl',
    validPaytm: 'john.doe@paytm',
    missingAtSymbol: 'usernobank',
    multipleAtSymbols: 'user@@okaxis',
    missingHandle: 'username@',
    leadingAtSymbol: '@okhdfcbank',
    spacesInUpi: 'user name@okicici'
  },

  // Numeric Edge Cases (Prices, Percentages, Splits)
  Numbers: {
    zero: '0',
    zeroPointZero: '0.00',
    negativeStandard: '-50.00',
    negativeFraction: '-0.01',
    alphanumeric: '12abc.45',
    multipleDecimals: '12.34.56',
    extremeOverflow: '999999999999.99',
    tinyFraction: '0.00001',
    percentageOver100: '150',
    negativePercentage: '-15'
  },

  // Malformed & Real OCR Receipt Texts for Paste Bill Dialog
  Receipts: {
    validStandard: `
      1 Butter Chicken 380.00
      2 Garlic Naan 120.00
      2 Kingfisher Beer 440.00
      1 Mineral Water 40.00
    `,
    validLiquorAndFood: `
      1 Margarita Cocktail 450
      1 Chicken Biryani 320
      2 Heineken Draught 600
      1 Chocolate Brownie 180
    `,
    malformedNoPrices: `
      Order summary:
      Item One
      Item Two
      Thank you for dining with us!
    `,
    malformedOnlySymbols: `
      @@@ $$$ %%% ***
      ### !!! === +++
    `,
    mixedCurrencies: `
      1 Caesar Salad $14.50
      2 Craft Beer ₹500.00
      1 Espresso €3.50
    `,
    hugeMultilineSpam: Array.from({ length: 50 }, (_, i) => `Item_${i + 1} ${(i + 1) * 10}.00`).join('\n')
  },

  // Lobby Code Fixtures
  LobbyCodes: {
    validFormat: 'SPLIX-4892',
    validCustom: 'GOA-2026',
    invalidTooShort: 'SPL-1',
    invalidSpecialChars: 'SPLIX@#$',
    nonExistentLobby: 'SPLIX-9999'
  },

  // Preset Users & Groups
  Presets: {
    standardUsers: [
      { name: 'Alice', emoji: '👩', color: 0xFF6C5CE7 },
      { name: 'Bob', emoji: '🧔', color: 0xFF00CEC9 },
      { name: 'Charlie', emoji: '🧑‍🦰', color: 0xFFFF7675 },
      { name: 'Diana', emoji: '👩‍⚕️', color: 0xFFFDCB6E }
    ],
    sampleGroup: {
      name: 'Weekend Goa Squad',
      members: ['Alice', 'Bob', 'Charlie', 'Diana']
    }
  }
};

module.exports = { TestData };
