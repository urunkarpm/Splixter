import pytest
from appium.webdriver.common.appiumby import AppiumBy
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time

class TestSplixterE2E:
    def wait_for_element(self, driver, by, value, timeout=10):
        return WebDriverWait(driver, timeout).until(
            EC.presence_of_element_located((by, value))
        )

class TestSplixterE2E:
    def wait_for_element(self, driver, by, value, timeout=10):
        return WebDriverWait(driver, timeout).until(
            EC.presence_of_element_located((by, value))
        )

    def test_splixter_end_to_end_flow(self, driver):
        """End-to-End Test for Splixter"""
        
        # --- 1. App Launch & Splash Screen ---
        try:
            btn = self.wait_for_element(driver, AppiumBy.XPATH, "//android.widget.TextView[@text='Get Started']", timeout=5)
            btn.click()
        except:
            pass # already passed splash

        # --- 2. Add People ---
        name_input = self.wait_for_element(driver, AppiumBy.XPATH, "//android.widget.EditText")
        name_input.send_keys("Alice")
        add_btn = self.wait_for_element(driver, AppiumBy.ACCESSIBILITY_ID, "Add person")
        add_btn.click()

        name_input = self.wait_for_element(driver, AppiumBy.XPATH, "//android.widget.EditText")
        name_input.send_keys("Bob")
        add_btn.click()

        next_btn = self.wait_for_element(driver, AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Continue')]")
        next_btn.click()

        # --- 3. Add Items ---
        time.sleep(2)
        item_inputs = WebDriverWait(driver, 10).until(
            EC.presence_of_all_elements_located((AppiumBy.XPATH, "//android.widget.EditText"))
        )
        item_inputs[0].send_keys("Pizza")
        item_inputs[1].send_keys("500")
        
        add_item_btn = self.wait_for_element(driver, AppiumBy.ACCESSIBILITY_ID, "Add Item")
        add_item_btn.click()

        item_inputs = WebDriverWait(driver, 10).until(
            EC.presence_of_all_elements_located((AppiumBy.XPATH, "//android.widget.EditText"))
        )
        item_inputs[0].send_keys("Burger")
        item_inputs[1].send_keys("200")
        add_item_btn.click()

        try:
            driver.hide_keyboard()
        except:
            pass

        next_assign_btn = self.wait_for_element(driver, AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Next:')]")
        next_assign_btn.click()

        # --- 4. Assign Items ---
        time.sleep(2)
        alice_chips = WebDriverWait(driver, 10).until(
            EC.presence_of_all_elements_located((AppiumBy.XPATH, "//*[contains(@text, 'Alice')]"))
        )
        alice_chips[0].click()

        bob_chips = WebDriverWait(driver, 10).until(
            EC.presence_of_all_elements_located((AppiumBy.XPATH, "//*[contains(@text, 'Bob')]"))
        )
        if len(bob_chips) > 1:
            bob_chips[1].click()
        else:
            bob_chips[0].click()

        # --- 5. Tax, Tip, Discount ---
        # These are at the top of the ItemAssignmentScreen
        inputs = WebDriverWait(driver, 10).until(
            EC.presence_of_all_elements_located((AppiumBy.XPATH, "//android.widget.EditText"))
        )
        inputs[0].send_keys("50")
        inputs[1].send_keys("50")
        inputs[2].send_keys("100")

        next_calc_split = self.wait_for_element(driver, AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Calculate')]")
        next_calc_split.click()

        # --- 6. Summary ---
        time.sleep(2)
        summary_title = self.wait_for_element(driver, AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Final')]")
        assert summary_title is not None
