package com.api.api.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.api.DTO.CloudinaryUploadDTO;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary){
        this.cloudinary = cloudinary;
    }

    //Función para eliminar una imagen o sonido de Cloudinary
    public void deleteFile(String publicId, boolean isRaw){
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", isRaw ? "raw" : "image"));
            if (!"ok".equals(result.get("result")) && !"not found".equals(result.get("result"))) {
                throw new RuntimeException("Error al eliminar el archivo de Cloudinary");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el archivo de Cloudinary: " + e.getMessage());
        }
    }

    //Función para subir una imagen o sonido a Cloudinary y obtener su public_url como su public_id
    public CloudinaryUploadDTO uploadMultipartFile(MultipartFile file, boolean isRaw) {
        try {
            String originalName = file.getOriginalFilename(); // => "profilePicture-user.jpg"
            String nameWithoutExtension = originalName.replaceFirst("[.][^.]+$", "");
            /*
             * "profilePicture-user", cloudinary espera que el public_id no tenga extension
             * 
             * Pero eso significa que Cloudinary generará una URL como https://res.cloudinary.com/your_cloud_name/image/upload/vXXXXXXXXX/profilePicture-user.jpg.jpg
             * 
            */
            String resourceType = isRaw ? "raw" : "image";
            Map<?,?> res = this.cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "upload_preset", "cloudinary-TFG",
                    "resource_type", resourceType,
                    //En caso de que sea un audio tenemos que guardar la extensión para que el audio pueda ser reproducido
                    "public_id", isRaw ? originalName : nameWithoutExtension
                )
            );
            String publicId = (String) res.get("public_id");
            String url = (String) res.get("secure_url");
            return new CloudinaryUploadDTO(url, publicId, isRaw ? originalName : nameWithoutExtension);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo para subir a Cloudinary", e);
        } catch (Exception e) {
            throw new RuntimeException("Error subiendo archivo a Cloudinary", e);
        }
    }
}
