package ch.modeso.mcompoundquestionnaire

/**
 * Created by Hazem on 7/28/2017.
 */
interface CardInteractionCallbacks {
    fun itemAcceptClick(itemId: String)
    fun itemCancelClick(itemId: String)
    fun itemNone(itemId: String)
    fun itemDismiss(itemId: String)
}