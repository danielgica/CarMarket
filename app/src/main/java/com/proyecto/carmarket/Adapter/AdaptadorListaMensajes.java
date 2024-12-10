package com.proyecto.carmarket.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.proyecto.carmarket.Activity.VerMensaje;
import com.proyecto.carmarket.Objetos.Mensaje;
import com.proyecto.carmarket.R;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdaptadorListaMensajes extends RecyclerView.Adapter<AdaptadorListaMensajes.ViewHolder> {

    private final Context context;
    private final List<Mensaje> listaMensajes;
    private final String estadoVista;

    public AdaptadorListaMensajes(Context context, List<Mensaje> listaMensajes, String estadoVista) {
        this.context = context;
        this.listaMensajes = listaMensajes;
        this.estadoVista = estadoVista;

        Collections.sort(listaMensajes, (m1, m2) -> {
            if (m1.isLeido() != m2.isLeido()) {
                return Boolean.compare(m1.isLeido(), m2.isLeido());
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date fecha1 = sdf.parse(m1.getFecha());
                Date fecha2 = sdf.parse(m2.getFecha());
                return fecha2.compareTo(fecha1);
            } catch (Exception e) {
                return 0;
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vista_de_mensajes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mensaje mensaje = listaMensajes.get(position);

        holder.asunto.setText(mensaje.getAsunto());
        holder.fecha.setText(mensaje.getFecha());

        if ("recibidos".equals(estadoVista)) {
            holder.email.setText(mensaje.getEmailEmisor());
        } else if ("enviados".equals(estadoVista)) {
            holder.email.setText(mensaje.getEmailReceptor());
        }

        if (mensaje.isLeido()) {
            holder.layout.setBackgroundResource(R.drawable.white_dark_gb);
        } else {
            holder.layout.setBackgroundResource(R.drawable.white_bg);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, VerMensaje.class);
            intent.putExtra("mensajeId", mensaje.getId());
            intent.putExtra("estadoVista", estadoVista);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaMensajes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView asunto, email, fecha;
        ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = (ConstraintLayout) itemView;
            asunto = itemView.findViewById(R.id.vistaMensajes_asunto);
            email = itemView.findViewById(R.id.vistaMensajes_correo);
            fecha = itemView.findViewById(R.id.vistaMensajes_fecha);
        }
    }
}
