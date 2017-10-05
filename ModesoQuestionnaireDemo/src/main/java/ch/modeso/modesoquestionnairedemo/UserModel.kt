package ch.modeso.modesoquestionnairedemo

import ch.modeso.modesoquestionnaire.BaseModel
import ch.modeso.modesoquestionnaire.QuestionnaireCardView
import java.util.*

/**
 * Created by Hazem on 8/23/2017.
 */
class UserModel(question: String, var userAdditionalInfo: String,
                status: QuestionnaireCardView.CardStatus = QuestionnaireCardView.CardStatus.NONE,
                id: String = UUID.randomUUID().toString()) : BaseModel(question, status, id)