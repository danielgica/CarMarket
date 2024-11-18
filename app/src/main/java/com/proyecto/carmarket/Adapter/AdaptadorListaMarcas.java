package com.proyecto.carmarket.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.proyecto.carmarket.Objetos.Marca;
import com.proyecto.carmarket.R;

import java.util.List;

public class AdaptadorListaMarcas extends RecyclerView.Adapter<AdaptadorListaMarcas.ViewHolder> {

    private final List<Marca> items;
    private final FirebaseStorage firebaseStorage;

    public AdaptadorListaMarcas(List<Marca> items) {
        this.items = items;
        this.firebaseStorage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vista_de_marcas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Marca marca = items.get(position);
        holder.nombreMarca.setText(marca.getNombre());

        String nombreArchivo = marca.getUrlIcono();

        if (nombreArchivo == null || nombreArchivo.isEmpty()) {
            Log.d("AdaptadorListaMarcas", "URL vacía para la marca: " + marca.getNombre());
            holder.iconoMarca.setImageResource(R.drawable.black_bg);
        } else {
            String rutaImagen = "marcas/" + nombreArchivo;
            StorageReference storageReference = firebaseStorage.getReference().child(rutaImagen);

            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(holder.iconoMarca.getContext())
                        .load(uri.toString())
                        .placeholder(R.drawable.black_bg)
                        .error(R.drawable.black_bg)
                        .into(holder.iconoMarca);
            }).addOnFailureListener(e -> {
                Log.e("AdaptadorListaMarcas", "Error al obtener la URL de descarga", e);
                holder.iconoMarca.setImageResource(R.drawable.black_bg);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconoMarca;
        TextView nombreMarca;

        public ViewHolder(View itemView) {
            super(itemView);
            iconoMarca = itemView.findViewById(R.id.logo_marca);
            nombreMarca = itemView.findViewById(R.id.titulo_marca);
        }
    }
}
