package com.example.calculatorapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    private lateinit var display: android.widget.EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        display = findViewById(R.id.input)
        display.setShowSoftInputOnFocus(false)

        display.setOnClickListener {
            if (display.text.toString() == "Enter a value…") {
                display.text.clear()
            }
        }

    }

    fun updateText(newString: String) {
        val oldString = display.text.toString()
        val cursorPosition = display.selectionStart
        val leftString = oldString.substring(0, cursorPosition)
        val rightString = oldString.substring(cursorPosition)
        if (oldString == "Enter a value…") {
            display.setText("")
        }
        display.setText(String.format("%s%s%s", leftString, newString, rightString))
        display.setSelection(cursorPosition + 1)
    }
    
    //methods for calculator number buttons

    fun zeroBTN(view: android.view.View) {
        updateText("0")
    }

    fun oneBTN(view: android.view.View) {
        updateText("1")        
    }

    fun twoBTN(view: android.view.View) {
        updateText("2")
    }

    fun threeBTN(view: android.view.View) {
        updateText("3")
    }

    fun fourBTN(view: android.view.View) {
        updateText("4")
    }

    fun fiveBTN(view: android.view.View) {
        updateText("5")
    }

    fun sixBTN(view: android.view.View) {
        updateText("6")
    }

    fun sevenBTN(view: android.view.View) {
        updateText("7")
    }

    fun eightBTN(view: android.view.View) {
        updateText("8")
    }

    fun nineBTN(view: android.view.View) {
        updateText("9")
    }

    //methods for calculator operator buttons

    fun addBTN(view: android.view.View) {
        updateText("+")
    }

    fun subtractBTN(view: android.view.View) {
        updateText("-")
    }

    fun multiplyBTN(view: android.view.View) {
        updateText("*")
    }

    fun divideBTN(view: android.view.View) {
        updateText("/")
    }

    fun equalsBTN(view: android.view.View) {
        val userExp = display.text.toString()
        val result: String
        try {
            val expression = ExpressionBuilder(userExp).build()
            val answer = expression.evaluate()
            result = answer.toString()
        } catch (e: Exception) {
            result = "Error"
        }
        display.setText(result)
    }

    fun clearBTN(view: android.view.View) {
        display.setText("")
    }

    fun decimalBTN(view: android.view.View) {
        updateText(".")
    }

    fun exponentBTN(view: android.view.View) {
        updateText("^")
    }

    fun plusMinusBTN(view: android.view.View) {
        updateText("-")
    }

    fun parenthesesBTN(view: android.view.View) {
        val oldString = display.text.toString()
        val cursorPosition = display.selectionStart
        var openPar = 0
        var closePar = 0
        for (i in 0 until cursorPosition) {
            if (oldString[i] == '(') {
                openPar++
            }
            if (oldString[i] == ')') {
                closePar++
            }
        }
        if (openPar == closePar || oldString[cursorPosition - 1] == '(') {
            updateText("(")
        } else if (closePar < openPar && oldString[cursorPosition - 1] != '(') {
            updateText(")")
        }
        display.setSelection(cursorPosition + 1)
        
    }

    fun backspaceBTN(view: android.view.View) {
        val oldString = display.text.toString()
        val cursorPosition = display.selectionStart
        val leftString = oldString.substring(0, cursorPosition - 1)
        val rightString = oldString.substring(cursorPosition)
        display.setText(String.format("%s%s", leftString, rightString))
        display.setSelection(cursorPosition - 1)        
    }





}