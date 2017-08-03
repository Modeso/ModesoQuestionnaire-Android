package ch.modeso.mcompoundquestionnaire

/**
 * Created by Hazem on 7/28/2017.
 */
interface CardInteractionCallbacks {
    fun itemAcceptClick(itemPosition: Int)
    fun itemCancelClick(itemPosition: Int)
    fun itemNone(itemPosition: Int)
    fun itemDismiss(itemPosition: Int)
}