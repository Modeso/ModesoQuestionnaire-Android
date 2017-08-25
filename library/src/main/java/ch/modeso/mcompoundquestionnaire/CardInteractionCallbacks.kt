package ch.modeso.mcompoundquestionnaire

/**
 * Created by Hazem on 7/28/2017.
 */
interface CardInteractionCallbacks {
    fun onItemAcceptClick(itemId: String)
    fun onItemCancelClick(itemId: String)
    fun onItemNone(itemId: String)
    fun onItemDismiss(itemId: String)
    fun onQuestionnaireFinish()
}