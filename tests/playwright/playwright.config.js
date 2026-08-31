// @ts-check
const { defineConfig } = require('@playwright/test');
const path = require('path');

/**
 * Playwright configuration for Splixter Android App testing
 */
module.exports = defineConfig({
  testDir: path.join(__dirname, 'src', 'specs'),
  timeout: 60000,
  expect: {
    timeout: 10000,
  },
  fullyParallel: false, // Run mobile tests sequentially to prevent device state collisions
  workers: 1,
  reporter: [
    ['list'],
    ['html', { outputFolder: 'test-results/html-report', open: 'never' }],
    ['json', { outputFile: 'test-results/test-summary.json' }]
  ],
  use: {
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
});
