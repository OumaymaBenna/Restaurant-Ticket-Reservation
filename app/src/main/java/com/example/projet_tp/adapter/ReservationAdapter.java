package com.example.projet_tp.adapter;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.model.Reservation;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    private List<Reservation> reservationList;

    public ReservationAdapter(List<Reservation> reservationList) {
        this.reservationList = reservationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);
        holder.bind(reservation);
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textMealType, textDate;
        ImageView imageViewQRCode;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textMealType = itemView.findViewById(R.id.textMealType);
            textDate = itemView.findViewById(R.id.textDate);
            imageViewQRCode = itemView.findViewById(R.id.imageViewQRCode);
        }

        public void bind(Reservation reservation) {
            String mealType = getMealType(reservation.getMenuName());
            textMealType.setText(mealType);

            String date = reservation.getDate();
            if (date == null || date.isEmpty()) {
                date = "Date non disponible";
            }
            textDate.setText(date);

            final Reservation finalReservation = reservation;
            final String finalMealType = mealType;
            final String finalDate = date;

            String qrData = generateQRData(reservation);
            Bitmap qrBitmap = generateQRCode(qrData, 300, 300);
            if (qrBitmap != null) {
                imageViewQRCode.setImageBitmap(qrBitmap);
                
                imageViewQRCode.setOnClickListener(v -> showFullScreenQRCode(finalReservation, finalMealType, finalDate));
                imageViewQRCode.setClickable(true);
                imageViewQRCode.setFocusable(true);
            }
        }

        private void showFullScreenQRCode(Reservation reservation, String mealType, String date) {
            Dialog dialog = new Dialog(itemView.getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.dialog_qr_code);
            
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }

            ImageView qrImageView = dialog.findViewById(R.id.imageViewQRCode);
            TextView textMealName = dialog.findViewById(R.id.textMealName);
            TextView textDate = dialog.findViewById(R.id.textDate);
            MaterialButton buttonClose = dialog.findViewById(R.id.buttonClose);

            if (textMealName != null) {
                textMealName.setText("Repas: " + mealType);
            }
            if (textDate != null) {
                textDate.setText("Date: " + date);
            }
            if (buttonClose != null) {
                buttonClose.setOnClickListener(v -> dialog.dismiss());
            }

            if (qrImageView != null) {
                String qrData = generateQRData(reservation);
                Bitmap qrBitmap = generateQRCode(qrData, 1000, 1000);
                
                if (qrBitmap != null) {
                    qrImageView.setImageBitmap(qrBitmap);
                    qrImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    qrImageView.setAdjustViewBounds(true);
                } else {
                    android.util.Log.e("ReservationAdapter", "Erreur: Impossible de générer le QR code");
                }
                
                qrImageView.post(() -> {
                    int viewWidth = qrImageView.getWidth();
                    int viewHeight = qrImageView.getHeight();
                    
                    if (viewWidth > 0 && viewHeight > 0) {
                        // Générer le QR code avec la taille exacte de l'ImageView
                        String qrData2 = generateQRData(reservation);
                        Bitmap qrBitmap2 = generateQRCode(qrData2, viewWidth, viewHeight);
                        
                        if (qrBitmap2 != null) {
                            qrImageView.setImageBitmap(qrBitmap2);
                        }
                    }
                });
            }

            dialog.show();
        }

        private String getMealType(String menuName) {
            if (menuName == null || menuName.isEmpty()) {
                return "Déjeuner";
            }
            
            String lowerName = menuName.toLowerCase();
            if (lowerName.contains("déjeuner") || lowerName.contains("dejeuner") || lowerName.contains("lunch")) {
                return "Déjeuner";
            } else if (lowerName.contains("dîner") || lowerName.contains("diner") || lowerName.contains("dinner")) {
                return "Dîner";
            } else if (lowerName.contains("froid") || lowerName.contains("cold")) {
                return "Repas Froid";
            } else {
                return menuName;
            }
        }

        private String generateQRData(Reservation reservation) {
            String mealType = getMealType(reservation.getMenuName());
            String date = reservation.getDate();
            if (date == null || date.isEmpty()) {
                date = "Date non disponible";
            }
            
            return String.format("%s|%s", mealType, date);
        }

        private Bitmap generateQRCode(String data, int width, int height) {
            try {
                if (data == null || data.isEmpty()) {
                    android.util.Log.e("ReservationAdapter", "Données QR code vides");
                    return null;
                }
                
                int qrCodeSize = Math.min(width, height);
                if (qrCodeSize < 100) {
                    qrCodeSize = 100; // Taille minimale
                }
                
                java.util.Map<com.google.zxing.EncodeHintType, Object> hintMap = new java.util.HashMap<>();
                hintMap.put(com.google.zxing.EncodeHintType.MARGIN, 2);
                hintMap.put(com.google.zxing.EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);
                
                QRCodeWriter writer = new QRCodeWriter();
                BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);
                
                int matrixWidth = bitMatrix.getWidth();
                int matrixHeight = bitMatrix.getHeight();
                
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                
                bitmap.eraseColor(Color.WHITE);
                
                int scaleX = width / matrixWidth;
                int scaleY = height / matrixHeight;
                int scale = Math.min(scaleX, scaleY);
                
                if (scale <= 0) {
                    scale = 1;
                }
                
                int qrDisplayWidth = matrixWidth * scale;
                int qrDisplayHeight = matrixHeight * scale;
                int offsetX = (width - qrDisplayWidth) / 2;
                int offsetY = (height - qrDisplayHeight) / 2;
                
                for (int x = 0; x < matrixWidth; x++) {
                    for (int y = 0; y < matrixHeight; y++) {
                        if (bitMatrix.get(x, y)) {
                            for (int sx = 0; sx < scale; sx++) {
                                for (int sy = 0; sy < scale; sy++) {
                                    int px = offsetX + (x * scale) + sx;
                                    int py = offsetY + (y * scale) + sy;
                                    if (px >= 0 && px < width && py >= 0 && py < height) {
                                        bitmap.setPixel(px, py, Color.BLACK);
                                    }
                                }
                            }
                        }
                    }
                }
                
                android.util.Log.d("ReservationAdapter", "QR code généré avec succès: " + width + "x" + height);
                return bitmap;
            } catch (WriterException e) {
                android.util.Log.e("ReservationAdapter", "Erreur génération QR code: " + e.getMessage(), e);
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                android.util.Log.e("ReservationAdapter", "Erreur inattendue génération QR code: " + e.getMessage(), e);
                e.printStackTrace();
                return null;
            }
        }
    }
}