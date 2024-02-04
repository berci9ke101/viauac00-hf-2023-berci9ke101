package hu.kszi2.android.schpincer.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import hu.kszi2.android.schpincer.R
import hu.kszi2.android.schpincer.adapter.OpeningAdapter
import hu.kszi2.android.schpincer.api.Opening
import hu.kszi2.android.schpincer.api.getDeveloperOpenings
import hu.kszi2.android.schpincer.api.getOpenings
import hu.kszi2.android.schpincer.data.OpeningItem
import hu.kszi2.android.schpincer.data.OpeningListDatabase
import hu.kszi2.android.schpincer.databinding.FragmentWelcomeBinding
import hu.kszi2.android.schpincer.fragments.WelcomeFragment.LoadOpenings.loadOpeningsIntoDB
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class WelcomeFragment : Fragment(), OpeningAdapter.OpeningItemClickListener,
    OpeningAdapter.NewOpeningItemAdderListener {

    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var database: OpeningListDatabase
    private lateinit var adapter: OpeningAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoadOpenings.fragment = this
        (requireActivity() as AppCompatActivity).supportActionBar?.hide() //hide support actionbar
        setHasOptionsMenu(true) //although it is deprecated we need it to show the menu
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val toolbarMenu: Menu = binding.toolbar.menu
        inflater.inflate(R.menu.menu, toolbarMenu)
        for (i in 0 until toolbarMenu.size()) {
            val menuItem: MenuItem = toolbarMenu.getItem(i)
            menuItem.setOnMenuItemClickListener { item -> onOptionsItemSelected(item) }
            if (menuItem.hasSubMenu()) {
                val subMenu: SubMenu = menuItem.subMenu!!
                for (j in 0 until subMenu.size()) {
                    subMenu.getItem(j)
                        .setOnMenuItemClickListener { item -> onOptionsItemSelected(item) }
                }
            }
        }
        //although it is deprecated we need it to show the menu
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //database stuff
        database = OpeningListDatabase.getDatabase(requireActivity().applicationContext)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = OpeningAdapter(this)
        binding.rvOpenings.layoutManager = LinearLayoutManager(this.context)
        binding.rvOpenings.adapter = adapter
        loadItemsInBackground()
    }

    override fun onItemChanged(item: OpeningItem) {
        thread {
            database.openingItemDao().update(item)
        }
    }

    private fun loadItemsInBackground() {
        thread {
            val items = database.openingItemDao().getAll()
            requireActivity().runOnUiThread {
                adapter.update(items)
            }
        }
    }

    override fun onOpeningItemCreated(newItem: OpeningItem) {
        thread {
            val insertId = database.openingItemDao().insert(newItem)
            newItem.id = insertId
            requireActivity().runOnUiThread {
                adapter.addItem(newItem)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_order -> {
                val b = Bundle()
                b.putString("URL", "https://schpincer.sch.bme.hu")
                findNavController().navigate(R.id.action_welcomeFragment_to_webFragment, b)
                true
            }

            R.id.menu_refresh -> {
                thread {
                    loadOpeningsIntoDB()
                }
                true
            }

            R.id.menu_settings -> {
                findNavController().navigate(R.id.action_welcomeFragment_to_settingsFragment)
                true
            }

            else -> super.onOptionsItemSelected(item) //although it is deprecated we need it to show the menu
        }
    }

    internal object LoadOpenings {
        var fragment: WelcomeFragment? = null
            set(value) {
                if (field == null)
                    field = value
                else
                    throw IllegalAccessError("Property has been set!")
            }

        fun clearDb() {
            fragment ?: return
            fragment!!.database.openingItemDao().clearAll()
        }

        fun loadOpeningsIntoDB() {
            fragment ?: return
            val openings: List<Opening> = if (SettingsFragment.developer) {
                runBlocking { getDeveloperOpenings() }
            } else {
                runBlocking { getOpenings() }
            }

            if (openings.isEmpty()) {
                return
            }

            thread {
                openings.forEach {
                    val newItem = OpeningItem(
                        circleName = it.circleName,
                        nextOpeningDate = it.nextOpeningDate,
                        outOfStock = it.outOfStock
                    )

                    if (isAddedOpening(newItem)) {
                        return@forEach //if it has been added we skip
                    }

                    val insertId = fragment!!.database.openingItemDao().insert(
                        newItem
                    )
                    newItem.id = insertId
                    fragment!!.requireActivity().runOnUiThread {
                        fragment!!.adapter.addItem(newItem)
                    }
                }
            }
        }

        fun anyNewOpening(): Boolean {
            val openings: List<Opening> = if (SettingsFragment.developer) {
                runBlocking { getDeveloperOpenings() }
            } else {
                runBlocking { getOpenings() }
            }

            return openings.isNotEmpty()
        }

        private fun isAddedOpening(opening: OpeningItem): Boolean {
            fragment ?: return true
            var been = false
            thread {
                val db = fragment!!.database.openingItemDao().getAll()
                db.forEach {
                    Log.d("dbOpening", "${it.circleName}, ${it.nextOpeningDate}")
                    Log.d("Opening", "${opening.circleName}, ${opening.nextOpeningDate}")
                    if (it == opening) {
                        been = true
                        return@thread
                    }
                }
            }.join()
            return been
        }
    }
}