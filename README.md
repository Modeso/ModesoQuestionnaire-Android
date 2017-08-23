# ModesoQuestionnaire-Android

<p align="center">
  <img src="https://media.licdn.com/mpr/mpr/shrink_200_200/AAEAAQAAAAAAAAZsAAAAJDM2NTU0MDA1LTA3YmEtNGUyMC05YmZjLTIxMDNlZWZlM2ZkMQ.png">
</p>

ModesoQuestionnaire-Android is an android questionnaire written mostly in Kotlin. It enable creating questionnaire with a lot of features and ease of use

- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Options](#options)
- [Communication](#communication)
- [Credits](#credits)
- [License](#license)

## Requirements

- minSdkVersion 16

## Installation

## Usage

- in **XML**
```
<ch.modeso.mcompoundquestionnaire.MCompoundQuestionnaire
        android:id="@+id/mcompound_questionnaire"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:mcqAcceptColor="@color/colorAccept"
        app:mcqAcceptDrawable="@drawable/ic_check"
        app:mcqCancelColor="@color/colorCancel"
        app:mcqCancelDrawable="@drawable/ic_close"
        app:mcqCardBackgroundDrawable="@drawable/card_bg"
        app:mcqCardTextColor="@color/colorAccent"
        app:mcqIndicatorBackgroundColor="@color/colorPrimaryDark"
        app:mcqIndicatorDrawableIcon="@drawable/ic_indicator"
        app:mcqIndicatorSizeFraction="2.5"
        app:mcqIndicatorUpperColor="@color/colorPrimary"
        app:mcqNotApplicableColor="@color/colorNotApplicable"
        app:mcqNotApplicableDrawable="@drawable/ic_not_applicable" />
```
- in **Activity** or **Fragment**
```
  mcompound_questionnaire.updateList(title.toMutableList())
  mcompound_questionnaire.cardInteractionCallBacks = this
```

## Options
- XML **Attributes**
  - **mcqIndicatorBackgroundColor**: indicator inner background color
  - **mcqIndicatorUpperColor**: indicator upper background color
  - **mcqIndicatorLowerColor**: indicator lower background color
  - **mcqIndicatorDrawableIcon**: indicator drawable icon
  - **mcqIndicatorSizeFraction**: indicator height relative to inner background color (value 1 :indcator height = inner background height)
  - **mcqCardTextColor**: card text color
  - **mcqAcceptColor**: accept color
  - **mcqCancelColor**: cancel color
  - **mcqNotApplicableColor**: not applicable color
  - **mcqAcceptDrawable**: card accept button drawable
  - **mcqCancelDrawable**: card cancel button drawable
  - **mcqNotApplicableDrawable**: card not applicable icon drawable
  - **mcqNotApplicableArrowDrawable**: card not applicable arrow drawable
  - **mcqCardBackgroundDrawable**: card background drawable

- **CardInteractionCallBacks** this is card callbacks for each action:
  - itemAcceptClick(itemId: String): card acceppt button has been clicked
  - itemCancelClick(itemId: String): card cancel button has been clicked
  - itemDismiss(itemId: String): card swiped to not applicable
  - itemNone(itemId: String): user undo last action

## Communication

- If you **found a bug**, open an issue.
- If you **have a feature request**, open an issue.
- If you **want to contribute**, submit a pull request.

## Credits

ModesoQuestionnaire-Android is owned and maintained by [Modeso](http://modeso.ch). You can follow them on Twitter at [@modeso_ch](https://twitter.com/modeso_ch) for project updates and releases.

## License

ModesoQuestionnaire-Android is released under the MIT license. See LICENSE for details.
