package ch.modeso.modesoquestionnaire
/* ktlint-disable no-wildcard-imports */
import java.util.*

/**
 * Created by Hazem on 7/31/2017.
 */
open class BaseModel(var question: String, var status: QuestionnaireCardView.CardStatus = QuestionnaireCardView.CardStatus.NONE, val id: String = UUID.randomUUID().toString())
