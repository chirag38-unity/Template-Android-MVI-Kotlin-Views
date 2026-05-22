package com.myapp.feature.feed.impl.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.myapp.core.ui.base.BaseAdapter
import com.myapp.feature.feed.api.model.Player
import com.myapp.feature.feed.impl.databinding.ItemPlayerBinding

class PlayerAdapter(
    private val onPlayerClick: (Player) -> Unit,
) : BaseAdapter<Player, ItemPlayerBinding>(
    object : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Player, newItem: Player) = oldItem == newItem
    }
) {
    override fun createBinding(parent: ViewGroup, viewType: Int): ItemPlayerBinding =
        ItemPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun bindItem(binding: ItemPlayerBinding, item: Player, position: Int) {
        binding.textPlayerName.text = item.name
        binding.textTeamName.text = item.teamName
        binding.textNationality.text = item.nationality
        binding.textPosition.text = item.position
        // Letter avatar: first letter of player's name
        binding.textAvatarInitial.text = item.name.first().uppercase()
        binding.root.setOnClickListener { onPlayerClick(item) }
    }
}
