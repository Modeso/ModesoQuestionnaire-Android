package ch.modeso.mcompoundquestionnaire

import java.util.*

/**
 * Created by Hazem on 7/31/2017.
 */
open class BaseModel(var question: String, var status: QuestionnaireCardView.CardStatus = QuestionnaireCardView.CardStatus.NONE, val id: String = UUID.randomUUID().toString())