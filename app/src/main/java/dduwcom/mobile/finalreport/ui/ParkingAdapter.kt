package dduwcom.mobile.finalreport.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dduwcom.mobile.finalreport.data.Parking
import dduwcom.mobile.finalreport.databinding.ListItemBinding



class ParkingAdapter : RecyclerView.Adapter<ParkingAdapter.ParkingHolder>() {
    var parkings: List<Parking>? = null

    override fun getItemCount(): Int {
        return parkings?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkingHolder {
        val itemBinding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParkingHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ParkingHolder, position: Int) {
        holder.itemBinding.tvRow.text = parkings?.get(position).toString()
        holder.itemBinding.clRow.setOnClickListener{
            clickListener?.onItemClick(it, position)
        }
    }

    class ParkingHolder(val itemBinding: ListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

    interface OnItemClickListner {
        fun onItemClick(view: View, position: Int)
    }

    var clickListener: OnItemClickListner? = null

    fun setOnItemClickListener(listener: OnItemClickListner) {
        this.clickListener = listener
    }

}