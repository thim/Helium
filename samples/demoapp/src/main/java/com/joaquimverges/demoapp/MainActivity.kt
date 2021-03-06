package com.joaquimverges.demoapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.joaquimverges.demoapp.view.GridSpacingDecorator
import com.joaquimverges.helium.core.event.ClickEvent
import com.joaquimverges.helium.ui.presenter.ListPresenter
import com.joaquimverges.helium.ui.repository.BaseRepository
import com.joaquimverges.helium.ui.viewdelegate.BaseRecyclerViewItem
import com.joaquimverges.helium.ui.viewdelegate.CardRecyclerItem
import com.joaquimverges.helium.ui.viewdelegate.ListViewDelegate
import io.reactivex.Observable
import io.reactivex.Single

class MainActivity : AppCompatActivity() {

    // DATA

    enum class MenuItem(val title: String) {
        LIST_NOT_RETAINED("List (not retained)"),
        LIST_RETAINED("List (retained)"),
        LIST_RETAINED_FRAGMENT("List Fragment (retained)"),
        ADVANCED_LIST("Advanced List"),
        CARD_LIST("Card List"),
        VIEW_PAGER("ViewPager"),
    }

    class MenuRepository : BaseRepository<List<MenuItem>> {
        private fun getMenuItems() = MenuItem.values().toList()
        override fun getData(): Single<List<MenuItem>> = Observable.fromIterable(getMenuItems()).toList()
    }

    // PRESENTER

    class MenuPresenter : ListPresenter<MenuItem, ClickEvent<MenuItem>>(MenuRepository()) {

        override fun onViewEvent(event: ClickEvent<MenuItem>) {
            val context = event.view.context
            val activityClass = when (event.data) {
                MenuItem.LIST_NOT_RETAINED -> SimpleListActivity::class.java
                MenuItem.LIST_RETAINED -> SimpleListActivityRetained::class.java
                MenuItem.LIST_RETAINED_FRAGMENT -> SimpleListFragmentActivityRetained::class.java
                MenuItem.ADVANCED_LIST -> AdvancedListActivity::class.java
                MenuItem.CARD_LIST -> CardListActivity::class.java
                MenuItem.VIEW_PAGER -> ViewPagerActivity::class.java
            }
            context.startActivity(Intent(context, activityClass))
        }
    }

    // VIEW

    class MenuRecyclerItem(inflater: LayoutInflater,
                           parent: ViewGroup,
                           root: View = inflater.inflate(R.layout.menu_item_layout, parent, false))
        : CardRecyclerItem<MenuItem, ClickEvent<MenuItem>>(root, inflater, parent) {

        private val title = root.findViewById<TextView>(R.id.menu_title)

        override fun bind(data: MenuItem) {
            title.text = data.title
            view.setOnClickListener { pushEvent(ClickEvent(view, data)) }
        }
    }

    // ACTIVITY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewDelegate = ListViewDelegate(layoutInflater, { inflater, container ->
            MenuRecyclerItem(inflater, container)
        }, recyclerViewConfig = {
            it.addItemDecoration(GridSpacingDecorator(resources.getDimensionPixelSize(R.dimen.menu_padding)))
        })
        MenuPresenter().attach(viewDelegate)
        setContentView(viewDelegate.view)
    }
}
