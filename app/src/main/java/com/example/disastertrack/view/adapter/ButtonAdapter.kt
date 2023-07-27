package com.example.disastertrack.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.disastertrack.R

class ButtonAdapter(private val buttonActions: List<String>) : RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_button, parent, false)
        return ButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val buttonText = buttonActions[position]
        holder.bindButton(buttonText)
    }

    override fun getItemCount(): Int {
        return buttonActions.size
    }

    inner class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val button: Button = itemView.findViewById(R.id.button_disaster)

        fun bindButton(action: String) {
            button.text = action
            button.setOnClickListener {
                // Perform action based on the button clicked
                when (action) {
                    "Action 1" -> performAction1()
                    "Action 2" -> performAction2()
                    // Add more cases for other actions as needed
                    else -> {
                        // Handle default action or do nothing
                    }
                }
            }
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