package hu.kszi2.android.schpincer.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import hu.kszi2.android.schpincer.R
import hu.kszi2.android.schpincer.data.OpeningItem
import hu.kszi2.android.schpincer.databinding.ItemOpeningListBinding
import hu.kszi2.android.schpincer.fragments.WelcomeFragment
import io.ktor.util.date.toDate
import java.util.Calendar
import java.util.TimeZone

class OpeningAdapter(private val fragment : WelcomeFragment) :
    RecyclerView.Adapter<OpeningAdapter.OpeningViewHolder>() {
    private val items = mutableListOf<OpeningItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        OpeningViewHolder(
            ItemOpeningListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: OpeningViewHolder, position: Int) {
        val openingItem = items[position]

        holder.binding.ivIcon.setImageResource(getImageResource(openingItem))
        holder.binding.tvCircleName.text = openingItem.circleName
        holder.binding.tvDate.text = convertLongToDate(openingItem.nextOpeningDate)

        holder.binding.llOpening.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("URL", generateUrl(openingItem))
            fragment.findNavController().navigate(R.id.action_welcomeFragment_to_webFragment, bundle)
        }
    }

    private fun generateUrl(openingItem: OpeningItem): String {
        return "https://schpincer.sch.bme.hu/provider/" +
                when (openingItem.circleName) {
                    "Pizzásch" -> "pizzasch"
                    "Americano" -> "americano"
                    "ReggeliSCH" -> "reggelisch"
                    "Vödör" -> "vodor"
                    "Lángosch" -> "langosch"
                    "Dzsájrosz" -> "dzsajrosz"
                    else -> "pizzasch"
                }
    }

    private fun convertLongToDate(epochMillis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("Europe/Budapest")
        val date = cal.toDate(epochMillis)

        return "(${date.dayOfWeek}) " +
                "${
                    date.dayOfMonth.toString().padStart(2, '0')
                }/${
                    ((date.month.ordinal) + 1).toString().padStart(2, '0')
                }/${date.year} ${
                    date.hours.toString().padStart(2, '0')
                }:${date.minutes.toString().padStart(2, '0')}"
    }

    @DrawableRes
    private fun getImageResource(openingItem: OpeningItem): Int {
        return when (openingItem.circleName) {
            "Pizzásch" -> R.drawable.ic_pizza
            "Americano" -> R.drawable.ic_americano
            "ReggeliSCH" -> R.drawable.ic_breakfast
            "Vödör" -> R.drawable.ic_bucket
            "Lángosch" -> R.drawable.ic_langos
            "Dzsájrosz" -> R.drawable.ic_gyros
            else -> R.drawable.ic_order
        }
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: OpeningItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun update(shoppingItems: List<OpeningItem>) {
        items.clear()
        items.addAll(shoppingItems)
        notifyDataSetChanged()
    }

    inner class OpeningViewHolder(val binding: ItemOpeningListBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface NewOpeningItemAdderListener {
        fun onOpeningItemCreated(newItem: OpeningItem)
    }

    interface OpeningItemClickListener {
        fun onItemChanged(item: OpeningItem)
    }
}