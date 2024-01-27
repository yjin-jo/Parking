package ddwu.com.mobile.finalreport.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ddwu.com.mobile.finalreport.databinding.ListItemBinding
import ddwu.com.mobile.finalreport.data.Parking



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
        val cnt = parkings?.get(position)?.capacity?.minus(parkings?.get(position)?.curParking!!)
        holder.itemBinding.tvRow.text = "${parkings?.get(position)?.parkingName} [ ${cnt}자리 남았어요 ]"
        holder.itemBinding.tvRow.setOnClickListener{
            clickListener?.onItemClick(it, position)
        }
    }
    interface OnItemClickListner {
        fun onItemClick(view: View, position: Int)
    }
    var clickListener: OnItemClickListner? = null
    fun setOnItemClickListener(listener: OnItemClickListner) {
        this.clickListener = listener
    }
    class ParkingHolder(val itemBinding: ListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

}