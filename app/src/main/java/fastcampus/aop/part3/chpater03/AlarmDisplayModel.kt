package fastcampus.aop.part3.chpater03

data class AlarmDisplayModel(
    val hour: Int,
    val minute: Int,
    var onOff: Boolean,
){
    val timeText: String
        get(){
            val h = "%02d".format(if(hour<12) hour else hour -12) //두자리수까지 int형으로, 두자리수가 되지않는 앞 공백은 0으로 채운다
            val m = "%02d".format(minute)

            return "$h:$m"
        }

    val ampmText: String
        get(){
            return if(hour<12)"AM" else "PM"
        }

    val onOffText: String
        get(){
            return if (onOff) "알람 끄기" else "알람 켜기"
        }

    fun makeDataForDB() : String {
        return "$hour:$minute" // split을 할 때 key로써 : 을 이용하기 위해 이렇게 지정
    }

}