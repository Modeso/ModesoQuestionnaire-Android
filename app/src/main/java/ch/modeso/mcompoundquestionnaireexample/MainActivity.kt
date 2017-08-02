package ch.modeso.mcompoundquestionnaireexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ch.modeso.mcompoundquestionnaire.BaseModel
import ch.modeso.mcompoundquestionnaire.QuestionnaireCardView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.elevation = 0f
        val title = ArrayList<BaseModel>()
        val size = 50
        for (i in 0..size - 1) {
            title.add(BaseModel("Hello" + i, QuestionnaireCardView.CardStatus.NONE))
        }
        mcompound_questionnaire.updateList(title)

    }

}
