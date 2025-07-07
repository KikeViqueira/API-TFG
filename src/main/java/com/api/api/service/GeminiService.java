package com.api.api.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.api.api.DTO.DrmObjectDTO;
import com.api.api.DTO.OnboardingAnswerDTO;
import com.api.api.DTO.FormRequestDTO.DRMRequestDTO;
import com.api.api.DTO.TipDTO.TipResponseDTO;
import com.api.api.model.SleepLogAnswer;
import com.api.api.model.User;

@Service
public class GeminiService {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    public GeminiService(WebClient webClient){
        this.webClient = webClient;
    }

    //Creamos la función para mandar un mensaje a la api de gemini como prompt
    public String sendMessage(String message){ 
        String apiUrl = "/models/gemini-2.0-flash:generateContent?key=" + apiKey; // Cambiar el modelo a 2.0 Flash

        System.out.println("\n\nMensaje que estamos mandado a la IA: "+ message + "\n\n");

        //PROMPT PARA INDICARLE EL FORMATO DE LA RESPUESTA A LOS MENSAJES DEL USER
        String prompt = """
            Eres un especialista en sueño y bienestar que trabaja en la aplicación ZzzTime. Tu función principal es ayudar con la interpretación de sueños y todo lo relacionado con la mejora del descanso.
            
            RESTRICCIONES IMPORTANTES:
            - ESTÁ ESTRICTAMENTE PROHIBIDO generar, crear, editar o procesar imágenes
            - NO puedes generar contenido visual de ningún tipo
            - Si el usuario solicita imágenes, explícale que solo puedes ayudar con texto
            - Enfócate únicamente en respuestas de texto relacionadas con sueño y bienestar
            
            ÁMBITO DE ESPECIALIZACIÓN:
            - Interpretación de sueños y su significado
            - Mejora de la calidad del sueño e higiene del sueño
            - Factores que afectan el descanso (estrés, alimentación, ejercicio, ambiente)
            - Técnicas de relajación y rutinas nocturnas
            - Bienestar general relacionado con el sueño
            
            ENFOQUE CONVERSACIONAL:
            - Si el usuario habla de temas que NO son directamente sobre sueño, intenta relacionarlo con el sueño de manera natural cuando sea posible
            - Ejemplo: "El sobrepeso puede afectar la calidad del sueño porque..."
            - Si no se puede relacionar naturalmente, responde de forma amable: "Mi especialidad está en el ámbito del sueño y los sueños. ¿Has notado cómo esto podría estar afectando tu descanso?"
            
            ESTILO DE COMUNICACIÓN:
            - Natural y profesional, como un especialista cercano pero competente
            - ADAPTA la longitud según lo que necesite la respuesta: 1, 2 o máximo 3 párrafos BREVES
            - Si la pregunta es simple, responde de forma concisa en 1 párrafo
            - Si requiere más detalle, usa 2-3 párrafos pero MÁS CORTOS que antes
            - Sé directo y al grano, evita repetir información ya mencionada
            - Lenguaje fluido y profesional, pero sin sonar robotico
            - NUNCA uses formato markdown, negritas, cursivas o símbolos especiales
            - Responde de manera contextual y coherente con la conversación
            
            Sé empático, profesional y natural. Adapta tu respuesta a lo que el usuario realmente necesita.
            """;

        String fullPrompt = message + "\n\n" + prompt;

        //Construímos el cuerpo de la petición
        /*
        "contents": [{
        "parts":[{"text": "Explain how AI works"}]
        }]
    */
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", fullPrompt))
            ))
        ); //Creamos un mapa con la estructura que espera la API

        //Llamada a la API de Gemini
        return webClient.post().uri(apiUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) //Especificamos el tipo de contenido, en este caso JSON
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class) // Convertimos la respuesta en String (puede ajustarse según el JSON de Gemini)
        .block();
    }


    //FUNCION PARA CREAR UN TÍTULO EN BASE AL CONTENIDO DEL MENSAJE
    public String createTitle(String message){ 
        String apiUrl = "/models/gemini-2.0-flash:generateContent?key=" + apiKey; // Cambiar el modelo a 2.0 Flash

        //PROMPT PARA INDICARLE EL FORMATO DE LA RESPUESTA A LOS MENSAJES DEL USER
        String prompt = """
            Genera un título para este chat de máximo 3 palabras relacionado con el tema de sueño o bienestar que se menciona en el mensaje.
            
            INSTRUCCIONES ESTRICTAS:
            - Máximo 3 palabras
            - Debe reflejar el tema principal del mensaje
            - Usa lenguaje profesional pero accesible
            - PROHIBIDO usar cualquier formato markdown, HTML o símbolos especiales
            - NO uses asteriscos, guiones, negritas, cursivas o cualquier formato
            - Solo texto plano, sin comillas ni símbolos adicionales
            - Si el mensaje no es sobre sueño/bienestar, usa: "Consulta General"
            
            Responde ÚNICAMENTE con el título, nada más.
            """;

        String fullPrompt = message + "\n\n" + prompt;


        //Construímos el cuerpo de la petición
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", fullPrompt))
            ))
        ); //Creamos un mapa con la estructura que espera la API

        //llamamos a la api de gemini
        return webClient.post().uri(apiUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) //Especificamos el tipo de contenido, en este caso JSON
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class) // Convertimos la respuesta en String (puede ajustarse según el JSON de Gemini)
        .block();
    }

    //Función para generar un informe sobre la roma de decisiones en base a la info del user
    public String generateReport(Map<String, Float> sleepLogsLastWeek,Map<Long, SleepLogAnswer> sleepLogsForContext, OnboardingAnswerDTO onboardingAnswerDTO, DRMRequestDTO drmRequestDTO, User user){
        String apiUrl = "/models/gemini-2.0-flash:generateContent?key=" + apiKey; // Cambiar el modelo a 2.0 Flash

        //PROMPT PARA GENERAR UN INFORME DRM PROFESIONAL ENFOCADO EN CORRELACIONES Y ANÁLISIS REAL
        String prompt = """
        Eres un especialista en psicología del sueño aplicando la metodología Day Reconstruction Method (DRM). Tu función es analizar las correlaciones específicas entre los patrones de sueño del usuario y su funcionamiento cognitivo-emocional, identificando conexiones causales y patrones de comportamiento.

        Tu análisis debe reconstruir la experiencia vivida del usuario, correlacionando episodios de sueño con su funcionamiento cognitivo y emocional durante los últimos días.

        INFORMACIÓN DISPONIBLE:
        1. Registro de sueño semanal: %s (horas dormidas cada día de la última semana; 0 = no registrado)
        
        2. Perfil del usuario: %s
           - question1: Horas habituales de sueño que necesita
           - question2: Días de actividad física por semana  
           - question4: Tipo de alimentación predominante
           - question5: Nivel de estrés diario habitual
        
        3. Edad del usuario: %d años (calculada desde su fecha de nacimiento)
        
        4. Respuestas del cuestionario DRM (Day Reconstruction Method): %s
           - drm_question1: Concentración durante el día (escala 1-10)
           - drm_question2: Influencia del sueño en toma de decisiones
           - drm_question3: Estado de ánimo general durante el día
           - drm_question4: Nivel de energía experimentado
           - drm_question5: Productividad percibida durante el día
        
        5. Contexto adicional de registros de sueño: %s (análisis detallado de patrones específicos)


        ENFOQUE METODOLÓGICO DRM:
        Analiza cómo la variabilidad en el sueño durante la semana se correlaciona directamente con las puntuaciones DRM reportadas. Identifica patrones específicos donde déficits o excesos de sueño impactaron measurablemente en capacidades cognitivas y regulación emocional.

        ANÁLISIS REQUERIDO:
        Establece correlaciones directas entre horas de sueño específicas y las puntuaciones DRM. Por ejemplo, si durmió 5 horas el martes y reporta concentración de 4/10, conecta causalmente esa privación con el deterioro cognitivo. Si durmió 8 horas el viernes y reporta energía de 8/10, correlaciona esa recuperación con el rendimiento.

        Evalúa cómo las fluctuaciones de sueño crearon un efecto dominó en la toma de decisiones. Analiza si días con sueño inadecuado llevaron a decisiones impulsivas, procrastinación o reducción en la capacidad de análisis, y cómo esto se refleja en las respuestas DRM.

        Examina el patrón emocional resultante. Determina si la irregularidad del sueño generó inestabilidad emocional, irritabilidad o cambios de humor que se manifiestan en las puntuaciones de estado de ánimo y productividad reportadas.

        Integra el perfil individual (edad, estrés, actividad física) para contextualizar por qué ciertos déficits de sueño tuvieron mayor o menor impacto en este usuario específico comparado con estándares poblacionales.

        FORMATO DE RESPUESTA:
        Redacta en párrafos corridos, sin usar markdown, negritas, cursivas, viñetas o numeraciones. Usa un lenguaje científico pero directo que establezca conexiones causales claras. No hagas meros resúmenes de datos, sino análisis interpretativos que revelen insights sobre cómo el sueño moldea específicamente la experiencia diaria de este usuario.

        No ofrezcas consejos ni recomendaciones. Enfócate exclusivamente en el análisis correlacional y las implicaciones psicológicas de los patrones identificados. Longitud: 350-500 palabras con alto valor analítico.
        """;

        String sleepLogsString = sleepLogsLastWeek.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue() + " horas")
                .collect(Collectors.joining(", "));

        String onboardingString = onboardingAnswerDTO.toString();
        String drmString = drmRequestDTO.toString();
        String contextString = sleepLogsForContext.entrySet().stream()
                .map(entry -> "ID " + entry.getKey() + ": " + entry.getValue().toString())
                .collect(Collectors.joining("; "));

        String formattedPrompt = String.format(prompt, sleepLogsString, onboardingString, user.getAge(), drmString, contextString);

        //Construímos el cuerpo de la petición
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", formattedPrompt))
            ))
        ); //Creamos un mapa con la estructura que espera la API

        //llamamos a la api de gemini
        return webClient.post().uri(apiUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) //Especificamos el tipo de contenido, en este caso JSON
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class) // Convertimos la respuesta en String (puede ajustarse según el JSON de Gemini)
        .block();
    }


     //Función para generar un tip que mejores los hábitos en general del user
     public String generateTip(Map<String, Float> sleepLogsLastWeek,Map<Long, SleepLogAnswer> sleepLogsForContext, OnboardingAnswerDTO onboardingAnswerDTO, DrmObjectDTO drmObjectDTO, User user, List<TipResponseDTO> userTips){
        String apiUrl = "/models/gemini-2.0-flash:generateContent?key=" + apiKey; // Cambiar el modelo a 2.0 Flash

        //PROMPT PARA INDICARLE EL FORMATO DEL INFORME DE LA TOMA DE DECISIONES Y QUE ES LO QUE TIENE QUE TENER EN CUENTA PARA HACERLO
        String prompt = """
        Eres un especialista en bienestar y sueño. Genera un tip personalizado y práctico que el usuario pueda implementar fácilmente en su entorno cotidiano (tanto en casa como fuera) para mejorar su calidad de vida.

        INFORMACIÓN DEL USUARIO:
        1. Registro de sueño semanal: %s (horas dormidas cada día de la última semana; 0 = no registrado)
        
        2. Perfil del usuario: %s
           - question1: Horas habituales de sueño que necesita
           - question2: Días de actividad física por semana
           - question4: Tipo de alimentación predominante
           - question5: Nivel de estrés diario habitual
        
        3. Edad del usuario: %d años (calculada desde su fecha de nacimiento)
        
        4. Análisis DRM actual: %s
           - drm_question1: Concentración durante el día (escala 1-10)
           - drm_question2: Influencia del sueño en toma de decisiones
           - drm_question3: Estado de ánimo general durante el día
           - drm_question4: Nivel de energía experimentado
           - drm_question5: Productividad percibida durante el día
        
        5. Contexto de patrones de sueño: %s (registros detallados para personalización)
        
        6. Tips previos del usuario: %s

        CATEGORÍAS DISPONIBLES (equilibra entre estas opciones):
        🍎 Alimentación - Consejos nutricionales y hábitos alimentarios
        🏃 Ejercicio - Actividad física y movimiento
        😴 Sueño - Higiene del sueño y rutinas nocturnas  
        🧘 Bienestar Mental - Manejo del estrés y relajación
        ⏰ Rutinas - Organización del tiempo y hábitos diarios
        🏠 Ambiente - Optimización del espacio personal
        💧 Hidratación - Consumo de líquidos y bienestar
        📱 Tecnología - Uso consciente de dispositivos

        OBJETIVO: DIVERSIDAD Y EQUILIBRIO
        - Analiza las categorías de los tips previos del usuario
        - Prioriza categorías menos representadas en su historial
        - Si hay desbalance (ej: 5 tips de sueño, 0 de alimentación), genera uno de alimentación
        - Busca equilibrio sin sacrificar relevancia personal

        FORMATO DEL TIP:
        Título: [3-5 palabras descriptivos]
        Categoría: [Una de las 8 categorías mencionadas]
        Descripción: [150-200 palabras]
        - Explicación clara del beneficio
        - Pasos específicos y realizables
        - Personalizado según su perfil y análisis DRM
        - Implementable tanto en casa como fuera

        INSTRUCCIONES:
        - Personaliza según edad, hábitos y resultados DRM
        - No repitas tips similares a los ya generados
        - Usa lenguaje motivador y accesible
        - Incluye acciones específicas y medibles
        - Asegura que sea realista y alcanzable
        - NO uses markdown ni formato especial
        """;
        
        String sleepLogsString = sleepLogsLastWeek.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue() + " horas")
                .collect(Collectors.joining(", "));

        String onboardingString = onboardingAnswerDTO.toString();
        String drmString = drmObjectDTO.toString();
        String contextString = sleepLogsForContext.entrySet().stream()
                .map(entry -> "ID " + entry.getKey() + ": " + entry.getValue().toString())
                .collect(Collectors.joining("; "));
                
        String userTipsString = userTips.stream()
                .map(tip -> tip.getTitle() + " (Categoría: " + tip.getIcon() + ")")
                .collect(Collectors.joining(", "));

        String formattedPrompt = String.format(prompt, sleepLogsString, onboardingString, user.getAge(), drmString, contextString, userTipsString);



        //Construímos el cuerpo de la petición
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", formattedPrompt))
            ))
        ); //Creamos un mapa con la estructura que espera la API

        //llamamos a la api de gemini
        return webClient.post().uri(apiUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) //Especificamos el tipo de contenido, en este caso JSON
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class) // Convertimos la respuesta en String (puede ajustarse según el JSON de Gemini)
        .block();
    }
   
}
