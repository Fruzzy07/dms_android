package com.example.dms.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.dms.R
import com.example.dms.models.ApiResponse
import com.example.dms.models.RequestRepair
import com.example.dms.models.RequestRepairCreate
import com.example.dms.network.RetrofitClient
import com.example.dms.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepairRequestFragment : Fragment() {

    private lateinit var employeeField: TextInputEditText
    private lateinit var descriptionField: TextInputEditText
    private lateinit var attachButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var fileNameText: TextView

    private var attachedFileUri: Uri? = null

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                attachedFileUri = uri
                fileNameText.text = uri.lastPathSegment ?: "Файл выбран"
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_repair_request, container, false)

        employeeField = view.findViewById(R.id.inputEmployee)
        descriptionField = view.findViewById(R.id.inputDescription)
        attachButton = view.findViewById(R.id.btnAttach)
        saveButton = view.findViewById(R.id.btnSave)
        cancelButton = view.findViewById(R.id.btnCancel)
        fileNameText = view.findViewById(R.id.tvFileName)

        attachButton.setOnClickListener { openFilePicker() }
        cancelButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        saveButton.setOnClickListener { submitRepairRequest() }

        return view
    }

    private fun openFilePicker() {
        filePickerLauncher.launch(arrayOf("*/*"))
    }

    private fun submitRepairRequest() {
        val description = descriptionField.text?.toString()?.trim().orEmpty()
        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Опишите проблему", Toast.LENGTH_SHORT).show()
            return
        }

        val token = SessionManager(requireContext()).getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Требуется авторизация", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.getInstance(token)
        val body = RequestRepairCreate(description = description)

        saveButton.isEnabled = false

        api.createRequestRepair(body).enqueue(object : Callback<ApiResponse<RequestRepair>> {
            override fun onResponse(
                call: Call<ApiResponse<RequestRepair>>,
                response: Response<ApiResponse<RequestRepair>>
            ) {
                saveButton.isEnabled = true

                if (!isAdded) return

                if (response.isSuccessful && response.body()?.data != null) {
                    Toast.makeText(requireContext(), "Заявка на ремонт отправлена", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                } else {
                    Toast.makeText(requireContext(), "Ошибка: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<RequestRepair>>, t: Throwable) {
                saveButton.isEnabled = true
                if (!isAdded) return
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

