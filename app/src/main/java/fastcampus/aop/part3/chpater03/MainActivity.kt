package fastcampus.aop.part3.chpater03

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.w3c.dom.Text
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // step0 뷰를 초기화 해주기
        // step1 데이터 가져오기
        // step2 view에 데이터를 그려주기

        initOnOffButton()
        initChangeAlarmTimeButton()

        val model = fetchDataFromSharedPreferences()
        renderView(model)


        //

    }

    private fun initOnOffButton() {
        val onOffButton = findViewById<Button>(R.id.onOffButton)
        onOffButton.setOnClickListener {
            // 데이터를 확인한다
            val model = it.tag as? AlarmDisplayModel ?: return@setOnClickListener
            // as? 를 통해 형변환, tag 자체는 object로 저장되어 있음, alarmDisplaymodel인지는 모름
            // ? 를 붙이면, 형변환 실패 시 null로 떨어지게 된다
            // tag 가 alarmDisplaymodel 이면 model에 저장돼서 나오게 되겠죠
            val newModel = saveAlarmModel(model.hour, model.minute, model.onOff.not()) // onOff값은 반전시켜서 넣는다
            renderView(newModel)


            //온오프 여부에 따라 작업을 처리한다

            //오프 -> 알람 제거
            //온 -> 알람 등록
            if(newModel.onOff){
                // 켜진 경우 -> 알람을 등록
                // model에 있는 data로 calendar 인스턴스를 만들 것이다
                val calendar = Calendar.getInstance().apply{
                    set(Calendar.HOUR_OF_DAY, newModel.hour)
                    set(Calendar.MINUTE, newModel.minute)

                    if(before(Calendar.getInstance())){
                        // 내가 설정한 시간이 이미 지난 경우라면, DATE에 +1을 해준다
                        add(Calendar.DATE,1)
                    }
                }
                // 이제 알람매니저에 등록
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this,AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE,intent,
                PendingIntent.FLAG_UPDATE_CURRENT) // 플래그 설명: 기존 것이 있다면 새로운 인텐트로 업데이트 하겠다


                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )

            }else{
                // 꺼진 경우 -> 알람을 제거
                cancelAlarm()
            }

        }
    }

    private fun initChangeAlarmTimeButton() {

        val changeAlarmButton = findViewById<Button>(R.id.changeAlarmTimeButton)
        changeAlarmButton.setOnClickListener {

            //TimePickerDiaglog 띄워줘서 시간을 설정하도록 함
            val calendar = Calendar.getInstance() // 현재시간 가져오기

            TimePickerDialog(
                this,
                { picker, hour, minute ->

                    // 데이터를 저장한다
                    val model = saveAlarmModel(hour, minute, false)

                    // 뷰를 업데이트한다
                    renderView(model)

                    // 기존에 있던 알람을 삭제한다
                    cancelAlarm()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ) // false = 12시간 형식으로 표시
                .show()


        }

    }

    private fun saveAlarmModel(hour: Int, minute: Int, onOff: Boolean): AlarmDisplayModel {

        val model = AlarmDisplayModel(
            hour = hour,
            minute = minute,
            onOff = onOff
        )
        val sharedPreferences = getSharedPreferences(
            SHARED_PREFERENCE_NAME,
            Context.MODE_PRIVATE
        ) // 우리만 사용할 수 있도록 MODE 프라이빗으로
        //edit하면 에디터가 열리는데 거기서 하는 작업을 with를 통해 작성
        with(sharedPreferences.edit()) {
            putString(ALARM_KEY, model.makeDataForDB())
            putBoolean(ONOFF_KEY, model.onOff)
            commit()
        }
        return model
    }

    private fun fetchDataFromSharedPreferences(): AlarmDisplayModel {
        val sharedPreferences = getSharedPreferences(
            SHARED_PREFERENCE_NAME,
            Context.MODE_PRIVATE
        ) // 우리만 사용할 수 있도록 MODE 프라이빗으로

        val timeDBValue =
            sharedPreferences.getString(ALARM_KEY, "9:30") ?: "9:30" // null일 경우 9:30으로 설정
        val onOffDBValue = sharedPreferences.getBoolean(ONOFF_KEY, false)
        val alarmData = timeDBValue.split(":")

        val alarmModel = AlarmDisplayModel(
            hour = alarmData[0].toInt(),
            minute = alarmData[1].toInt(),
            onOff = onOffDBValue
        )

        // 보정 보정
        // 알람이 등록or해제 되어있는데 shared에는 반대로 되어 있는 경우를 예외처리 해야 함

        val pendingIntent = PendingIntent.getBroadcast(
            this, ALARM_REQUEST_CODE,
            Intent(this, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE
        ) // 없으면 null로 있으면 가져옴

        //pendingIntent가 null이면 알람이 핸드폰에 등록이 되어 있는 것임

        if ((pendingIntent == null) and alarmModel.onOff) {
            // 알람은 꺼져있는데, 데이터는 켜져있는 경우
            // 데이터를 수정해주면 됨
            alarmModel.onOff = false


        } else if ((pendingIntent != null) and alarmModel.onOff.not()) {
            // 알람은 켜져있는데, 데이터는 꺼져있는 경우
            // 알람을 취소함
            cancelAlarm()

        }
        return alarmModel


    }

    private fun renderView(model: AlarmDisplayModel) {
        findViewById<TextView>(R.id.ampmTextView).apply {
            text = model.ampmText
        }

        findViewById<TextView>(R.id.timeTextView).apply {
            text = model.timeText
        }
        findViewById<Button>(R.id.onOffButton).apply{
            text = model.onOffText
            tag = model
            // model을 tag에 잠시 저장해놓고 버튼을 눌렀을 때 tag에 있는 데이터를 가져와서
            // 구성하는 형식으로 구현
        }
    }

    private fun cancelAlarm(){

        val pendingIntent = PendingIntent.getBroadcast(
            this, ALARM_REQUEST_CODE,
            Intent(this, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE // 없으면 null로 있으면 가져옴
        )
        pendingIntent?.cancel() // null 일 수도 있기 때문에 옵셔널 처리
    }

    companion object {
        // 여긴 static 영역임

        private const val SHARED_PREFERENCE_NAME = "time"
        private const val ALARM_KEY = "alarm"
        private const val ONOFF_KEY = "onOff"
        private const val ALARM_REQUEST_CODE = 1000
    }

}