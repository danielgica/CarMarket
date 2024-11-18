package com.proyecto.carmarket.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.proyecto.carmarket.Activity.MainActivity;
import com.proyecto.carmarket.Activity.VerAnuncio;
import com.proyecto.carmarket.Objetos.Anuncio;
import com.proyecto.carmarket.R;
import java.util.List;

public class AdaptadorListaAnuncios extends RecyclerView.Adapter<AdaptadorListaAnuncios.ViewHolder> {

    private final Context context;
    private final List<Anuncio> listaAnuncios;

    public AdaptadorListaAnuncios(Context context, List<Anuncio> listaAnuncios) {
        this.context = context;
        this.listaAnuncios = listaAnuncios;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.preview_de_anuncio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Anuncio anuncio = listaAnuncios.get(position);
        holder.marca.setText(anuncio.getMarca() + " " + anuncio.getModelo());
        holder.precio.setText(anuncio.getPrecio() + " €");

        if (anuncio.getFotos() != null && !anuncio.getFotos().isEmpty()) {
            Glide.with(context)
                    .load(anuncio.getFotoPrincipal())
                    .placeholder(R.drawable.black_bg)
                    .error(R.drawable.black_bg)
                    .into(holder.foto);
        } else {
            holder.foto.setImageResource(R.drawable.intro_car);
        }

        holder.itemView.setOnClickListener(v -> {
            ProgressBar progressBar = ((Activity) context).findViewById(R.id.menu_progressBar);
            progressBar.setVisibility(View.VISIBLE);

            String id = anuncio.getId();

            Intent intent = new Intent(context, VerAnuncio.class);

            intent.putExtra("ID_ANUNCIO", id);

            context.startActivity(intent);

            progressBar.setVisibility(View.GONE);
            ((Activity) context).finish();
        });
    }

    @Override
    public int getItemCount() {
        return listaAnuncios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView foto;
        final TextView marca;
        final TextView precio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foto = itemView.findViewById(R.id.preview_foto);
            marca = itemView.findViewById(R.id.preview_marca);
            precio = itemView.findViewById(R.id.preview_precio);
        }
    }
}
