package com.example.disastertrack.view.adapter

import android.graphics.Color
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
            button.text = action.getName()
            button.isSelected = position == activeButtonPosition
            updateButtonBackground(button)
            button.setOnClickListener {
//                onButtonClick(position)
//                onButtonMarkerClick(position)
                if(position != activeButtonPosition) {
                    val previousActivePosition = activeButtonPosition
                    activeButtonPosition = position
                    notifyItemChanged(previousActivePosition)
                    notifyItemChanged(activeButtonPosition)

                    onButtonClick(position)
                    onButtonMarkerClick(position)
                }
//                button.isSelected = !button.isSelected
//                updateButtonBackground(button)

//                action.performAction()
//                // Perform action based on the button clicked
//                when (action) {
//                    "Action 1" -> performAction1()
//                    "Action 2" -> performAction2()
//                    // Add more cases for other actions as needed
//                    else -> {
//                        // Handle default action or do nothing
//                    }
//                }
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

        private fun performAction1() {
            // Implement action 1 logic here
            Toast.makeText(itemView.context, "Action 1 clicked!", Toast.LENGTH_SHORT).show()
        }


        private fun performAction2() {
            // Implement action 2 logic here
            Toast.makeText(itemView.context, "Action 2 clicked!", Toast.LENGTH_SHORT).show()
        }
    }

}