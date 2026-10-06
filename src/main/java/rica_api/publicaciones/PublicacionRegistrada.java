package rica_api.publicaciones;

import java.time.Instant;

public record PublicacionRegistrada(
        String publicacionId,
        String investigadorCorreo,
        Instant ocurridoEn) {
}
