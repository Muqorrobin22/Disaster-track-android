package com.example.disastertrack.view.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.disastertrack.R
import com.example.disastertrack.utils.ButtonAction

class ButtonAdapter(
    private val buttonActions: List<ButtonAction>, private val onButtonClick: (Int) -> Unit,
    private val onButtonMarkerClick: (Int) -> Unit
) : RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder>() {

    private var activeButtonPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_button, parent, false)
        return ButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val buttonText = buttonActions[position]
        holder.bindButton(buttonText, position)
    }

    override fun getItemCount(): Int {
        return buttonActions.size
    }

    inner class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val button: Button = itemView.findViewById(R.id.button_disaster)

        fun bindButton(action: ButtonAction, position: Int) {
            Log.d("MainButtonSelect", "position : $position")
            button.text = action.getName()
            button.isSelected = position == activeButtonPosition
            updateButtonBackground(button)
            button.setOnClickListener {
                if(position != activeButtonPosition) {
                    val previousActivePosition = activeButtonPosition
                    activeButtonPosition = position

                    notifyDataSetChanged()

                    onButtonClick(position)
                    onButtonMarkerClick(position)
                }
            }
        }

        fun updateButtonBackground(button: Button) {
            val activeBackground = R.drawable.button_background_active
            val inactiveBackground = R.drawable.button_background_inactive

            val activeTextColor = Color.WHITE
            val inactiveTextColor = Color.BLACK

            val backgroundResource = if (button.isSelected) activeBackground else inactiveBackground
            val textResources = if (button.isSelected) activeTextColor else inactiveTextColor
            button.setBackgroundResource(backgroundResource)
            button.setTextColor(textResources)
        }

    }

    fun resetButtonState() {
        val previousActivePosition = activeButtonPosition
        activeButtonPosition = -1
        notifyItemChanged(previousActivePosition)
        notifyDataSetChanged()
    }


}