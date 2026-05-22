package com.myapp.feature.search.impl.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.myapp.core.ui.base.BaseAdapter
import com.myapp.feature.feed.api.model.Player
import com.myapp.feature.search.impl.databinding.ItemSearchPlayerBinding

class SearchPlayerAdapter(
    private val onPlayerClick: (Player) -> Unit,
) : BaseAdapter<Player, ItemSearchPlayerBinding>(
    object : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Player, newItem: Player) = oldItem == newItem
    }
) {
    override fun createBinding(parent: ViewGroup, viewType: Int): ItemSearchPlayerBinding =
        ItemSearchPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun bindItem(binding: ItemSearchPlayerBinding, item: Player, position: Int) {
        binding.textPlayerName.text = item.name
        binding.textTeamName.text = item.teamName
        binding.textNationality.text = item.nationality
        binding.textAvatarInitial.text = item.name.first().uppercase()
        binding.root.setOnClickListener { onPlayerClick(item) }
    }
}
