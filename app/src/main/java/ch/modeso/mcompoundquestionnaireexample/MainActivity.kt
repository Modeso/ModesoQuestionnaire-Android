package ch.modeso.mcompoundquestionnaireexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import ch.modeso.mcompoundquestionnaire.CardInteractionCallbacks
import ch.modeso.mcompoundquestionnaire.QuestionnaireCardView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), CardInteractionCallbacks {

    val title = ArrayList<UserModel>()
    val randText = listOf<String>("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus semper, nulla at bibendum auctor, lacus velit rutrum leo",
            "At lobortis velit nisl non turpis. Praesent feugiat dui at massa consequat, sit amet pulvinar risus aliquet. ",
            "Aliquam nec viverra mauris. Nulla libero justo, euismod at dignissim et, elementum eu felis. Vestibulum mollis nisl in volutpat molestie.",
            "Sed consectetur, metus fermentum posuere semper, nunc nisl luctus lectus, sit amet placerat ipsum leo ut ante. Donec consequat diam nibh,",
            "In consequat massa pellentesque ut. Sed suscipit et enim tincidunt tempor. Mauris nec efficitur lacus, in consectetur felis. ")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.elevation = 0f
        for (i in 0..4) {
            title.add(UserModel(randText[i % 5], "additional info $i", QuestionnaireCardView.CardStatus.NONE))
        }
        mcompound_questionnaire.updateList(title.toMutableList())
        mcompound_questionnaire.cardInteractionCallBacks = this
    }

    override fun onItemAcceptClick(itemId: String) {
        Toast.makeText(this, "item $itemId Accepted", Toast.LENGTH_SHORT).show()
        val items = title.filter { it.id == itemId }
        items.forEach {
            Toast.makeText(this, "item ${it.userAdditionalInfo} Accepted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemCancelClick(itemId: String) {
        Toast.makeText(this, "item $itemId Canceled", Toast.LENGTH_SHORT).show()
        val items = title.filter { it.id == itemId }
        items.forEach {
            Toast.makeText(this, "item ${it.userAdditionalInfo} Canceled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemNone(itemId: String) {
        Toast.makeText(this, "item $itemId Idle", Toast.LENGTH_SHORT).show()
        val items = title.filter { it.id == itemId }
        items.forEach {
            Toast.makeText(this, "item ${it.userAdditionalInfo} Idle", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemDismiss(itemId: String) {
        Toast.makeText(this, "item $itemId Dismissed", Toast.LENGTH_SHORT).show()
        val items = title.filter { it.id == itemId }
        items.forEach {
            Toast.makeText(this, "item ${it.userAdditionalInfo} Dismissed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onQuestionnaireFinish() {
        Toast.makeText(this, "Questionnaire is Finished thank you", Toast.LENGTH_SHORT).show()
    }

}
