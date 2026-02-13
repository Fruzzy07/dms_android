package com.example.dms.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.dms.R
import com.example.dms.models.*
import com.example.dms.network.RetrofitClient
import com.example.dms.utils.SessionManager
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AlertDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target

class ResidenceFragment : Fragment() {

    private lateinit var selectBuilding: AutoCompleteTextView
    private lateinit var selectFloor: AutoCompleteTextView
    private lateinit var selectRoom: AutoCompleteTextView
    private lateinit var checkInButton: MaterialButton
    private lateinit var liveStatusText: TextView
    private lateinit var previewImage: ImageView
    private lateinit var selectResidenceContainer: View
    private lateinit var currentResidenceContainer: View
    private lateinit var currentRoomText: TextView
    private lateinit var changeRoomButton: MaterialButton
    private lateinit var evictButton: MaterialButton
    private lateinit var sessionManager: SessionManager

    private val buildings = mutableListOf<Building>()
    private val floors = mutableListOf<Floor>()
    private val rooms = mutableListOf<Room>()
    private var selectedBuilding: Building? = null
    private var selectedFloor: Floor? = null
    private var selectedRoom: Room? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_residence, container, false)

        sessionManager = SessionManager(requireContext())
        selectResidenceContainer = view.findViewById(R.id.selectResidenceContainer)
        currentResidenceContainer = view.findViewById(R.id.currentResidenceContainer)
        selectBuilding = view.findViewById(R.id.selectBuilding)
        selectFloor = view.findViewById(R.id.selectFloor)
        selectRoom = view.findViewById(R.id.selectRoom)
        checkInButton = view.findViewById(R.id.checkInButton)
        liveStatusText = view.findViewById(R.id.liveStatusText)
        previewImage = view.findViewById(R.id.residencePreviewImage)
        currentRoomText = view.findViewById(R.id.currentRoomText)
        changeRoomButton = view.findViewById(R.id.changeRoomButton)
        evictButton = view.findViewById(R.id.evictButton)

        checkInButton.isEnabled = false
        checkInButton.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.grey)

        loadBuildings()
        setupFloorClick()
        setupRoomClick()
        setupTextWatchers()

        // initial preview: all buildings
        updateResidencePreview()

        checkInButton.setOnClickListener {
            if (selectedRoom == null) {
                Toast.makeText(requireContext(), "Пожалуйста, выберите все поля", Toast.LENGTH_SHORT).show()
            } else {
                val buildingName = selectedBuilding?.address ?: "Неизвестно"
                val floorNum = selectedFloor?.floorNumber ?: 0
                val roomNum = selectedRoom?.roomNumber ?: ""
                showCheckInDialog(buildingName, floorNum.toString(), roomNum)
            }
        }

        changeRoomButton.setOnClickListener {
            // UI should return to selection (как 1 фото)
            sessionManager.setResidenceChangeMode(true)
            sessionManager.setResidenceChangeBaseAcceptedRequestId(sessionManager.getResidenceLastAcceptedRequestId())
            showSelectionMode()
            clearSelection()
            enableSelectionInputs(true)
            liveStatusText.visibility = View.GONE
            updateResidencePreview()
        }

        evictButton.setOnClickListener {
            showEvictDialog()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        refreshResidenceModeFromServer()
    }

    private fun loadBuildings() {
        val token = sessionManager.getToken() ?: return
        val api = RetrofitClient.getInstance(token)

        api.getBuildings().enqueue(object : Callback<ApiResponse<List<Building>>> {
            override fun onResponse(call: Call<ApiResponse<List<Building>>>, response: Response<ApiResponse<List<Building>>>) {
                if (!isAdded) return
                val data = response.body()?.data
                if (response.isSuccessful && data != null) {
                    buildings.clear()
                    buildings.addAll(data)
                    setupBuildingDropdown()
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки корпусов", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Building>>>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupBuildingDropdown() {
        val buildingNames = buildings.map { it.address }
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, buildingNames)
        selectBuilding.setAdapter(adapter)

        selectBuilding.setOnClickListener { selectBuilding.showDropDown() }

        selectBuilding.setOnItemClickListener { _, _, position, _ ->
            selectedBuilding = buildings[position]
            selectFloor.setText("", false)
            selectRoom.setText("", false)
            selectedFloor = null
            selectedRoom = null
            liveStatusText.visibility = View.GONE
            enableSelectionInputs(true)
            updateResidencePreview()
            loadFloors(selectedBuilding!!.id)
        }
    }

    private fun setupFloorClick() { selectFloor.setOnClickListener { selectFloor.showDropDown() } }

    private fun setupRoomClick() { selectRoom.setOnClickListener { selectRoom.showDropDown() } }

    private fun loadFloors(buildingId: Int) {
        val token = sessionManager.getToken() ?: return
        val api = RetrofitClient.getInstance(token)

        api.getFloorsByBuilding(buildingId).enqueue(object : Callback<ApiResponse<List<Floor>>> {
            override fun onResponse(call: Call<ApiResponse<List<Floor>>>, response: Response<ApiResponse<List<Floor>>>) {
                if (!isAdded) return
                val data = response.body()?.data
                if (response.isSuccessful && data != null) {
                    floors.clear()
                    floors.addAll(data)
                    setupFloorDropdown()
                } else {
                    floors.clear()
                    setupFloorDropdown()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Floor>>>, t: Throwable) {
                if (!isAdded) return
                floors.clear()
                setupFloorDropdown()
            }
        })
    }

    private fun setupFloorDropdown() {
        val floorNumbers = floors.map { it.floorNumber.toString() }
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, floorNumbers)
        selectFloor.setAdapter(adapter)

        selectFloor.setOnItemClickListener { _, _, position, _ ->
            selectedFloor = floors[position]
            selectRoom.setText("", false)
            selectedRoom = null
            liveStatusText.visibility = View.GONE
            updateResidencePreview()
            loadRooms(selectedFloor!!.id)
        }
    }

    private fun loadRooms(floorId: Int) {
        val token = sessionManager.getToken() ?: return
        val api = RetrofitClient.getInstance(token)

        api.getRoomsByFloor(floorId).enqueue(object : Callback<ApiResponse<List<Room>>> {
            override fun onResponse(call: Call<ApiResponse<List<Room>>>, response: Response<ApiResponse<List<Room>>>) {
                if (!isAdded) return
                val data = response.body()?.data
                if (response.isSuccessful && data != null) {
                    rooms.clear()
                    rooms.addAll(data)
                    setupRoomDropdown()
                } else {
                    rooms.clear()
                    setupRoomDropdown()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Room>>>, t: Throwable) {
                if (!isAdded) return
                rooms.clear()
                setupRoomDropdown()
            }
        })
    }

    private fun setupRoomDropdown() {
        val roomNumbers = rooms.map { it.roomNumber }
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, roomNumbers)
        selectRoom.setAdapter(adapter)

        selectRoom.setOnItemClickListener { _, _, position, _ ->
            selectedRoom = rooms[position]
            liveStatusText.visibility = View.GONE
            updateResidencePreview()
            updateCheckInButtonState()
        }
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { updateCheckInButtonState() }
            override fun afterTextChanged(s: Editable?) {}
        }

        selectBuilding.addTextChangedListener(watcher)
        selectFloor.addTextChangedListener(watcher)
        selectRoom.addTextChangedListener(watcher)
    }

    private fun updateCheckInButtonState() {
        val isFilled = selectedBuilding != null && selectedFloor != null && selectedRoom != null
        checkInButton.isEnabled = isFilled
        checkInButton.backgroundTintList = if (isFilled)
            ContextCompat.getColorStateList(requireContext(), R.color.purple)
        else
            ContextCompat.getColorStateList(requireContext(), R.color.grey)
    }

    private fun showCheckInDialog(building: String, floor: String, room: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Подтверждение заселения")
            .setMessage("Вы уверены, что хотите подать запрос на проживание в $building, этаж $floor, комната $room?")
            .setPositiveButton("Да") { dialog, _ ->
                submitRequestLive()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun submitRequestLive() {
        val roomId = selectedRoom?.id ?: return
        val token = sessionManager.getToken() ?: return
        val api = RetrofitClient.getInstance(token)

        checkInButton.isEnabled = false
        val request = RequestLiveCreate(roomId = roomId)

        api.createRequestLive(request).enqueue(object : Callback<ApiResponse<RequestLive>> {
            override fun onResponse(call: Call<ApiResponse<RequestLive>>, response: Response<ApiResponse<RequestLive>>) {
                checkInButton.isEnabled = true
                if (!isAdded) return
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Запрос на проживание отправлен", Toast.LENGTH_SHORT).show()
                    saveLocalLiveRequest(response.body()?.data)
                    // Пока менеджер не подтвердит, показываем статус и блокируем новые действия
                    liveStatusText.visibility = View.VISIBLE
                    enableSelectionInputs(false)
                    clearSelection()
                } else {
                    Toast.makeText(requireContext(), "Ошибка: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<RequestLive>>, t: Throwable) {
                checkInButton.isEnabled = true
                if (!isAdded) return
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clearSelection() {
        selectBuilding.setText("", false)
        selectFloor.setText("", false)
        selectRoom.setText("", false)
        selectedBuilding = null
        selectedFloor = null
        selectedRoom = null
        updateCheckInButtonState()
        updateResidencePreview()
    }

    private fun enableSelectionInputs(enabled: Boolean) {
        selectBuilding.isEnabled = enabled
        selectFloor.isEnabled = enabled
        selectRoom.isEnabled = enabled

        if (!enabled) {
            checkInButton.isEnabled = false
            checkInButton.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.grey)
        } else {
            updateCheckInButtonState()
        }
    }

    private fun showSelectionMode() {
        currentResidenceContainer.visibility = View.GONE
        selectResidenceContainer.visibility = View.VISIBLE
    }

    private fun showCurrentMode(request: RequestLive) {
        selectResidenceContainer.visibility = View.GONE
        currentResidenceContainer.visibility = View.VISIBLE

        sessionManager.setResidenceLastAcceptedRequestId(request.id)

        val building = request.room?.floor?.building?.address ?: "Корпус ?"
        val floorNum = request.room?.floor?.floorNumber?.toString() ?: "-"
        val roomNum = request.room?.roomNumber ?: request.roomId.toString()

        currentRoomText.text = "Ваша комната: $building, этаж $floorNum, комната $roomNum"
    }

    private fun refreshResidenceModeFromServer() {
        val token = sessionManager.getToken() ?: run {
            showSelectionMode()
            return
        }

        val api = RetrofitClient.getInstance(token)
        api.getLiveRequests().enqueue(object : Callback<ApiResponse<List<RequestLive>>> {
            override fun onResponse(call: Call<ApiResponse<List<RequestLive>>>, response: Response<ApiResponse<List<RequestLive>>>) {
                if (!isAdded) return
                val data = response.body()?.data
                if (!response.isSuccessful || data == null) {
                    showSelectionMode()
                    return
                }

                val userId = sessionManager.getUserId()?.toIntOrNull()
                val all = data
                val mine = if (userId != null) all.filter { it.userId == userId } else all

                val accepted = mine
                    .filter { it.status.equals("accepted", true) }
                    .maxByOrNull { it.id }

                val pending = mine
                    .filter { it.status.equals("pending", true) }
                    .maxByOrNull { it.id }

                // Если пользователь нажал "Поменять комнату" — всегда показываем выбор (1 фото),
                // даже если у него уже есть принятая комната.
                if (sessionManager.isResidenceChangeMode()) {
                    // Если приняли НОВУЮ комнату уже после перехода в режим смены — возвращаемся в экран проживания.
                    val baseAcceptedId = sessionManager.getResidenceChangeBaseAcceptedRequestId()
                    if (accepted != null && accepted.id > baseAcceptedId) {
                        sessionManager.setResidenceChangeMode(false)
                        liveStatusText.visibility = View.GONE
                        showCurrentMode(accepted)
                        return
                    }

                    showSelectionMode()
                    if (pending != null) {
                        liveStatusText.visibility = View.VISIBLE
                        enableSelectionInputs(false)
                    } else {
                        liveStatusText.visibility = View.GONE
                        enableSelectionInputs(true)
                    }
                    return
                }

                // Если менеджер подтвердил (accepted) — показываем экран проживания (3 фото).
                if (accepted != null) {
                    liveStatusText.visibility = View.GONE
                    showCurrentMode(accepted)
                    return
                }

                // Иначе показываем выбор (1 фото).
                showSelectionMode()

                if (pending != null) {
                    liveStatusText.visibility = View.VISIBLE
                    enableSelectionInputs(false)
                } else {
                    liveStatusText.visibility = View.GONE
                    enableSelectionInputs(true)
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<RequestLive>>>, t: Throwable) {
                if (!isAdded) return
                // Если не удалось загрузить статус — просто оставляем режим выбора.
                showSelectionMode()
            }
        })
    }

    private fun saveLocalLiveRequest(created: RequestLive?) {
        val prefs = requireContext().getSharedPreferences("live_requests", 0)
        val key = (created?.id ?: System.currentTimeMillis()).toString()
        val room = selectedRoom?.roomNumber ?: (created?.room?.roomNumber ?: "-")
        val floor = selectedFloor?.floorNumber?.toString() ?: (created?.room?.floor?.floorNumber?.toString() ?: "-")
        prefs.edit().putString(key, "$room|$floor|pending").apply()
    }

    private fun showEvictDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Подтверждение")
            .setMessage("Вы уверены, что хотите выселиться?")
            .setPositiveButton("Да") { dialog, _ ->
                // Backend endpoint for eviction is not defined in this app.
                // For now, return to selection mode (как 1 фото).
                sessionManager.setResidenceChangeMode(true)
                showSelectionMode()
                clearSelection()
                enableSelectionInputs(true)
                Toast.makeText(requireContext(), "Заявка на выселение: функция пока не подключена", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Отменить") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateResidencePreview() {
        val packageName = requireContext().packageName

        fun lookupDrawable(name: String): Int? {
            val id = resources.getIdentifier(name, "drawable", packageName)
            return if (id != 0) id else null
        }

        val (drawableRes, crop) = when {
            selectedRoom != null -> {
                val dynamic = lookupDrawable("residence_room_${selectedRoom!!.id}")
                (dynamic ?: R.drawable.residence_preview_room) to (dynamic != null)
            }
            selectedFloor != null -> {
                val dynamic = lookupDrawable("residence_floor_${selectedFloor!!.id}")
                (dynamic ?: R.drawable.floor_1_1_2) to (dynamic != null)
            }
            selectedBuilding != null -> {
                val dynamic = lookupDrawable("residence_building_${selectedBuilding!!.id}")
                (dynamic ?: R.drawable.residence_preview_building) to (dynamic != null)
            }
            else -> R.drawable.buildings to false
        }

        // Загрузка уменьшенной картинки
        val targetHeightPx = dpToPx(200)
        Glide.with(this)
            .load(drawableRes)
            .override(Target.SIZE_ORIGINAL, targetHeightPx)
            .into(previewImage)
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
