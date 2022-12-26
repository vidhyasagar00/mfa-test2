package com.net.routee.otpdetection

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.net.routee.R
import kotlinx.android.synthetic.main.otp_validation_xml.view.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class OTPValidationFragmentTest {
    private lateinit var scenario: FragmentScenario<OTPValidationFragment>

    /**
     * Added unit tests are
     * 1. Trying to click the button with empty text box
     * 2. Trying to click the button without adding 4 digits OTP
     * 3. Trying to click the button by adding 4 digits of OTP
     * 4. Finding the maximum size of text box by entering more than 4 digits
     * 5. Check the received message from OTP
     * NOTE - we have to check final one only on debugging
     */

    /**
     * Starting the fragment which where we have written our code
     */
    @Before
    fun setUp() {
        scenario = launchFragmentInContainer ()
        scenario.moveToState(Lifecycle.State.STARTED)
    }

    /**
     * 1. Trying to click the button with empty text box
     */
    @Test
    fun buttonClickWithEmptyTextBox() {
        onView(withId(R.id.submitBTN)).check { view, _ ->
            Assert.assertTrue(!view.isEnabled)
        }
    }

    /**
     * 2. Trying to click the button without adding 4 digits OTP
     */
    @Test
    fun buttonClickWithOutEmptyTextBox() {
        onView(withId(R.id.textBox)).perform(typeText("0"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.submitBTN)).check { view, _ ->
            Assert.assertTrue(!view.isEnabled)
        }
    }

    /**
     * 3. Trying to click the button by adding 4 digits of OTP
     */
    @Test
    fun buttonClickWithFilledTextBox() {
        onView(withId(R.id.textBox)).perform(typeText("0000"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.submitBTN)).check { view, _ ->
            Assert.assertTrue(view.isEnabled)
        }
    }

    /**
     * 4. Finding the maximum size of text box by entering more than 4 digits
     */
    @Test
    fun checkingTextBoxMaxLength() {
        val textBox = onView(withId(R.id.textBox))
        textBox.perform(typeText("123456"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.textBox)).check { view, _ ->
            Assert.assertTrue(view.textBox.text.length == 4)
        }
    }

    /**
     * 5. Check the received message from OTP
     * NOTE - we have to check this one only on debugging
     */
    @Test
    fun addingOTPOnceItReceived() {
        var keyHash = ""
        scenario.onFragment { fragment ->
            keyHash = fragment.checkKeyHash()
        }
        val message = "Hello your OTP is 1234 RLwuXSNatck"
        print("KeyHash: $keyHash, Message: $message")
        onView(withId(R.id.textBox)).check { view, _ ->
            Assert.assertTrue(view.textBox.text.length == 4)
        }
    }

}