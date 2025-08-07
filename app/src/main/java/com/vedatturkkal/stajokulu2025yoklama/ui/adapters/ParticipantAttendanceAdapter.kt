import android.R.attr.visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vedatturkkal.stajokulu2025yoklama.data.model.ParticipantAttendance
import com.vedatturkkal.stajokulu2025yoklama.databinding.ItemParticipantAttendanceBinding
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel

class ParticipantAttendanceAdapter(
    private val onApprove: (participantId: Int) -> Unit,
    private val onUnapprove: (participantId: Int) -> Unit,
    private val onReject: (participantId: Int) -> Unit,
    private val onUnreject: (participantId: Int) -> Unit
) : ListAdapter<ParticipantAttendance, ParticipantAttendanceAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: ItemParticipantAttendanceBinding) : RecyclerView.ViewHolder(binding.root)

    class DiffCallback : DiffUtil.ItemCallback<ParticipantAttendance>() {
        override fun areItemsTheSame(oldItem: ParticipantAttendance, newItem: ParticipantAttendance): Boolean {
            return oldItem.participant.id == newItem.participant.id
        }

        override fun areContentsTheSame(oldItem: ParticipantAttendance, newItem: ParticipantAttendance): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemParticipantAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            participantName.text = item.participant.name
            approvalStatus.text = when {
                item.denied -> "🚫 Reddedildi"
                item.approval -> "✔ Onaylı"
                else -> "⏳ Beklemede"
            }
            checkInTime.text = "Giriş: ${item.checkInTime.ifBlank { "-" }}"
            checkOutTime.text = "Çıkış: ${item.checkOutTime.ifBlank { "-" }}"

            approveBtn.apply {
                text = if (item.approval) "Onayı Kaldır" else "Onayla"
                setOnClickListener {
                    if (item.approval) onUnapprove(item.participant.id) else onApprove(item.participant.id)
                }
            }

            rejectBtn.apply {
                visibility = View.VISIBLE
                text = if (item.denied) "Reddi Kaldır" else "Reddet"
                setOnClickListener {
                    if (item.denied) onUnreject(item.participant.id) else onReject(item.participant.id)
                }
            }
        }
    }
}

