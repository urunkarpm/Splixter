import pytest
from appium import webdriver
from appium.options.android import UiAutomator2Options
import subprocess
import time

@pytest.fixture(scope="session", autouse=True)
def start_appium():
    """Starts the Appium server before the test session and tears it down after."""
    print("Starting Appium Server...")
    # Redirect output to DEVNULL to prevent PIPE buffer deadlock
    process = subprocess.Popen(
        ['appium', '-p', '4723'],
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
        shell=True
    )
    time.sleep(10)  # Give Appium time to start
    yield
    print("Stopping Appium Server...")
    process.terminate()

@pytest.fixture(scope="function")
def driver():
    """Creates an Appium driver for the device."""
    options = UiAutomator2Options()
    options.platform_name = 'Android'
    options.automation_name = 'UiAutomator2'
    options.app_package = 'com.example.splixter'
    options.app_activity = '.MainActivity'
    options.no_reset = False
    options.ensure_webviews_have_pages = True
    options.native_web_screenshot = True
    options.new_command_timeout = 3600

    driver = webdriver.Remote("http://127.0.0.1:4723", options=options)
    driver.implicitly_wait(10)
    
    yield driver
    
    driver.quit()
