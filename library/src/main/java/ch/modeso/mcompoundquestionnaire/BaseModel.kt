package ch.modeso.mcompoundquestionnaire

import ch.modeso.mcompoundquestionnaire.QuestionnaireCardView

/**
 * Created by Hazem on 7/31/2017.
 */
open class BaseModel(var question: String, var status: QuestionnaireCardView.CardStatus = QuestionnaireCardView.CardStatus.NONE)