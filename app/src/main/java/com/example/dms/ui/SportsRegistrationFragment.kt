package com.example.dms.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.dms.R
import com.example.dms.models.*
import com.example.dms.utils.SessionManager
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AlertDialog

class SportsRegistrationFragment : Fragment() {

    private lateinit var selectSport: AutoCompleteTextView
    private lateinit var selectTeacher: AutoCompleteTextView
    private lateinit var selectTime: AutoCompleteTextView
    private lateinit var registerButton: MaterialButton
    private lateinit var sessionManager: SessionManager

    // Данные: вид спорта → преподаватели → время
    private val sportsData = mapOf(
        "Футбол" to mapOf(
            "Иванов И.И." to listOf("10:00", "14:00"),
            "Петров П.П." to listOf("12:00", "16:00")
        ),
        "Баскетбол" to mapOf(
            "Сидоров С.С." to listOf("09:00", "13:00"),
            "Кузнецов К.К." to listOf("11:00", "15:00")
        ),
        "Йога" to mapOf(
            "Алексеева А.А." to listOf("08:00", "18:00"),
            "Морозов М.М." to listOf("10:00", "17:00")
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sport, container, false)

        sessionManager = SessionManager(requireContext())
        selectSport = view.findViewById(R.id.selectSport)
        selectTeacher = view.findViewById(R.id.selectInstructor)
        selectTime = view.findViewById(R.id.selectTime)
        registerButton = view.findViewById(R.id.registerButton)

        registerButton.isEnabled = false
        registerButton.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.grey)

        setupSportDropdown()
        setupTeacherClick()
        setupTimeClick()
        setupTextWatchers()

        registerButton.setOnClickListener {
            val sport = selectSport.text.toString()
            val teacher = selectTeacher.text.toString()
            val time = selectTime.text.toString()

            if (sport.isEmpty() || teacher.isEmpty() || time.isEmpty()) {
                Toast.makeText(requireContext(), "Пожалуйста, выберите все поля", Toast.LENGTH_SHORT).show()
            } else {
                showRegistrationDialog(sport, teacher, time)
            }
        }

        return view
    }

    private fun setupSportDropdown() {
        val sports = sportsData.keys.toList()
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, sports)
        selectSport.setAdapter(adapter)
        selectSport.setOnClickListener { selectSport.showDropDown() }

        selectSport.setOnItemClickListener { _, _, position, _ ->
            val selectedSport = sports[position]
            selectTeacher.setText("", false)
            selectTime.setText("", false)
            setupTeacherDropdown(selectedSport)
        }
    }

    private fun setupTeacherClick() { selectTeacher.setOnClickListener { selectTeacher.showDropDown() } }

    private fun setupTimeClick() { selectTime.setOnClickListener { selectTime.showDropDown() } }

    private fun setupTeacherDropdown(sport: String) {
        val teachers = sportsData[sport]?.keys?.toList() ?: emptyList()
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, teachers)
        selectTeacher.setAdapter(adapter)

        selectTeacher.setOnItemClickListener { _, _, position, _ ->
            val selectedTeacher = teachers[position]
            selectTime.setText("", false)
            setupTimeDropdown(sport, selectedTeacher)
        }
    }

    private fun setupTimeDropdown(sport: String, teacher: String) {
        val times = sportsData[sport]?.get(teacher) ?: emptyList()
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, times)
        selectTime.setAdapter(adapter)
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { updateRegisterButtonState() }
            override fun afterTextChanged(s: Editable?) {}
        }

        selectSport.addTextChangedListener(watcher)
        selectTeacher.addTextChangedListener(watcher)
        selectTime.addTextChangedListener(watcher)
    }

    private fun updateRegisterButtonState() {
        val isFilled = selectSport.text.isNotEmpty() &&
                selectTeacher.text.isNotEmpty() &&
                selectTime.text.isNotEmpty()

        registerButton.isEnabled = isFilled
        registerButton.backgroundTintList = if (isFilled) {
            ContextCompat.getColorStateList(requireContext(), R.color.purple)
        } else {
            ContextCompat.getColorStateList(requireContext(), R.color.grey)
        }
    }

    private fun showRegistrationDialog(sport: String, teacher: String, time: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Подтверждение записи")
            .setMessage("Вы уверены, что хотите записаться на $sport с преподавателем $teacher в $time?")
            .setPositiveButton("Да") { dialog, _ ->
                saveRequestLocally(sport, teacher, time)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Сохраняем запрос локально через SharedPreferences
    private fun saveRequestLocally(sport: String, teacher: String, time: String) {
        val sharedPref = requireContext().getSharedPreferences("sports_requests", 0)
        val editor = sharedPref.edit()
        val id = System.currentTimeMillis()
        val requestString = "$sport|$teacher|$time|pending"
        editor.putString(id.toString(), requestString)
        editor.apply()

        Toast.makeText(requireContext(), "Запись сохранена локально", Toast.LENGTH_SHORT).show()

        selectSport.setText("", false)
        selectTeacher.setText("", false)
        selectTime.setText("", false)
        updateRegisterButtonState()
    }

    // Получение всех локальных запросов
    private fun getLocalRequests(): List<String> {
        val sharedPref = requireContext().getSharedPreferences("sports_requests", 0)
        return sharedPref.all.map { entry -> entry.value.toString() }
    }
}
