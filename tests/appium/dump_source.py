from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time

options = UiAutomator2Options()
options.platform_name = 'Android'
options.automation_name = 'UiAutomator2'
options.app_package = 'com.example.splixter'
options.app_activity = '.MainActivity'
options.no_reset = False

driver = webdriver.Remote("http://127.0.0.1:4723", options=options)
wait = WebDriverWait(driver, 10)

time.sleep(5) # wait for app to load
# Click Get Started
btn = wait.until(EC.presence_of_element_located((AppiumBy.XPATH, "//android.widget.TextView[@text='Get Started']")))
btn.click()
time.sleep(2)

with open('source_people.xml', 'w', encoding='utf-8') as f:
    f.write(driver.page_source)

driver.quit()
