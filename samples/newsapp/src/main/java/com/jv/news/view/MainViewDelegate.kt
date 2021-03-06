package com.jv.news.view

import android.view.LayoutInflater
import android.view.ViewGroup
import com.joaquimverges.helium.core.event.ViewEvent
import com.joaquimverges.helium.core.state.ViewState
import com.joaquimverges.helium.core.viewdelegate.BaseViewDelegate
import com.joaquimverges.helium.navigation.state.NavDrawerState
import com.joaquimverges.helium.navigation.viewdelegate.NavDrawerViewDelegate
import com.joaquimverges.helium.navigation.viewdelegate.ToolbarViewDelegate
import com.jv.news.R

/**
 * @author joaquim
 */
class MainViewDelegate(
    inflater: LayoutInflater,
    val mainView: ArticleListViewDelegate = ArticleListViewDelegate(inflater),
    val drawerView: SourcesViewDelegate = SourcesViewDelegate(inflater),
    private val navDrawer: NavDrawerViewDelegate = NavDrawerViewDelegate(mainView, drawerView)
) : BaseViewDelegate<ViewState, ViewEvent>(R.layout.activity_main, inflater) {

    private val mainContainer = findView<ViewGroup>(R.id.main_container)

    init {
        mainContainer.addView(navDrawer.view)
    }

    override fun render(viewState: ViewState) {
        when (viewState) {
            is NavDrawerState -> navDrawer.render(viewState)
        }
    }
}