package com.example.precional.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.precional.R;
import com.example.precional.data.entity.BloodPressureRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<BloodPressureRecord> records;

    public HistoryAdapter(List<BloodPressureRecord> records) {
        this.records = records;
    }

    public void updateRecords(List<BloodPressureRecord> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BloodPressureRecord record = records.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPressure, tvDateTime, tvUserInfo, tvStatus;
        private View statusIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPressure = itemView.findViewById(R.id.tv_pressure);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvUserInfo = itemView.findViewById(R.id.tv_user_info);
            tvStatus = itemView.findViewById(R.id.tv_status);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
        }

        public void bind(BloodPressureRecord record) {
            // Mostrar presión
            String pressureText = record.getSystolic() + "/" + record.getDiastolic() + " mmHg";
            tvPressure.setText(pressureText);

            // Mostrar fecha y hora
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("es", "ES"));
            String dateTime = dateFormat.format(new Date(record.getTimestamp()));
            tvDateTime.setText(dateTime);

            // Mostrar información del usuario incluyendo sexo
            String genderText = getGenderText(record.getGender());
            String userInfo = record.getName() + ", " + record.getAge() + " años, " + genderText;
            tvUserInfo.setText(userInfo);
            tvUserInfo.setVisibility(View.VISIBLE);

            // Mostrar estado y color
            String status = record.getPressureStatus();
            int statusColor = getStatusColor(status);
            String statusText = getStatusText(status);

            tvStatus.setText(statusText);
            tvStatus.setTextColor(statusColor);
            statusIndicator.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(statusColor)
            );
        }

        private String getGenderText(String gender) {
            switch (gender) {
                case "male":
                    return itemView.getContext().getString(R.string.male);
                case "female":
                    return itemView.getContext().getString(R.string.female);
                case "other":
                    return itemView.getContext().getString(R.string.other);
                default:
                    return itemView.getContext().getString(R.string.other);
            }
        }

        private int getStatusColor(String status) {
            switch (status) {
                case "normal":
                    return itemView.getContext().getColor(R.color.normal_pressure);
                case "elevated":
                    return itemView.getContext().getColor(R.color.elevated_pressure);
                case "high":
                    return itemView.getContext().getColor(R.color.high_pressure);
                default:
                    return itemView.getContext().getColor(R.color.medium_gray);
            }
        }

        private String getStatusText(String status) {
            switch (status) {
                case "normal":
                    return itemView.getContext().getString(R.string.normal_status);
                case "elevated":
                    return itemView.getContext().getString(R.string.elevated_status);
                case "high":
                    return itemView.getContext().getString(R.string.high_status);
                default:
                    return "Desconocido";
            }
        }
    }
}
