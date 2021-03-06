package com.joaquimverges.helium.ui.presenter

import com.joaquimverges.helium.core.event.ViewEvent
import com.joaquimverges.helium.test.HeliumTestCase
import com.joaquimverges.helium.test.viewdelegate.TestViewDelegate
import com.joaquimverges.helium.ui.repository.BaseRepository
import com.joaquimverges.helium.ui.state.ListViewState
import com.joaquimverges.helium.ui.util.RefreshPolicy
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ListPresenterTest : HeliumTestCase() {

    class TestItem

    @Mock lateinit var refreshPolicy: RefreshPolicy
    @Mock lateinit var repo: BaseRepository<List<TestItem>>

    private lateinit var presenter: ListPresenter<TestItem, ViewEvent>
    private val testScheduler = TestScheduler()
    private val testViewDelegate = TestViewDelegate<ListViewState<List<TestItem>>, ViewEvent>()
    private val testData = Observable.range(0, 20).map { TestItem() }.toList().blockingGet()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxAndroidPlugins.setMainThreadSchedulerHandler { testScheduler }
        whenever(repo.getData()).thenReturn(Single.just(testData))
        presenter = ListPresenter(repo, refreshPolicy)
        presenter.attach(testViewDelegate)
    }

    @Test
    fun testRefreshPolicy() {
        whenever(refreshPolicy.shouldRefresh()).thenReturn(false)
        presenter.refreshIfNeeded()
        testViewDelegate.assertNothingRendered()

        whenever(refreshPolicy.shouldRefresh()).thenReturn(true)
        presenter.refreshIfNeeded()
        testViewDelegate.assertHasRendered(ListViewState.Loading<TestItem>())
    }

    @Test
    fun testRefreshWithData() {
        presenter.loadData()
        testViewDelegate.assertHasRendered(ListViewState.Loading<List<TestItem>>())
        testScheduler.triggerActions()
        testViewDelegate.assertHasRendered(ListViewState.DataReady<List<TestItem>>(testData))
        verify(refreshPolicy).updateLastRefreshedTime()
    }

    @Test
    fun testRefreshWithNoData() {
        whenever(repo.getData()).thenReturn(Single.just(emptyList()))
        presenter.loadData()
        testViewDelegate.assertHasRendered(ListViewState.Loading<List<TestItem>>())
        testScheduler.triggerActions()
        testViewDelegate.assertHasRendered(ListViewState.Empty<List<TestItem>>())
        verify(refreshPolicy).updateLastRefreshedTime()
    }

    @Test
    fun testRefreshError() {
        val error = RuntimeException("error")
        whenever(repo.getData()).thenReturn(Single.error<List<TestItem>>(error))
        presenter.loadData()
        testViewDelegate.assertHasRendered(ListViewState.Loading<List<TestItem>>())
        testScheduler.triggerActions()
        testViewDelegate.assertHasRendered(ListViewState.Error<List<TestItem>>(error))
        verify(refreshPolicy, never()).updateLastRefreshedTime()
    }
}
