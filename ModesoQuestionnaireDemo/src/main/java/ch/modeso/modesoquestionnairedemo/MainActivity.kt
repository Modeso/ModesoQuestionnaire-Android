package ch.modeso.modesoquestionnairedemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import ch.modeso.modesoquestionnaire.CardInteractionCallbacks
import ch.modeso.modesoquestionnaire.QuestionnaireCardView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), CardInteractionCallbacks {

    val title = ArrayList<UserModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.elevation = 0f
        val size = 50
        for (i in 0..size - 1) {
            title.add(UserModel("Hello $i", "additional info $i",QuestionnaireCardView.CardStatus.NONE))
        }
        mcompound_questionnaire.updateList(title.toMutableList())
        mcompound_questionnaire.cardInteractionCallBacks = this
    }

    override fun itemAcceptClick(itemId: String) {
        Toast.makeText(this, "item $itemId Accepted", Toast.LENGTH_SHORT).show()
        val items = title.filter { it.id == itemId }
        items.forEach {
            Toast.makeText(this, "item ${it.userAdditionalInfo} Accepted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun itemCancelClick(itemId: String) {
        Toast.makeText(this, "item $itemId Canceled", Toast.LENGTH_SHORT).show()
        val items = title.filter { it.id == itemId }
        items.forEach {
            Toast.makeText(this, "item ${it.userAdditionalInfo} Canceled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun itemNone(itemId: String) {
        Toast.makeText(this, "item $itemId Idle", Toast.LENGTH_SHORT).show()
        val items = title.filter { it.id == itemId }
        items.forEach {
            Toast.makeText(this, "item ${it.userAdditionalInfo} Idle", Toast.LENGTH_SHORT).show()
        }
    }

    override fun itemDismiss(itemId: String) {
        Toast.makeText(this, "item $itemId Dismissed", Toast.LENGTH_SHORT).show()
        val items = title.filter { it.id == itemId }
        items.forEach {
            Toast.makeText(this, "item ${it.userAdditionalInfo} Dismissed", Toast.LENGTH_SHORT).show()
        }
    }

}
